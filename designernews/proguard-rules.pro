##
## Copyright 2016 Google Inc.
##
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
##
##      http://www.apache.org/licenses/LICENSE-2.0
##
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.
##

# For stack traces
-keepattributes SourceFile, LineNumberTable

# Get rid of package names, makes file smaller
-repackageclasses

# Required for Retrofit/OkHttp
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keepattributes *Annotation*, Signature, Exceptions

# This optimization conflicts with how Retrofit uses proxy objects without concrete implementations
-optimizations !method/removal/parameter

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Required for GSON parsing of data coming from network requests to models
-keep class io.plaidapp.data.api.dribbble.model.** { *; }
-keep class io.plaidapp.data.api.designernews.model.**  { *; }
-keep class io.plaidapp.data.api.producthunt.model.**  { *; }

# Required for classes created and used from JNI code (on C/C++ side)
-keep, includedescriptorclasses class in.uncod.android.bypass.Document { *; }
-keep, includedescriptorclasses class in.uncod.android.bypass.Element { *; }

# A resource is loaded with a relative path so the package of this class must be preserved.
-keepnames class org.jsoup.nodes.Entities


