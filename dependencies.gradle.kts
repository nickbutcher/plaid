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
    implementation("com.google.android.material:material:${Versions.material}")
    implementation("com.google.dagger:dagger:${Versions.dagger}")
    implementation("androidx.arch.core:core-common:${Versions.androidxCoreRuntime}")
    implementation("androidx.arch.core:core-runtime:${Versions.androidxCoreRuntime}")
    implementation("androidx.core:core-ktx:${Versions.coreKtx}")
    implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
    implementation("com.squareup.retrofit2:converter-gson:${Versions.retrofit}")
    implementation("androidx.lifecycle:lifecycle-viewmodel:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")

    // Work around issue with runtime classpath version conflict
    implementation("androidx.collection:collection:${Versions.androidxCollection}")
    implementation("androidx.legacy:legacy-support-core-utils:${Versions.legacyCoreUtils}")
    implementation("androidx.lifecycle:lifecycle-runtime:${Versions.lifecycle}")

    testImplementation(project(":test_shared"))
    testImplementation("androidx.arch.core:core-testing:${Versions.androidxArch}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockito_kotlin}")
    testImplementation("com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}")
    testImplementation("junit:junit:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")

    // Workaround for dependency conflict during assembleAndroidTest
    androidTestImplementation("androidx.arch.core:core-runtime:2.0.1-alpha01")

    // Work around issue with runtime classpath version conflict
    androidTestImplementation("androidx.arch.core:core-testing:${Versions.androidxArch}")
    androidTestImplementation("androidx.legacy:legacy-support-core-utils:${Versions.legacyCoreUtils}")
    androidTestImplementation("androidx.lifecycle:lifecycle-runtime:${Versions.lifecycle}")

    androidTestImplementation(project(":test_shared"))
    androidTestImplementation("androidx.arch.core:core-testing:${Versions.androidxArch}")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:${Versions.espresso}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.espresso}")
    androidTestImplementation("androidx.test.ext:junit:${Versions.extJunit}")
    androidTestImplementation("androidx.test:rules:${Versions.test_rules}")
    androidTestImplementation("androidx.test:runner:${Versions.test_runner}")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:${Versions.ui_automator}")
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Versions.mockito_kotlin}")
    androidTestImplementation("com.squareup.retrofit2:retrofit-mock:${Versions.retrofit}")
    androidTestImplementation("org.mockito:mockito-android:${Versions.mockito}")
    androidTestImplementation("org.mockito:mockito-core:${Versions.mockito}")
    // Adding this to bring "google_play_services_version" into the test project
    // without this, it fails on AGP 3.6.x.
    androidTestImplementation("com.google.android.gms:play-services-gcm:16.0.0")
}
