package io.plaidapp.base.producthunt.data.api

import io.plaidapp.base.data.prefs.SourceManager
import io.plaidapp.base.producthunt.data.api.model.Post
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductHuntRepository(private val service: ProductHuntService) {
    private val inflight: MutableMap<String, Call<*>> = HashMap()

    fun loadProductHuntData(
            page: Int,
            onSuccess: (List<Post>) -> Unit,
            onError: (String) -> Unit
    ) {
        val postsCall = service.getPosts(page - 1)
        postsCall.enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                val result = response.body()
                if (response.isSuccessful && result != null) {
                    onSuccess(result)
                } else {
                    onError("Unable to load Product Hunt data")
                }

                inflight.remove(SourceManager.SOURCE_PRODUCT_HUNT)
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                onError("Unable to load Product Hunt data ${t.message}")
                inflight.remove(SourceManager.SOURCE_PRODUCT_HUNT)
            }
        })
        inflight[SourceManager.SOURCE_PRODUCT_HUNT] = postsCall
    }

    fun cancelAllRequests() {
        for (request in inflight.values) request.cancel()
    }

    companion object {
        @Volatile
        private var sINSTANCE: ProductHuntRepository? = null

        fun getInstance(service: ProductHuntService): ProductHuntRepository {
            return sINSTANCE ?: synchronized(this) {
                sINSTANCE
                        ?: ProductHuntRepository(service).also { sINSTANCE = it }
            }
        }
    }
}