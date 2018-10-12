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

package io.plaidapp.core.data.database

import androidx.room.TypeConverter

/**
 * Type converters to allow Room to reference complex data types.
 */
class Converters {

    @TypeConverter fun csvToLongArray(csvString: String): List<Long> {
        return if (csvString.isEmpty()) {
            emptyList()
        } else {
            csvString.split(CSV_DELIMITER).map { it.toLong() }
        }
    }

    @TypeConverter fun longListToCsv(longList: List<Long>): String {
        return longList.joinToString(CSV_DELIMITER)
    }

    companion object {
        private const val CSV_DELIMITER = ","
    }
}
