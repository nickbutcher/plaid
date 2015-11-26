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
import android.widget.TextView;

import io.plaidapp.R;

/**
 * An extension to TextView which supports a custom state of {@link #STATE_ORIGINAL_POSTER} for
 * denoting that a comment author was the original poster.
 */
public class AuthorTextView extends BaselineGridTextView {

    private static final int[] STATE_ORIGINAL_POSTER = {R.attr.state_original_poster};

    private boolean isOP = false;

    public AuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isOP) {
            mergeDrawableStates(drawableState, STATE_ORIGINAL_POSTER);
        }
        return drawableState;
    }

    public boolean isOriginalPoster() {
        return isOP;
    }

    public void setOriginalPoster(boolean isOP) {
        if (this.isOP != isOP) {
            this.isOP = isOP;
            refreshDrawableState();
        }
    }
}
