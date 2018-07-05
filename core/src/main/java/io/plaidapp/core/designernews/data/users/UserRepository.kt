package io.plaidapp.core.designernews.data.users

import io.plaidapp.core.data.Result
import io.plaidapp.core.designernews.data.api.DesignerNewsService
import io.plaidapp.core.designernews.data.api.model.User
import java.io.IOException

class UserRepository(private val service: DesignerNewsService) {

    private val cachedUsers = mutableMapOf<Long, User>()

    suspend fun getUsers(ids: Set<Long>): Result<List<User>> {
        // find the ids in the cached users first and only request the ones that we don't have yet
        val notCachedUsers = ids.filterNot { cachedUsers.containsKey(it) }
        val requestIds = notCachedUsers.joinToString(",")
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

    companion object {
        @Volatile
        private var INSTANCE: UserRepository? = null

        fun getInstance(service: DesignerNewsService): UserRepository {
            return INSTANCE
                    ?: synchronized(this) {
                        INSTANCE ?: UserRepository(service).also { INSTANCE = it }
                    }
        }
    }
}

