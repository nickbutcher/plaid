/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.core.data;

import java.util.Comparator;
import java.util.List;

/**
 *  Classes related to sorting {@link PlaidItem}s.
 */
public class PlaidItemSorting {

    /**
     * A comparator that compares {@link PlaidItem}s based on their {@code weight} attribute.
     */
    public static class PlaidItemComparator implements Comparator<PlaidItem> {

        @Override
        public int compare(PlaidItem lhs, PlaidItem rhs) {
            return Float.compare(lhs.getWeight(), rhs.getWeight());
        }
    }

    /**
     *  Interface for weighing a group of {@link PlaidItem}s
     */
    public interface PlaidItemGroupWeigher<T extends PlaidItem> {
        void weigh(List<T> items);
    }

    /**
     *  Applies a weight to a group of {@link PlaidItem}s according to their natural order.
     */
    public static class NaturalOrderWeigher implements PlaidItemGroupWeigher<PlaidItem> {

        @Override
        public void weigh(List<PlaidItem> items) {
            final float step = 1f / (float) items.size();
            for (int i = 0; i < items.size(); i++) {
                PlaidItem item = items.get(i);
                item.setWeight(item.getPage() + ((float) i) * step);
            }
        }
    }
}
