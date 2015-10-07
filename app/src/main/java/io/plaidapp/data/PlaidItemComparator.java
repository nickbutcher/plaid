/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.plaidapp.data;

import java.util.Comparator;

/**
 * A comparator that compares {@link PlaidItem}s based on their {@code weight} attribute.
 */
public class PlaidItemComparator implements Comparator<PlaidItem> {

    @Override
    public int compare(PlaidItem lhs, PlaidItem rhs) {
        return Float.compare(lhs.weight, rhs.weight);
    }
}
