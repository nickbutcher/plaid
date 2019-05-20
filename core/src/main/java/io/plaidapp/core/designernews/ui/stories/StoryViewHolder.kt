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

package io.plaidapp.core.designernews.ui.stories

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import androidx.recyclerview.widget.RecyclerView
import android.util.Pair
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import io.plaidapp.core.R
import io.plaidapp.core.designernews.data.stories.model.Story
import io.plaidapp.core.ui.recyclerview.Divided
import io.plaidapp.core.ui.transitions.GravityArcMotion
import io.plaidapp.core.ui.widget.BaselineGridTextView
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ViewUtils
import java.util.Arrays

class StoryViewHolder(
    itemView: View,
    pocketIsInstalled: Boolean,
    private val onPocketClicked: (story: Story, adapterPosition: Int) -> Unit,
    private val onCommentsClicked: (data: TransitionData) -> Unit,
    private val onItemClicked: (data: TransitionData) -> Unit
) : RecyclerView.ViewHolder(itemView), Divided {
    private var story: Story? = null
    private val title: BaselineGridTextView = itemView.findViewById(R.id.story_title)
    private val comments: TextView = itemView.findViewById(R.id.story_comments)
    private val pocket: ImageButton = itemView.findViewById(R.id.pocket)

    init {
        pocket.apply {
            visibility = if (pocketIsInstalled) View.VISIBLE else View.GONE
            if (pocketIsInstalled) {
                imageAlpha = 178 // grumble... no xml setter, grumble...
                setOnClickListener { story?.let { story -> onPocketClicked(story, adapterPosition) } }
            }
        }
        comments.setOnClickListener {
            story?.let { story ->
                val data =
                    TransitionData(
                        story,
                        adapterPosition,
                        title,
                        getSharedElementsForTransition(),
                        itemView
                    )
                onCommentsClicked(data)
            }
        }
        itemView.setOnClickListener {
            story?.let { story ->
                val data =
                    TransitionData(
                        story,
                        adapterPosition,
                        title,
                        getSharedElementsForTransition(),
                        itemView
                    )
                onItemClicked(data)
            }
        }
    }

    fun bind(story: Story) {
        this.story = story
        title.text = story.title
        title.alpha = 1f // interrupted add to pocket anim can mangle
        comments.text = story.commentCount.toString()
        itemView.transitionName = story.url
    }

    private fun getSharedElementsForTransition(): Array<Pair<View, String>> {
        val resources = itemView.context.resources
        return arrayOf(Pair(title as View, resources.getString(R.string.transition_story_title)),
                Pair(itemView, resources.getString(R.string.transition_story_title_background)),
                Pair(itemView, resources.getString(R.string.transition_story_background)))
    }

    fun createAddToPocketAnimator(): Animator {
        // setup for anim
        (pocket.parent.parent as ViewGroup).clipChildren = false
        val initialLeft = pocket.left
        val initialTop = pocket.top
        val translatedLeft = (itemView.width - pocket.width) / 2
        val translatedTop = initialTop - (itemView.height - pocket.height) / 2
        val arc = GravityArcMotion()

        // animate the title & pocket icon up, scale the pocket icon up
        val titleMoveFadeOut = ObjectAnimator.ofPropertyValuesHolder(
                title,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, -(itemView.height / 5).toFloat()),
                PropertyValuesHolder.ofFloat(View.ALPHA, 0.54f))

        val pocketMoveUp = ObjectAnimator.ofFloat(pocket,
                View.TRANSLATION_X, View.TRANSLATION_Y,
                arc.getPath(initialLeft.toFloat(), initialTop.toFloat(), translatedLeft.toFloat(), translatedTop.toFloat()))
        val pocketScaleUp = ObjectAnimator.ofPropertyValuesHolder(pocket,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 3f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 3f))
        val pocketFadeUp = ObjectAnimator.ofInt<ImageView>(pocket,
                ViewUtils.IMAGE_ALPHA, 255)

        val up = AnimatorSet().apply {
            playTogether(titleMoveFadeOut, pocketMoveUp, pocketScaleUp, pocketFadeUp)
            duration = 300L
            interpolator = AnimUtils.getFastOutSlowInInterpolator(itemView.context)
        }

        // animate everything back into place
        val titleMoveFadeIn = ObjectAnimator.ofPropertyValuesHolder(title,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f))
        val pocketMoveDown = ObjectAnimator.ofFloat(pocket,
                View.TRANSLATION_X, View.TRANSLATION_Y,
                arc.getPath(translatedLeft.toFloat(), translatedTop.toFloat(), 0f, 0f))
        val pvhPocketScaleDown = ObjectAnimator.ofPropertyValuesHolder(pocket,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f))
        val pocketFadeDown = ObjectAnimator.ofInt<ImageView>(pocket,
                ViewUtils.IMAGE_ALPHA, 178)

        val down = AnimatorSet().apply {
            playTogether(titleMoveFadeIn, pocketMoveDown, pvhPocketScaleDown, pocketFadeDown)
            startDelay = 500L
            duration = 300L
            interpolator = AnimUtils.getFastOutSlowInInterpolator(itemView.context)
        }

        return AnimatorSet().apply {
            playSequentially(up, down)

            doOnEnd { (pocket.parent.parent as ViewGroup).clipChildren = true }
            doOnCancel {
                title.apply {
                    alpha = 1f
                    translationY = 0f
                }

                pocket.apply {
                    translationX = 0f
                    translationY = 0f
                    scaleX = 1f
                    scaleY = 1f
                    imageAlpha = 178
                }
            }
        }
    }

    fun createStoryCommentReturnAnimator(): Animator {
        val animator = AnimatorSet()
        animator.playTogether(
                ObjectAnimator.ofFloat(pocket, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(comments, View.ALPHA, 0f, 1f))
        animator.duration = 120L
        animator.interpolator = AnimUtils.getLinearOutSlowInInterpolator(itemView.context)
        animator.doOnCancel {
            pocket.alpha = 1f
            comments.alpha = 1f
        }
        return animator
    }

    /**
     * Data needed for creating transitions from this view to the story view.
     */
    data class TransitionData(
        val story: Story,
        val position: Int,
        val title: BaselineGridTextView,
        val sharedElements: Array<Pair<View, String>>,
        val itemView: View
    ) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TransitionData

            if (position != other.position) return false
            if (title != other.title) return false
            if (!Arrays.equals(sharedElements, other.sharedElements)) return false
            if (itemView != other.itemView) return false

            return true
        }

        override fun hashCode(): Int {
            var result = position
            result = 31 * result + title.hashCode()
            result = 31 * result + Arrays.hashCode(sharedElements)
            result = 31 * result + itemView.hashCode()
            return result
        }
    }
}
