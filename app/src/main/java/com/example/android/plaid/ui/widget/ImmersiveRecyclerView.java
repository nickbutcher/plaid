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

package com.example.android.plaid.ui.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * An extension to RecyclerView intended to render behind system chrome (status bar and nav bar).
 * This adds the concepts of content and chrome (bottom) padding which lets us update them
 * independently. Separating these concepts allows us to alter the content padding (e.g. to make
 * room for a FAB) without having to re-query the chrome padding.
 */
public class ImmersiveRecyclerView extends RecyclerView {

    private int chromePaddingBottom;
    private int contentPaddingBottom;

    public ImmersiveRecyclerView(Context context) {
        super(context);
    }

    public ImmersiveRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImmersiveRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getContentPaddingBottom() {
        return contentPaddingBottom;
    }

    public void setContentPaddingBottom(int contentPaddingBottom) {
        this.contentPaddingBottom = contentPaddingBottom;
        updatePadding();
    }

    public int getChromePaddingBottom() {
        return chromePaddingBottom;
    }

    public void setChromePaddingBottom(int chromePaddingBottom) {
        this.chromePaddingBottom = chromePaddingBottom;
        updatePadding();
    }

    private void updatePadding() {
        setPadding(getPaddingLeft(),
                getPaddingTop(),
                getPaddingRight(),
                contentPaddingBottom + chromePaddingBottom);
    }
}
