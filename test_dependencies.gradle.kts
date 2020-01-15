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

    testImplementation(project(":test_shared"))
    testImplementation(Libs.TEST_CORE)
    testImplementation(Libs.TEST_MOCKITO_KT)
    testImplementation(Libs.TEST_RETROFIT_MOCK)
    testImplementation(Libs.TEST_JUNIT)
    testImplementation(Libs.TEST_MOCKITO_CORE)
    testImplementation(Libs.TEST_COROUTINES)

    // Work around issue with runtime classpath version conflict
    implementation(Libs.AX_CORE_COMMON)
    implementation(Libs.AX_ARCH_CORE_RUNTIME)
    implementation(Libs.AX_COLLECTION)
    implementation(Libs.AX_LEGACY_CORE)
    implementation(Libs.AX_LIFECYCLE_RUNTIME)

    // Work around issue with runtime classpath version conflict
    androidTestImplementation(Libs.TEST_CORE)
    androidTestImplementation(Libs.AX_LEGACY_CORE)
    androidTestImplementation(Libs.AX_LIFECYCLE_RUNTIME)

    androidTestImplementation(project(":test_shared"))
    androidTestImplementation(Libs.TEST_CORE)
    androidTestImplementation(Libs.TEST_AX_ESPRESSO_CONTRIB)
    androidTestImplementation(Libs.TEST_AX_ESPRESSO_CORE)
    androidTestImplementation(Libs.TEST_AX_JUNIT_EXT)
    androidTestImplementation(Libs.TEST_AX_RULES)
    androidTestImplementation(Libs.TEST_AX_RUNNER)
    androidTestImplementation(Libs.TEST_AX_UIAUTOMATOR)
    androidTestImplementation(Libs.TEST_MOCKITO_KT)
    androidTestImplementation(Libs.TEST_RETROFIT_MOCK)
    androidTestImplementation(Libs.TEST_MOCKITO_ANDROID)
    androidTestImplementation(Libs.TEST_MOCKITO_CORE)
}
