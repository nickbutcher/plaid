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
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

/**
 * A {@link Checkable} {@link ImageButton} which has a minimum offset i.e. translation Y.
 */
public class FABToggle extends ImageButton implements Checkable {

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

    private boolean isChecked = false;
    private int minOffset;

    public FABToggle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOffset(int offset) {
        offset = Math.max(minOffset, offset);
        if (getTranslationY() != offset) {
            setTranslationY(offset);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setMinOffset(int minOffset) {
        this.minOffset = minOffset;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        if (this.isChecked != isChecked) {
            this.isChecked = isChecked;
            refreshDrawableState();
        }
    }

    public void toggle() {
        setChecked(!isChecked);
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

}
