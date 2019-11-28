/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.core.feed

import android.app.Activity
import android.app.ActivityOptions
import android.app.SharedElementCallback
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.util.ViewPreloadSizeProvider
import io.plaidapp.core.R
import io.plaidapp.core.data.PlaidItem
import io.plaidapp.core.data.pocket.PocketUtils
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.designernews.ui.stories.StoryViewHolder
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.producthunt.data.api.model.Post
import io.plaidapp.core.producthunt.ui.ProductHuntPostHolder
import io.plaidapp.core.ui.DribbbleShotHolder
import io.plaidapp.core.ui.HomeGridItemAnimator
import io.plaidapp.core.ui.transitions.ReflowText
import io.plaidapp.core.util.Activities
import io.plaidapp.core.util.customtabs.CustomTabActivityHelper
import io.plaidapp.core.util.glide.DribbbleTarget
import io.plaidapp.core.util.glide.GlideApp
import io.plaidapp.core.util.intentTo

/**
 * Adapter for displaying a grid of [PlaidItem]s.
 */
class FeedAdapter(
    // we need to hold on to an activity ref for the shared element transitions :/
    private val host: Activity,
    private val columns: Int,
    private val pocketIsInstalled: Boolean,
    private val isDarkTheme: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ListPreloader.PreloadModelProvider<Shot> {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(host)
    private val shotLoadingPlaceholders: Array<ColorDrawable?>
    private val shotPreloadSizeProvider = ViewPreloadSizeProvider<Shot>()

    @ColorInt
    private val initialGifBadgeColor: Int
    private var showLoadingMore = false
    private val loadingMoreItemPosition: Int
        get() = if (showLoadingMore) itemCount - 1 else RecyclerView.NO_POSITION

    var items: List<PlaidItem> = emptyList()
        /**
         * Main entry point for setting items to this adapter.
         */
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    init {
        setHasStableIds(true)

        // get the dribbble shot placeholder colors & badge color from the theme
        val a = host.obtainStyledAttributes(R.styleable.DribbbleFeed)
        val loadingColorArrayId =
            a.getResourceId(R.styleable.DribbbleFeed_shotLoadingPlaceholderColors, 0)
        if (loadingColorArrayId != 0) {
            val placeholderColors = host.resources.getIntArray(loadingColorArrayId)
            shotLoadingPlaceholders = arrayOfNulls(placeholderColors.size)
            placeholderColors.indices.forEach {
                shotLoadingPlaceholders[it] = ColorDrawable(placeholderColors[it])
            }
        } else {
            shotLoadingPlaceholders = arrayOf(ColorDrawable(Color.DKGRAY))
        }
        val initialGifBadgeColorId = a.getResourceId(R.styleable.DribbbleFeed_initialBadgeColor, 0)
        initialGifBadgeColor = if (initialGifBadgeColorId != 0) {
            ContextCompat.getColor(host, initialGifBadgeColorId)
        } else {
            0x40ffffff
        }
        a.recycle()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DESIGNER_NEWS_STORY -> createDesignerNewsStoryHolder(parent)
            TYPE_DRIBBBLE_SHOT -> createDribbbleShotHolder(parent)
            TYPE_PRODUCT_HUNT_POST -> createProductHuntStoryHolder(parent)
            TYPE_LOADING_MORE -> LoadingMoreHolder(
                layoutInflater.inflate(R.layout.infinite_loading, parent, false)
            )
            else -> throw IllegalStateException("Unsupported View type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_DESIGNER_NEWS_STORY -> (holder as StoryViewHolder).bind((getItem(position) as Story))
            TYPE_DRIBBBLE_SHOT -> bindDribbbleShotHolder(
                (getItem(position) as Shot), holder as DribbbleShotHolder, position
            )
            TYPE_PRODUCT_HUNT_POST -> (holder as ProductHuntPostHolder).bind((getItem(position) as Post))
            TYPE_LOADING_MORE -> bindLoadingViewHolder(holder as LoadingMoreHolder, position)
            else -> throw IllegalStateException("Unsupported View type")
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is DribbbleShotHolder) {
            // reset the badge & ripple which are dynamically determined
            holder.reset()
        }
    }

    private fun createDesignerNewsStoryHolder(parent: ViewGroup): StoryViewHolder {

        return StoryViewHolder(
            layoutInflater.inflate(R.layout.designer_news_story_item, parent, false),
            pocketIsInstalled,
            { (_, _, _, url), position ->
                PocketUtils.addToPocket(host, url)
                // notify changed with a payload asking RV to run the anim
                notifyItemChanged(position, HomeGridItemAnimator.ADD_TO_POCKET)
                Unit
            },
            { data ->
                openDesignerNewsStory(data)
                Unit
            },
            { data ->
                if (data.story.url != null) {
                    openTabDesignerNews(data.story)
                } else {
                    openDesignerNewsStory(data)
                }
                Unit
            }
        )
    }

    private fun openDesignerNewsStory(data: StoryViewHolder.TransitionData) {
        val intent = intentTo(Activities.DesignerNews.Story)
        intent.putExtra(Activities.DesignerNews.Story.EXTRA_STORY_ID, data.story.id)
        ReflowText.addExtras(intent, ReflowText.ReflowableTextView(data.title))

        // on return, fade the pocket & comments buttons in
        host.setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(
                sharedElementNames: List<String>,
                sharedElements: List<View>,
                sharedElementSnapshots: List<View>
            ) {
                host.setExitSharedElementCallback(null)
                notifyItemChanged(data.position, HomeGridItemAnimator.STORY_COMMENTS_RETURN)
            }
        })

        val options = ActivityOptions.makeSceneTransitionAnimation(
            host,
            *data.sharedElements
        )
        host.startActivity(intent, options.toBundle())
    }

    private fun openTabDesignerNews(story: Story) {
        CustomTabActivityHelper.openCustomTab(
            host,
            Activities.DesignerNews.Story
                .customTabIntent(host, story, null).build(),
            Uri.parse(story.url)
        )
    }

    private fun createDribbbleShotHolder(parent: ViewGroup): DribbbleShotHolder {
        return DribbbleShotHolder(
            layoutInflater.inflate(R.layout.dribbble_shot_item, parent, false),
            initialGifBadgeColor,
            isDarkTheme
        ) { view, position ->
            val intent = intentTo(Activities.Dribbble.Shot)
            intent.putExtra(
                Activities.Dribbble.Shot.EXTRA_SHOT_ID,
                getItem(position)!!.id
            )
            val options = ActivityOptions.makeSceneTransitionAnimation(
                host,
                Pair.create(view, host.getString(R.string.transition_shot)),
                Pair.create(view, host.getString(R.string.transition_shot_background))
            )
            host.startActivityForResult(intent, REQUEST_CODE_VIEW_SHOT, options.toBundle())
            Unit
        }
    }

    private fun bindDribbbleShotHolder(
        shot: Shot,
        holder: DribbbleShotHolder,
        position: Int
    ) {
        val imageSize = shot.images.bestSize()
        GlideApp.with(host)
            .load(shot.images.best())
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    if (!shot.hasFadedIn) {
                        holder.fade()
                        shot.hasFadedIn = true
                    }
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ) = false
            })
            .placeholder(shotLoadingPlaceholders[position % shotLoadingPlaceholders.size])
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .fitCenter()
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(imageSize.width, imageSize.height)
            .into(DribbbleTarget(holder.image, false))
        // need both placeholder & background to prevent seeing through shot as it fades in
        shotLoadingPlaceholders[position % shotLoadingPlaceholders.size]?.apply {
            holder.prepareForFade(
                this,
                shot.animated,
                // need a unique transition name per shot, let's use its url
                shot.htmlUrl
            )
        }
        shotPreloadSizeProvider.setView(holder.image)
    }

    private fun createProductHuntStoryHolder(parent: ViewGroup): ProductHuntPostHolder {
        return ProductHuntPostHolder(
            layoutInflater.inflate(R.layout.product_hunt_item, parent, false),
            { post ->
                openTabForProductHunt(post.discussionUrl)
                Unit
            },
            { post ->
                openTabForProductHunt(post.redirectUrl)
                Unit
            })
    }

    private fun openTabForProductHunt(uri: String) {
        CustomTabActivityHelper.openCustomTab(
            host,
            CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(host, R.color.product_hunt))
                .addDefaultShareMenuItem()
                .build(),
            Uri.parse(uri)
        )
    }

    private fun bindLoadingViewHolder(holder: LoadingMoreHolder, position: Int) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.setVisibility(if (position > 0 && showLoadingMore) View.VISIBLE else View.INVISIBLE)
    }

    override fun getItemViewType(position: Int): Int {
        if (position < items.size && items.isNotEmpty()) {
            val item = getItem(position)
            when (item) {
                is Story -> return TYPE_DESIGNER_NEWS_STORY
                is Shot -> return TYPE_DRIBBBLE_SHOT
                is Post -> return TYPE_PRODUCT_HUNT_POST
            }
        }
        return TYPE_LOADING_MORE
    }

    private fun getItem(position: Int): PlaidItem? {
        return if (position < 0 || position >= items.size) null else items[position]
    }

    fun getItemColumnSpan(position: Int): Int {
        return if (getItemViewType(position) == TYPE_LOADING_MORE) {
            columns
        } else {
            getItem(position)!!.colspan
        }
    }

    override fun getItemId(position: Int): Long {
        return if (getItemViewType(position) == TYPE_LOADING_MORE) {
            -1L
        } else {
            getItem(position)?.id ?: -1L
        }
    }

    fun getItemPosition(itemId: Long): Int {
        items.forEachIndexed { index, plaidItem ->
            if (plaidItem.id == itemId) return index
        }
        return RecyclerView.NO_POSITION
    }

    override fun getItemCount(): Int {
        return items.size + if (showLoadingMore) 1 else 0
    }

    fun dataStartedLoading() {
        if (showLoadingMore) return
        showLoadingMore = true
        notifyItemInserted(loadingMoreItemPosition)
    }

    fun dataFinishedLoading() {
        if (!showLoadingMore) return
        val loadingPos = loadingMoreItemPosition
        showLoadingMore = false
        notifyItemRemoved(loadingPos)
    }

    override fun getPreloadItems(position: Int): List<Shot> {
        val item = getItem(position)
        return if (item is Shot) {
            listOf(item)
        } else {
            emptyList()
        }
    }

    override fun getPreloadRequestBuilder(item: Shot): RequestBuilder<Drawable>? {
        return GlideApp.with(host).load(item.images.best())
    }

    private class LoadingMoreHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        private val progress = itemView as ProgressBar

        fun setVisibility(visibility: Int) {
            progress.visibility = visibility
        }
    }

    companion object {

        const val REQUEST_CODE_VIEW_SHOT = 5407

        private const val TYPE_DESIGNER_NEWS_STORY = 0
        private const val TYPE_DRIBBBLE_SHOT = 1
        private const val TYPE_PRODUCT_HUNT_POST = 2
        private const val TYPE_LOADING_MORE = -1

        fun createSharedElementReenterCallback(
            context: Context
        ): SharedElementCallback {
            val shotTransitionName = context.getString(R.string.transition_shot)
            val shotBackgroundTransitionName =
                context.getString(R.string.transition_shot_background)
            return object : SharedElementCallback() {

                /**
                 * We're performing a slightly unusual shared element transition i.e. from one view
                 * (image in the grid) to two views (the image & also the background of the details
                 * view, to produce the expand effect). After changing orientation, the transition
                 * system seems unable to map both shared elements (only seems to map the shot, not
                 * the background) so in this situation we manually map the background to the
                 * same view.
                 */
                override fun onMapSharedElements(
                    names: List<String>,
                    sharedElements: MutableMap<String, View>
                ) {
                    if (sharedElements.size != names.size) {
                        // couldn't map all shared elements
                        sharedElements[shotTransitionName]?.let {
                            // has shot so add shot background, mapped to same view
                            sharedElements[shotBackgroundTransitionName] = it
                        }
                    }
                }
            }
        }
    }
}
