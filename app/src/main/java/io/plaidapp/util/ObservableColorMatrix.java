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

package io.plaidapp.util;

import android.graphics.ColorMatrix;
import android.util.Property;

/**
 * An extension to {@link ColorMatrix} which caches the saturation value for animation purposes.
 */
public class ObservableColorMatrix extends ColorMatrix {

    private float saturation = 1f;

    public ObservableColorMatrix() {
        super();
    }

    float getSaturation() {
        return saturation;
    }

    @Override
    public void setSaturation(float saturation) {
        this.saturation = saturation;
        super.setSaturation(saturation);
    }

    public static final Property<ObservableColorMatrix, Float> SATURATION =
            AnimUtils.createFloatProperty(
                    new AnimUtils.FloatProp<ObservableColorMatrix>("saturation") {
                        @Override
                        public float get(ObservableColorMatrix ocm) {
                            return ocm.getSaturation();
                        }

                        @Override
                        public void set(ObservableColorMatrix ocm, float saturation) {
                            ocm.setSaturation(saturation);
                        }
                    });

}
