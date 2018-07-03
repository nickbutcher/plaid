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

package io.plaidapp.ui.transitions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Keep;
import android.transition.TransitionValues;
import android.transition.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.plaidapp.R;
import io.plaidapp.core.util.TransitionUtils;

/**
 * An alternative to {@link android.transition.Slide} which staggers elements by <b>distance</b>
 * rather than using start delays. That is elements start from/end at a progressively increasing
 * displacement such that they come together/move apart over the same duration as they enter/exit.
 * This can produce more cohesive choreography. The displacement factor can be controlled by the
 * {@code spread} attribute.
 * <p>
 * Currently only supports entering/exiting from the bottom edge.
 */
public class StaggeredDistanceSlide extends Visibility {

    private static final String PROPNAME_SCREEN_LOCATION = "android:visibility:screenLocation";

    private int spread = 1;

    public StaggeredDistanceSlide() {
        super();
    }

    @Keep
    public StaggeredDistanceSlide(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.StaggeredDistanceSlide);
        spread = a.getInteger(R.styleable.StaggeredDistanceSlide_spread, spread);
        a.recycle();
    }

    public int getSpread() {
        return spread;
    }

    public void setSpread(int spread) {
        this.spread = spread;
    }

    @Override
    public Animator onAppear(ViewGroup sceneRoot, View view,
                             TransitionValues startValues, TransitionValues endValues) {
        int[] position = (int[]) endValues.values.get(PROPNAME_SCREEN_LOCATION);
        return createAnimator(view, sceneRoot.getHeight() + (position[1] * spread), 0f);
    }

    @Override
    public Animator onDisappear(ViewGroup sceneRoot, View view,
                                TransitionValues startValues, TransitionValues endValues) {
        int[] position = (int[]) endValues.values.get(PROPNAME_SCREEN_LOCATION);
        return createAnimator(view, 0f, sceneRoot.getHeight() + (position[1] * spread));
    }

    private Animator createAnimator(
            final View view, float startTranslationY, float endTranslationY) {
        view.setTranslationY(startTranslationY);
        final List<Boolean> ancestralClipping = TransitionUtils.setAncestralClipping(view, false);
        Animator transition = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, endTranslationY);
        transition.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                TransitionUtils.restoreAncestralClipping(view, ancestralClipping);
            }
        });
        return transition;
    }
}
