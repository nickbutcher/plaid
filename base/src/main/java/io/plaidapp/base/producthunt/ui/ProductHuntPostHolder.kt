package io.plaidapp.base.producthunt.ui

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

import io.plaidapp.base.R
import io.plaidapp.base.producthunt.data.api.model.Post
import io.plaidapp.base.ui.recyclerview.Divided

/**
 * ViewHolder for a Product Hunt Post
 */
class ProductHuntPostHolder(
        itemView: View,
        private val commentsClicked: (post: Post) -> Unit,
        private val viewClicked: (post: Post) -> Unit
) : RecyclerView.ViewHolder(itemView), Divided {

    private var post: Post? = null
    private var title: TextView = itemView.findViewById(R.id.hunt_title)
    private var tagline: TextView = itemView.findViewById(R.id.tagline)
    private var comments: TextView = itemView.findViewById(R.id.story_comments)

    init {
        comments.setOnClickListener { post?.let { commentsClicked(it) } }
        itemView.setOnClickListener { post?.let { viewClicked(it) } }
    }

    fun bind(item: Post) {
        post = item
        title.text = item.name
        tagline.text = item.tagline
        comments.text = item.comments_count.toString()
    }
}
