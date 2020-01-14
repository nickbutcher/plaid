/*
 * Copyright 2020 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.kotlin.dsl.dependencies

val implementation by configurations
val testImplementation by configurations
val androidTestImplementation by configurations
val compile by configurations
val api by configurations

dependencies {
    api(platform(project(":depconstraints")))

    implementation(Libs.MATERIAL)
    implementation(Libs.DAGGER)
    implementation(Libs.AX_CORE_KTX)
    implementation(Libs.RETROFIT)
    implementation(Libs.RETROFIT_CONVERTER_GSON)
    implementation(Libs.AX_LIFECYCLE_VM)
    implementation(Libs.AX_LIFECYCLE_LIVEDATA)
    implementation(Libs.AX_LIFECYCLE_EXTENSION)
    implementation(Libs.AX_LIFECYCLE_VM_KTX)
}
