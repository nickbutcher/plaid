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
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.IntDef;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.plaidapp.R;

/**
 * A pretty rough and ready implementation of
 * http://www.google.com/design/spec/components/text-fields.html#text-fields-floating-labels
 */
public class FloatLabel extends FrameLayout {

    public static final int MODE_HINT = 0;
    public static final int MODE_FLOAT_LABEL = 1;
    private static final int DEFAULT_LABEL_TEXT_SIZE = 12; // SP
    private static final int ANIMATION_DURATION = 150; // ms
    private TextView label;
    private EditText editText;
    private int labelTextColor;
    private int accentColor;
    private float labelTextSize;
    private
    @LabelMode
    int currentMode = -1;
    private Interpolator interp;
    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s)) {
                showHint(true);
            } else {
                showFloatLabel(true);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    public FloatLabel(Context context) {
        super(context);
        init(null, 0);
    }

    public FloatLabel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FloatLabel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        label = new TextView(getContext());

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.FloatLabel, defStyle, 0);

        label.setText(a.getString(R.styleable.FloatLabel_label));
        labelTextSize = a.getDimensionPixelSize(R.styleable.FloatLabel_labelTextSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_LABEL_TEXT_SIZE,
                        getResources().getDisplayMetrics()));
        label.setTextSize(labelTextSize);
        if (a.hasValue(R.styleable.FloatLabel_labelTextColor)) {
            labelTextColor = a.getColor(R.styleable.FloatLabel_labelTextColor, 0);
        } else {
            TypedValue hintTextColor = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.textColorHint, hintTextColor,
                    true);
            labelTextColor = hintTextColor.data;
        }
        label.setTextColor(labelTextColor);
        // we scale the label down later so need to setup the pivot
        label.setPivotX(0f);
        label.setPivotY(0f);

        // when focused set the label to the accent color to match the EditText background
        TypedValue accent = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.colorAccent, accent, true);
        accentColor = accent.data;

        a.recycle();

        addView(label, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        interp = AnimationUtils.loadInterpolator(getContext(), android.R.interpolator
                .fast_out_slow_in);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (child instanceof EditText) {
            params = setEditText((EditText) child, params);
        }
        super.addView(child, index, params);
    }

    private LayoutParams setEditText(EditText editText, ViewGroup.LayoutParams lp) {
        if (this.editText != null) {
            throw new IllegalArgumentException("We already have an EditText; there can be only " +
                    "one.");
        }
        this.editText = editText;

        // set the label's size to match the input
        label.setTextSize(TypedValue.COMPLEX_UNIT_PX, editText.getTextSize());

        // listen to input and focus changes
        this.editText.addTextChangedListener(textWatcher);
        this.editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                label.setTextColor(focused ? accentColor : labelTextColor);
            }
        });

        // check if the label is set directly or grab it from editText's hint
        if (TextUtils.isEmpty(label.getText())) {
            label.setText(this.editText.getHint());
        }
        this.editText.setHint(null);

        // EditText has some padding built in to it's background, need to pad the label to align it.
        Rect editTextPadding = new Rect();
        editText.getBackground().getPadding(editTextPadding);
        label.setPaddingRelative(editTextPadding.left, 0, editTextPadding.right, 0);

        // add padding to the editText to leave room for the label
        LayoutParams newLp = new LayoutParams(lp);
        Paint paint = new Paint();
        paint.setTextSize(this.editText.getTextSize());
        newLp.topMargin = (int) -paint.ascent();

        return newLp;
    }

    private void updateLabel(boolean animate) {
        if (editText != null) {
            if (TextUtils.isEmpty(editText.getText())) {
                showHint(animate);
            } else {
                showFloatLabel(animate);
            }
        }
    }

    private void showHint(boolean animate) {
        if (currentMode != MODE_HINT) {
            float labelTop = editText.getBaseline();
            if (animate) {
                label.animate()
                        .translationY(labelTop)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(interp);
            } else {
                label.setTranslationY(labelTop);
                label.setScaleX(1f);
                label.setScaleY(1f);
            }
            currentMode = MODE_HINT;
        }
    }

    private void showFloatLabel(boolean animate) {
        if (currentMode != MODE_FLOAT_LABEL) {
            float scale = labelTextSize / editText.getTextSize();
            if (animate) {
                label.animate()
                        .translationY(0f)
                        .scaleX(scale)
                        .scaleY(scale)
                        .setDuration(ANIMATION_DURATION)
                        .setInterpolator(interp);
            } else {
                label.setTranslationY(0f);
                label.setScaleX(scale);
                label.setScaleY(scale);
            }
            currentMode = MODE_FLOAT_LABEL;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateLabel(false);
    }

    // Label mode
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_HINT, MODE_FLOAT_LABEL})
    public @interface LabelMode {
    }
}