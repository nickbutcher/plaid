package io.plaidapp.ui.about

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade

import io.plaidapp.about.R
import io.plaidapp.base.util.glide.GlideApp

internal class LibraryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var image: ImageView = itemView.findViewById(R.id.library_image)
    var name: TextView = itemView.findViewById(R.id.library_name)
    var description: TextView = itemView.findViewById(R.id.library_description)
    var link: Button = itemView.findViewById(R.id.library_link)

    fun bind(lib: Library) {
        name.text = lib.name
        description.text = lib.description
        val request = GlideApp.with(image.context)
                .load(lib.imageUrl)
                .transition(withCrossFade())
                .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
        if (lib.circleCrop) {
            request.circleCrop()
        }
        request.into(image)
    }
}
