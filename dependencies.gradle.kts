/*
 * Copyright 2019 Google LLC.
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

// this will try to take configuration from existing ones
val implementation by configurations
val testImplementation by configurations
val androidTestImplementation by configurations
val compile by configurations
val api by configurations
dependencies {
    api(platform(project(":depconstraints")))

    implementation("com.google.android.material:material")
    implementation("com.google.dagger:dagger")
    implementation("androidx.arch.core:core-common")
    implementation("androidx.arch.core:core-runtime")
    implementation("androidx.core:core-ktx")
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-gson")
    implementation("androidx.lifecycle:lifecycle-viewmodel")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx")
    implementation("androidx.lifecycle:lifecycle-extensions")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx")

    // Work around issue with runtime classpath version conflict
    implementation("androidx.collection:collection")
    implementation("androidx.legacy:legacy-support-core-utils")
    implementation("androidx.lifecycle:lifecycle-runtime")

    testImplementation(project(":test_shared"))
    testImplementation("androidx.arch.core:core-testing")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
    testImplementation("com.squareup.retrofit2:retrofit-mock")
    testImplementation("junit:junit")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")

    // Workaround for dependency conflict during assembleAndroidTest
    androidTestImplementation("androidx.arch.core:core-runtime:2.0.1-alpha01")

    // Work around issue with runtime classpath version conflict
    androidTestImplementation("androidx.arch.core:core-testing")
    androidTestImplementation("androidx.legacy:legacy-support-core-utils")
    androidTestImplementation("androidx.lifecycle:lifecycle-runtime:")

    androidTestImplementation(project(":test_shared"))
    androidTestImplementation("androidx.arch.core:core-testing")
    androidTestImplementation("androidx.test.espresso:espresso-contrib")
    androidTestImplementation("androidx.test.espresso:espresso-core")
    androidTestImplementation("androidx.test.ext:junit")
    androidTestImplementation("androidx.test:rules")
    androidTestImplementation("androidx.test:runner")
    androidTestImplementation("androidx.test.uiautomator:uiautomator")
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin")
    androidTestImplementation("com.squareup.retrofit2:retrofit-mock")
    androidTestImplementation("org.mockito:mockito-android")
    androidTestImplementation("org.mockito:mockito-core")
    // Adding this to bring "google_play_services_version" into the test project
    // without this, it fails on AGP 3.6.x.
    androidTestImplementation("com.google.android.gms:play-services-gcm:16.0.0")
}
