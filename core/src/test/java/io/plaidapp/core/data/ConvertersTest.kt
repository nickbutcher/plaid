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

import org.junit.Test

import org.junit.Assert.assertEquals

class ConvertersTest {

    @Test fun csvToStringArray_oneValue() {
        // Given a non-empty CSV string with one value
        val csv = "1"

        // When the string is converted via the type converter
        val actualStringList = Converters().csvToStringArray(csv)

        // Then it should return a list with one element
        assertEquals(listOf("1"), actualStringList)
    }

    @Test fun csvToStringArray_multipleValues() {
        // Given a non-empty CSV string with multiple values
        val csv = "1,2,3"

        // When the string is converted via the type converter
        val actualStringList = Converters().csvToStringArray(csv)

        // Then it should return a list of the strings, split by the delimiter
        assertEquals(listOf("1", "2", "3"), actualStringList)
    }

    @Test fun csvToStringArray_emptyString() {
        // Given an empty string
        val csv = ""

        // When the string is converted via the type converter
        val actualStringList = Converters().csvToStringArray(csv)

        // Then it should return an empty list
        assertEquals(emptyList<String>(), actualStringList)
    }

    @Test fun stringListToCsv_oneValue() {
        // Given a list with one element
        val list = listOf("1")

        // When the list is converted via the type converter
        val actualCsv = Converters().stringListToCsv(list)

        // Then it should return a CSV string with one value
        assertEquals("1", actualCsv)
    }

    @Test fun stringListToCsv_multipleValues() {
        // Given a list with multiple elements
        val list = listOf("1", "2", "3")

        // When the list is converted via the type converter
        val actualCsv = Converters().stringListToCsv(list)

        // Then it should return a CSV string with multiple values
        assertEquals("1,2,3", actualCsv)
    }

    @Test fun stringListToCsv_emptyList() {
        // Given an empty list
        val list = emptyList<String>()

        // When the list is converted via the type converter
        val actualCsv = Converters().stringListToCsv(list)

        // Then it should return an empty string
        assertEquals("", actualCsv)
    }
}
