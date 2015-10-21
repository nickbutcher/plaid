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

package in.uncod.android.bypass.style;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

/**
 * A simple text span used to mark text that will be replaced by an image once it has been
 * downloaded. See {@link in.uncod.android.bypass.Bypass.LoadImageCallback}
 */
public class ImageLoadingSpan extends CharacterStyle {
    @Override
    public void updateDrawState(TextPaint textPaint) {
        // no-op
    }
}
