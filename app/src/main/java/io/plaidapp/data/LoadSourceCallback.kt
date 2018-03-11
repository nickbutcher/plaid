package io.plaidapp.data

interface LoadSourceCallback {

    fun sourceLoaded(result: List<PlaidItem>?, page: Int, source: String)

    fun loadFailed(source: String)
}