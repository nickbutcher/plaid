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

package io.plaidapp.ui.transitions;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.transition.ArcMotion;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

/**
 * Helper class for setting up Fab <-> Dialog shared element transitions.
 */
public class FabDialogMorphSetup {

    public static final String EXTRA_SHARED_ELEMENT_START_COLOR =
            "EXTRA_SHARED_ELEMENT_START_COLOR";
    public static final String EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS =
            "EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS";

    private FabDialogMorphSetup() { }

    /**
     * Configure the shared element transitions for morphin from a fab <-> dialog. We need to do
     * this in code rather than declaratively as we need to supply the color to transition from/to
     * and the dialog corner radius which is dynamically supplied depending upon where this screen
     * is launched from.
     */
    public static void setupSharedEelementTransitions(@NonNull Activity activity,
                                                      @Nullable View target,
                                                      int dialogCornerRadius) {
        if (!activity.getIntent().hasExtra(EXTRA_SHARED_ELEMENT_START_COLOR)) return;

        int startCornerRadius = activity.getIntent().getIntExtra
                (EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS, -1);

        ArcMotion arcMotion = new ArcMotion();
        arcMotion.setMinimumHorizontalAngle(50f);
        arcMotion.setMinimumVerticalAngle(50f);
        int color = activity.getIntent().
                getIntExtra(EXTRA_SHARED_ELEMENT_START_COLOR, Color.TRANSPARENT);
        Interpolator easeInOut =
                AnimationUtils.loadInterpolator(activity, android.R.interpolator.fast_out_slow_in);
        MorphFabToDialog sharedEnter =
                new MorphFabToDialog(color, dialogCornerRadius, startCornerRadius);
        sharedEnter.setPathMotion(arcMotion);
        sharedEnter.setInterpolator(easeInOut);
        MorphDialogToFab sharedReturn = new MorphDialogToFab(color, startCornerRadius);
        sharedReturn.setPathMotion(arcMotion);
        sharedReturn.setInterpolator(easeInOut);
        if (target != null) {
            sharedEnter.addTarget(target);
            sharedReturn.addTarget(target);
        }
        activity.getWindow().setSharedElementEnterTransition(sharedEnter);
        activity.getWindow().setSharedElementReturnTransition(sharedReturn);
    }

}
