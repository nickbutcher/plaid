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
        api(Libs.AX_ANNOTATION + androidx)
        api(Libs.AX_APPCOMPAT + appcompat)
        api(Libs.AX_ARCH_CORE_RUNTIME + androidxCoreRuntime)
        api(Libs.AX_CORE_COMMON + androidxCoreRuntime)
        api(Libs.AX_CORE_KTX + coreKtx)
        api(Libs.AX_BROWSER + androidx)
        api(Libs.AX_COLLECTION + androidxCollection)
        api(Libs.AX_CONSTRAINT_LAYOUT + constraintLayout)
        api(Libs.AX_LEGACY_CORE + legacyCoreUtils)
        api(Libs.AX_LIFECYCLE_EXTENSION + lifecycle)
        api(Libs.AX_LIFECYCLE_LIVEDATA + lifecycle)
        api(Libs.AX_LIFECYCLE_RUNTIME + lifecycle)
        api(Libs.AX_LIFECYCLE_VM + lifecycle)
        api(Libs.AX_LIFECYCLE_VM_KTX + lifecycle)
        api(Libs.AX_ROOM_RUNTIME + room)
        api(Libs.AX_ROOM_KTX + room)
        api(Libs.AX_VIEWPAGER2 + viewPager2)
        api(Libs.COROUTINES_CORE + coroutines)
        api(Libs.COROUTINES_ANDROID + coroutines)
        api(Libs.CRASHLYTICS + crashlytics)
        api(Libs.DAGGER + dagger)
        api(Libs.DAGGER_COMPILER + dagger)
        api(Libs.GLIDE + glide)
        api(Libs.GLIDE_RECYCLERVIEW + glide)
        api(Libs.GLIDE_COMPILER + glide)
        api(Libs.GOOGLE_GSON + gson)
        api(Libs.FIREBASE_CORE + firebase)
        api(Libs.JSOUP + jsoup)
        api(Libs.KOTLIN_STDLIB + Versions.kotlin)
        api(Libs.KOTLIN_REFLECT + Versions.kotlin)
        api(Libs.MATERIAL + material)
        api(Libs.OKHTTP + okhttp)
        api(Libs.OKHTTP_LOGGING + okhttp)
        api(Libs.RETROFIT + retrofit)
        api(Libs.RETROFIT_CONVERTER_GSON + retrofit)
        api(Libs.SUPPORT_CUSTOMTABS + supportLibrary)
        api(Libs.SUPPORT_PALETTE + supportLibrary)
        api(Libs.SUPPORT_DYN_ANIMATION + supportLibrary)

        api(Libs.TEST_CORE + androidxArch)
        api(Libs.TEST_COROUTINES + coroutines)
        api(Libs.TEST_ESPRESSO_CONTRIB + espresso)
        api(Libs.TEST_ESPRESSO_CORE + espresso)
        api(Libs.TEST_JUNIT + junit)
        api(Libs.TEST_JUNIT_EXT + extJunit)
        api(Libs.TEST_MOCKITO_ANDROID + mockito)
        api(Libs.TEST_MOCKITO_CORE + mockito)
        api(Libs.TEST_MOCKITO_KT + mockito_kotlin)
        api(Libs.TEST_RETROFIT_MOCK + retrofit)
        api(Libs.TEST_RULES + test_rules)
        api(Libs.TEST_RUNNER + test_runner)
        api(Libs.TEST_UIAUTOMATOR + ui_automator)

        // Adding this to bring "google_play_services_version" into the test project
        // without this, it fails on AGP 3.6.x.
        api(Libs.GMS_PLAY_SERVICES_GCM + "16.0.0")
    }
}

publishing {
    publications {
        create<MavenPublication>("JavaPlatform") {
            from(components["javaPlatform"])
        }
    }
}
