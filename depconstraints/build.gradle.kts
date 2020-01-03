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

plugins {
    id("java-platform")
    id("maven-publish")
}

val appcompat = "1.1.0"
val androidx = "1.0.0"
val androidxCollection = "1.0.0"
val androidxCoreRuntime = "2.1.0"
val androidxArch = "2.0.0"
val constraintLayout = "2.0.0-beta3"
val coreKtx = "1.1.0"
val coroutines = "1.3.2"
val crashlytics = "2.10.1"
val dagger = "2.23.2"
val espresso = "3.1.0-beta02"
val extJunit = "1.1.0"
val fabric = "1.28.0"
val firebase = "17.2.1"
val glide = "4.9.0"
val googleServices = "4.3.0"
val gson = "2.8.5"
val jsoup = "1.11.3"
val junit = "4.12"
val legacyCoreUtils = "1.0.0"
val lifecycle = "2.2.0-rc03"
val material = "1.1.0-alpha05"
val mockito = "2.23.0"
val mockito_kotlin = "2.0.0-RC3"
val okhttp = "4.2.2"
val retrofit = "2.6.0"
val room = "2.2.2"
val supportLibrary = "28.0.0"
val test_rules = "1.1.0-beta02"
val test_runner = "1.1.0-beta02"
val ui_automator = "2.2.0-beta02"
val viewPager2 = "1.0.0-beta02"

dependencies {
    constraints {
        api("androidx.annotation:annotation:$androidx")
        api("androidx.appcompat:appcompat:$appcompat")
        api("androidx.arch.core:core-runtime:$androidxCoreRuntime")
        api("androidx.browser:browser:$androidx")
        api("androidx.collection:collection:$androidxCollection")
        api("androidx.constraintlayout:constraintlayout:$constraintLayout")
        api("androidx.legacy:legacy-support-core-utils:$legacyCoreUtils")
        api("androidx.lifecycle:lifecycle-extensions:$lifecycle")
        api("androidx.lifecycle:lifecycle-runtime:$lifecycle")
        api("androidx.room:room-runtime:$room")
        api("androidx.room:room-ktx:$room")
        api("androidx.viewpager2:viewpager2:$viewPager2")
        api("com.android.support:customtabs:$supportLibrary")
        api("com.android.support:palette-v7:$supportLibrary")
        api("com.android.support:support-dynamic-animation:$supportLibrary")
        api("com.crashlytics.sdk.android:crashlytics:$crashlytics")
        api("com.github.bumptech.glide:glide:$glide")
        api("com.github.bumptech.glide:recyclerview-integration:$glide")
        api("com.google.code.gson:gson:$gson")
        api("com.google.firebase:firebase-core:$firebase")
        api("com.squareup.okhttp3:okhttp:$okhttp")
        api("com.squareup.okhttp3:logging-interceptor:$okhttp")
        api("junit:junit:$junit")
        api("org.jsoup:jsoup:$jsoup")
        api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}")
        api("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines")

        api("com.google.android.material:material:$material")
        api("com.google.dagger:dagger:$dagger")
        api("androidx.arch.core:core-common:$androidxCoreRuntime")
        api("androidx.arch.core:core-runtime:$androidxCoreRuntime")
        api("androidx.core:core-ktx:$coreKtx")
        api("com.squareup.retrofit2:retrofit:$retrofit")
        api("com.squareup.retrofit2:converter-gson:$retrofit")
        api("androidx.lifecycle:lifecycle-viewmodel:$lifecycle")
        api("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle")
        api("androidx.lifecycle:lifecycle-extensions:$lifecycle")
        api("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle")

        api("androidx.arch.core:core-testing:$androidxArch")
        api("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockito_kotlin")
        api("com.squareup.retrofit2:retrofit-mock:$retrofit")
        api("junit:junit:$junit")
        api("org.mockito:mockito-core:$mockito")
        api("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines")

        api("androidx.arch.core:core-testing:$androidxArch")
        api("androidx.legacy:legacy-support-core-utils:$legacyCoreUtils")
        api("androidx.lifecycle:lifecycle-runtime:$lifecycle")

        api("androidx.arch.core:core-testing:$androidxArch")
        api("androidx.test.espresso:espresso-contrib:$espresso")
        api("androidx.test.espresso:espresso-core:$espresso")
        api("androidx.test.ext:junit:$extJunit")
        api("androidx.test:rules:$test_rules")
        api("androidx.test:runner:$test_runner")
        api("androidx.test.uiautomator:uiautomator:$ui_automator")
        api("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockito_kotlin")
        api("com.squareup.retrofit2:retrofit-mock:$retrofit")
        api("org.mockito:mockito-android:$mockito")
        api("org.mockito:mockito-core:$mockito")

        // Adding this to bring "google_play_services_version" into the test project
        // without this, it fails on AGP 3.6.x.
        api("com.google.android.gms:play-services-gcm:16.0.0")
    }
}

publishing {
    publications {
        create<MavenPublication>("JavaPlatform") {
            from(components["javaPlatform"])
        }
    }
}
