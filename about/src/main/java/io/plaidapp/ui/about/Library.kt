package io.plaidapp.ui.about

/**
 * Models an open source library we want to credit
 */
internal class Library(
        val name: String,
        val description: String,
        val link: String,
        val imageUrl: String,
        val circleCrop: Boolean
)
