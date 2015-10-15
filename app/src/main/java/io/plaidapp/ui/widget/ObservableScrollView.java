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

package io.plaidapp.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * An extension to {@link ScrollView} which exposes a scroll listener.
 */
public class ObservableScrollView extends ScrollView {

    private OnScrollListener listener;

    public ObservableScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setListener(OnScrollListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onScrollChanged(int currentScrollX,
                                   int currentScrollY,
                                   int oldScrollX,
                                   int oldScrollY) {
        super.onScrollChanged(currentScrollX, currentScrollY, oldScrollX, oldScrollY);
        if (listener != null) {
            listener.onScrolled(currentScrollY);
        }
    }

    public interface OnScrollListener {
        void onScrolled(int scrollY);
    }
}
