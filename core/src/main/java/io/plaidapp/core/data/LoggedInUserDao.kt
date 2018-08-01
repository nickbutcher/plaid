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

package io.plaidapp.core.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.plaidapp.core.designernews.data.users.model.LoggedInUser

@Dao
interface LoggedInUserDao {
    @Query("SELECT * FROM logged_in_user LIMIT 1")
    fun getLoggedInUser(): LiveData<LoggedInUser>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLoggedInUser(loggedInUser: LoggedInUser)
}
