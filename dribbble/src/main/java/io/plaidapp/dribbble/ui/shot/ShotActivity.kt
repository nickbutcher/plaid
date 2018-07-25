/*
 * Copyright 2018 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.dribbble.ui.shot

import android.animation.ValueAnimator
import android.app.Activity
import android.app.assist.AssistContent
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.plaidapp.core.ui.widget.ElasticDragDismissFrameLayout
import io.plaidapp.core.ui.widget.ParallaxScrimageView
import io.plaidapp.core.util.Activities
import io.plaidapp.core.util.AnimUtils.getFastOutSlowInInterpolator
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.HtmlUtils
import io.plaidapp.core.util.ViewUtils
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper
import io.plaidapp.core.util.event.EventObserver
import io.plaidapp.core.util.glide.GlideApp
import io.plaidapp.core.util.glide.getBitmap
import io.plaidapp.dribbble.R
import io.plaidapp.dribbble.domain.ShareShotInfo
import io.plaidapp.dribbble.provideShotViewModelFactory
import java.text.NumberFormat

/**
 * Activity displaying a single Dribbble shot.
 */
class ShotActivity : AppCompatActivity() {

    private var draggableFrame: ElasticDragDismissFrameLayout? = null
    private var bodyScroll: NestedScrollView? = null
    private var likeCount: Button? = null
    private var viewCount: Button? = null
    private var share: Button? = null
    private var playerAvatar: ImageView? = null
    private var title: TextView? = null
    private var description: TextView? = null
    private var playerName: TextView? = null
    private var shotTimeAgo: TextView? = null
    private var chromeFader: ElasticDragDismissFrameLayout.SystemChromeFader? = null
    private var back: ImageButton? = null
    private var imageView: ParallaxScrimageView? = null
    private var shotSpacer: View? = null

    private lateinit var viewModel: ShotViewModel
    private var largeAvatarSize: Int = 0

    // TODO invoke with data binding
    private val shotClick = OnClickListener { viewModel.viewShotRequested() }

    private val shotLoadListener = object : RequestListener<Drawable> {
        override fun onResourceReady(
            resource: Drawable,
            model: Any,
            target: Target<Drawable>,
            dataSource: DataSource,
            isFirstResource: Boolean
        ): Boolean {
            val bitmap = resource.getBitmap() ?: return false

            Palette.from(bitmap)
                .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                .generate { palette -> applyFullImagePalette(palette) }

            val twentyFourDip = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                24f,
                this@ShotActivity.resources.displayMetrics
            ).toInt()
            Palette.from(bitmap)
                .maximumColorCount(3)
                .clearFilters()
                .setRegion(0, 0, bitmap.width - 1, twentyFourDip) /* - 1 to work around
                        https://code.google.com/p/android/issues/detail?id=191013 */
                .generate { palette -> applyTopPalette(bitmap, palette) }

            // TODO should keep the background if the image contains transparency?!
            imageView?.background = null
            return false
        }

        override fun onLoadFailed(
            e: GlideException?,
            model: Any,
            target: Target<Drawable>,
            isFirstResource: Boolean
        ) = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shotId = intent.getLongExtra(Activities.Dribbble.Shot.EXTRA_SHOT_ID, -1L)
        if (shotId == -1L) {
            finishAfterTransition()
        }

        setContentView(R.layout.activity_dribbble_shot)
        bindResources()

        val factory = provideShotViewModelFactory(shotId, this)
        viewModel = ViewModelProviders.of(this, factory).get(ShotViewModel::class.java)
        viewModel.openLink.observe(this, EventObserver { openLink(it) })
        viewModel.shareShot.observe(this, EventObserver { shareShot(it) })

        bodyScroll?.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            imageView?.offset = -scrollY
        }
        back?.setOnClickListener { setResultAndFinish() }
        chromeFader = object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            override fun onDragDismissed() {
                setResultAndFinish()
            }
        }
        bindShot()
    }

    override fun onResume() {
        super.onResume()
        draggableFrame?.addListener(chromeFader)
    }

    override fun onPause() {
        draggableFrame?.removeListener(chromeFader)
        super.onPause()
    }

    override fun onBackPressed() {
        setResultAndFinish()
    }

    override fun onNavigateUp(): Boolean {
        setResultAndFinish()
        return true
    }

    override fun onProvideAssistContent(outContent: AssistContent) {
        outContent.webUri = Uri.parse(viewModel.shot.url)
    }

    private fun bindResources() {
        draggableFrame = findViewById(R.id.draggable_frame)
        back = findViewById(R.id.back)
        imageView = findViewById(R.id.shot)
        bodyScroll = findViewById(R.id.body_scroll)
        shotSpacer = findViewById(R.id.shot_spacer)
        title = findViewById(R.id.shot_title)
        description = findViewById(R.id.shot_description)
        likeCount = findViewById(R.id.shot_like_count)
        viewCount = findViewById(R.id.shot_view_count)
        share = findViewById(R.id.shot_share_action)
        playerName = findViewById(R.id.player_name)
        playerAvatar = findViewById(R.id.player_avatar)
        shotTimeAgo = findViewById(R.id.shot_time_ago)
        largeAvatarSize = resources.getDimensionPixelSize(io.plaidapp.R.dimen.large_avatar_size)
    }

    private fun bindShot() {
        val shot = viewModel.shot
        val res = resources

        // load the main image
        val (width, height) = shot.images.bestSize()
        GlideApp.with(this)
            .load(shot.images.best())
            .listener(shotLoadListener)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .priority(Priority.IMMEDIATE)
            .override(width, height)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView!!)
        imageView?.setOnClickListener(shotClick)
        shotSpacer?.setOnClickListener(shotClick)

        postponeEnterTransition()
        imageView?.viewTreeObserver?.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView?.viewTreeObserver?.removeOnPreDrawListener(this)
                    startPostponedEnterTransition()
                    return true
                }
            })

        title?.text = shot.title
        if (!shot.description.isNullOrEmpty()) {
            val descText = HtmlUtils.parseHtml(
                shot.description,
                ContextCompat.getColorStateList(this, R.color.dribbble_links),
                ContextCompat.getColor(this, io.plaidapp.R.color.dribbble_link_highlight)
            )
            HtmlUtils.setTextWithNiceLinks(description!!, descText)
        } else {
            description?.visibility = GONE
        }
        val nf = NumberFormat.getInstance()
        likeCount?.text = res.getQuantityString(
            io.plaidapp.R.plurals.likes,
            shot.likesCount.toInt(),
            nf.format(shot.likesCount)
        )
        likeCount?.setOnClickListener {
            (likeCount!!.compoundDrawables[1] as AnimatedVectorDrawable).start()
        }
        viewCount?.text = res.getQuantityString(
            io.plaidapp.R.plurals.views,
            shot.viewsCount.toInt(),
            nf.format(shot.viewsCount)
        )
        viewCount?.setOnClickListener {
            (viewCount!!.compoundDrawables[1] as? AnimatedVectorDrawable)?.start()
        }
        share?.setOnClickListener {
            (share!!.compoundDrawables[1] as AnimatedVectorDrawable).start()
            viewModel.shareShotRequested()
        }
        playerName?.text = shot.user.name.toLowerCase()
        GlideApp.with(this)
            .load(shot.user.highQualityAvatarUrl)
            .circleCrop()
            .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
            .override(largeAvatarSize, largeAvatarSize)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(playerAvatar!!)
        if (shot.createdAt != null) {
            shotTimeAgo?.text = DateUtils.getRelativeTimeSpanString(
                shot.createdAt!!.time,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
            ).toString().toLowerCase()
        }
    }

    private fun openLink(url: String) {
        CustomTabActivityHelper.openCustomTab(
            this,
            CustomTabsIntent.Builder()
                .setToolbarColor(
                    ContextCompat.getColor(this@ShotActivity, io.plaidapp.R.color.dribbble)
                )
                .addDefaultShareMenuItem()
                .build(),
            url
        )
    }

    private fun shareShot(shareInfo: ShareShotInfo) {
        with(shareInfo) {
            ShareCompat.IntentBuilder.from(this@ShotActivity)
                .setText(shareText)
                .setSubject(title)
                .setStream(imageUri)
                .setType(mimeType)
                .startChooser()
        }
    }

    internal fun applyFullImagePalette(palette: Palette) {
        // color the ripple on the image spacer (default is grey)
        shotSpacer?.background = ViewUtils.createRipple(
            palette, 0.25f, 0.5f,
            ContextCompat.getColor(this@ShotActivity, io.plaidapp.R.color.mid_grey), true
        )
        // slightly more opaque ripple on the pinned image to compensate for the scrim
        imageView?.foreground = ViewUtils.createRipple(
            palette, 0.3f, 0.6f,
            ContextCompat.getColor(this@ShotActivity, io.plaidapp.R.color.mid_grey), true
        )
    }

    internal fun applyTopPalette(bitmap: Bitmap, palette: Palette) {
        val lightness = ColorUtils.isDark(palette)
        val isDark = if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
            ColorUtils.isDark(bitmap, bitmap.width / 2, 0)
        } else {
            lightness == ColorUtils.IS_DARK
        }

        if (!isDark) { // make back icon dark on light images
            back?.setColorFilter(
                ContextCompat.getColor(this@ShotActivity, io.plaidapp.R.color.dark_icon)
            )
        }

        // color the status bar.
        var statusBarColor = window.statusBarColor
        ColorUtils.getMostPopulousSwatch(palette)?.let {
            statusBarColor = ColorUtils.scrimify(it.rgb, isDark, SCRIM_ADJUSTMENT)
            // set a light status bar
            if (!isDark) {
                ViewUtils.setLightStatusBar(imageView!!)
            }
        }

        if (statusBarColor != window.statusBarColor) {
            imageView?.setScrimColor(statusBarColor)
            ValueAnimator.ofArgb(window.statusBarColor, statusBarColor).apply {
                addUpdateListener { animation ->
                    window.statusBarColor = animation.animatedValue as Int
                }
                duration = 1000L
                interpolator = getFastOutSlowInInterpolator(this@ShotActivity)
            }.start()
        }
    }

    internal fun setResultAndFinish() {
        val resultData = Intent().apply {
            putExtra(Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID, viewModel.shot.id)
        }
        setResult(Activity.RESULT_OK, resultData)
        finishAfterTransition()
    }

    companion object {
        private const val SCRIM_ADJUSTMENT = 0.075f
    }
}
