package io.plaidapp.base.designernews.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import io.plaidapp.base.R
import io.plaidapp.base.designernews.data.api.model.Story
import io.plaidapp.base.ui.recyclerview.Divided
import io.plaidapp.base.ui.widget.BaselineGridTextView

class DesignerNewsStoryHolder(
        itemView: View,
        pocketIsInstalled: Boolean,
        private val onPocketClicked: (story: Story, adapterPosition: Int) -> Unit
) : RecyclerView.ViewHolder(itemView), Divided {
    private val story: Story? = null
    val title: BaselineGridTextView = itemView.findViewById(R.id.story_title)
    val comments: TextView = itemView.findViewById(R.id.story_comments)
    val pocket: ImageButton = itemView.findViewById(R.id.pocket)

    init {
        pocket.visibility = if (pocketIsInstalled) View.VISIBLE else View.GONE
        if (pocketIsInstalled) {
            pocket.setImageAlpha(178) // grumble... no xml setter, grumble...
            pocket.setOnClickListener { story?.let { onPocketClicked(it, adapterPosition) } }
        }
    }

    fun bind(story: Story) {
        title.text = story.title
        title.alpha = 1f // interrupted add to pocket anim can mangle
        comments.text = story.comment_count.toString()
        itemView.transitionName = story.url
    }
}
