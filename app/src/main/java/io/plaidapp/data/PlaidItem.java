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

/**
 * Base class for all model types
 */
public abstract class PlaidItem {

    public final long id;
    public final String title;
    public String url; // can't be final as some APIs use different serialized names
    public String dataSource;
    public int page;
    public float weight;
    public float weightBoost;
    public int colspan;

    public PlaidItem(long id,
                     String title,
                     String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * Equals check based on the id field
     */
    @Override
    public boolean equals(Object o) {
        return (o.getClass() == getClass() && ((PlaidItem) o).id == id);
    }
}
