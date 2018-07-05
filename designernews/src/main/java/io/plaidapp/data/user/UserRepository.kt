package io.plaidapp.data.user

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.model.User
import java.io.IOException

typealias UserId = Long

class UserRepository(private val service: DesignerNewsService) {

    private val cachedUsers = mutableMapOf<Long, User>()

    suspend fun getUsers(ids: List<Long>): Result<List<User>> {
        // find the ids in the cached users first and only request the ones that we don't have yet
        val (cached, notCached) = getCachedAndNonCachedUsers(ids)
        val requestIds = notCached.joinToString(",")
        val result = getUsersFromRemote(requestIds)
        // save the new users in the cachedUsers
        if (result is Result.Success) {
            result.data.map { cachedUsers[it.id] = it }
        }
        // compute the list of users requested
        val users = ids.mapNotNull { cachedUsers[it] }
        if (users.isNotEmpty()) {
            return Result.Success(users)
        }
        return Result.Error(IOException("Unable to get users"))
    }

    private suspend fun getUsersFromRemote(ids: String): Result<List<User>> {
        val response = service.getUsers(ids).await()
        return if (response.isSuccessful && response.body() != null) {
            Result.Success(response.body().orEmpty())
        } else {
            Result.Error(IOException("Error getting users ${response.code()} ${response.message()}"))
        }
    }

    private fun getCachedAndNonCachedUsers(ids: List<UserId>): CachedNotCachedUserIds {
        val result = ids.groupBy { cachedUsers.containsKey(it) }
        return CachedNotCachedUserIds(result[true].orEmpty(), result[false].orEmpty())
    }

    private data class CachedNotCachedUserIds(
            val cachedUserIds: List<UserId>,
            val notCachedUserIds: List<UserId>
    )
}

