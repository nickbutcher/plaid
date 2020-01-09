/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.designernews.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.plaidapp.core.designernews.data.login.model.LoggedInUser

/**
 * The Room database for this app
 */
@Database(entities = [LoggedInUser::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DesignerNewsDatabase : RoomDatabase() {

    abstract fun loggedInUserDao(): LoggedInUserDao

    companion object {

        private const val DATABASE_NAME = "plaid-db"

        // For Singleton instantiation
        @Volatile private var instance: DesignerNewsDatabase? = null

        fun getInstance(context: Context): DesignerNewsDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DesignerNewsDatabase {
            return Room.databaseBuilder(
                context, DesignerNewsDatabase::class.java,
                DATABASE_NAME
            ).build()
        }
    }
}
