#
# Copyright 2016 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

-dontobfuscate #STOPSHIP remove this
-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn rx.**

-keep class io.plaidapp.data.api.dribbble.model.**  { *; }
-keep class io.plaidapp.data.api.designernews.model.**  { *; }
-keep class io.plaidapp.data.api.producthunt.model.**  { *; }
-keep class io.plaidapp.ui.transitions.**  { *; }

-keep class android.support.v7.widget.LinearLayoutManager {
    public protected *;
}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-keep class in.uncod.android.bypass.** { *; }
-keep class retrofit2.** { *; }

-keepattributes *Annotation*,Signature,Exceptions

-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}
