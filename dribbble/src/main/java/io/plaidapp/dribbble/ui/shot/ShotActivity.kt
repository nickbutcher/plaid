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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.util.TypedValue
import androidx.core.view.doOnPreDraw
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.plaidapp.core.ui.widget.ElasticDragDismissFrameLayout
import io.plaidapp.core.util.Activities
import io.plaidapp.core.util.AnimUtils.getFastOutSlowInInterpolator
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.ViewUtils
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper
import io.plaidapp.core.util.delegates.contentView
import io.plaidapp.core.util.event.EventObserver
import io.plaidapp.core.util.glide.getBitmap
import io.plaidapp.dribbble.R
import io.plaidapp.dribbble.databinding.ActivityDribbbleShotBinding
import io.plaidapp.dribbble.domain.ShareShotInfo
import io.plaidapp.dribbble.provideShotViewModelFactory

/**
 * Activity displaying a single Dribbble shot.
 */
class ShotActivity : AppCompatActivity() {

    private val binding by contentView<ShotActivity, ActivityDribbbleShotBinding>(
        R.layout.activity_dribbble_shot
    )
    private var chromeFader: ElasticDragDismissFrameLayout.SystemChromeFader? = null

    private lateinit var viewModel: ShotViewModel

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
            binding.shot.background = null
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

        val factory = provideShotViewModelFactory(shotId, this)
        viewModel = ViewModelProviders.of(this, factory).get(ShotViewModel::class.java)
        viewModel.openLink.observe(this, EventObserver { openLink(it) })
        viewModel.shareShot.observe(this, EventObserver { shareShot(it) })
        binding.viewModel = viewModel
        binding.shotUiModel = viewModel.shot // TODO this should be a UI Model
        binding.shotLoadListener = shotLoadListener

        binding.bodyScroll.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            binding.shot.offset = -scrollY
        }
        binding.back.setOnClickListener { setResultAndFinish() }
        chromeFader = object : ElasticDragDismissFrameLayout.SystemChromeFader(this) {
            override fun onDragDismissed() {
                setResultAndFinish()
            }
        }
        postponeEnterTransition()
        binding.shot.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.draggableFrame.addListener(chromeFader)
    }

    override fun onPause() {
        binding.draggableFrame.removeListener(chromeFader)
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
        binding.shotSpacer.background = ViewUtils.createRipple(
            palette, 0.25f, 0.5f,
            ContextCompat.getColor(this@ShotActivity, io.plaidapp.R.color.mid_grey), true
        )
        // slightly more opaque ripple on the pinned image to compensate for the scrim
        binding.shot.foreground = ViewUtils.createRipple(
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
            binding.back.setColorFilter(
                ContextCompat.getColor(this@ShotActivity, io.plaidapp.R.color.dark_icon)
            )
        }

        // color the status bar.
        var statusBarColor = window.statusBarColor
        ColorUtils.getMostPopulousSwatch(palette)?.let {
            statusBarColor = ColorUtils.scrimify(it.rgb, isDark, SCRIM_ADJUSTMENT)
            // set a light status bar
            if (!isDark) {
                ViewUtils.setLightStatusBar(binding.shot)
            }
        }

        if (statusBarColor != window.statusBarColor) {
            binding.shot.setScrimColor(statusBarColor)
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
