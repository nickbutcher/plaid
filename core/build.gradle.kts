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

plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-android-extensions")
    kotlin("kapt")
}

apply(from = "$rootDir/dependencies.gradle.kts")

android {
    compileSdkVersion(Versions.compileSdk)

    defaultConfig {
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val designer_news_client_id: String by project
        val designer_news_client_secret: String by project
        val product_hunt_developer_token: String by project

        buildConfigField("String", "DESIGNER_NEWS_CLIENT_ID", "$designer_news_client_id")
        buildConfigField("String",
                "DESIGNER_NEWS_CLIENT_SECRET", "$designer_news_client_secret")

        buildConfigField("String",
                "PRODUCT_HUNT_DEVELOPER_TOKEN", "$product_hunt_developer_token")

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("dagger.gradle.incremental" to "true")
            }
        }
    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        exclude("META-INF/core_debug.kotlin_module")
    }

    dataBinding.isEnabled = true
}

dependencies {
    api(platform(project(":depconstraints")))
    kapt(platform(project(":depconstraints")))

    implementation(project(":bypass"))

    implementation("com.google.code.gson:gson")
    implementation("androidx.browser:browser")
    implementation("androidx.room:room-runtime")
    implementation("androidx.constraintlayout:constraintlayout")
    implementation("com.android.support:palette-v7")
    implementation("com.android.support:support-dynamic-animation")
    implementation("com.github.bumptech.glide:glide")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("org.jsoup:jsoup")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android")

    kapt("com.github.bumptech.glide:compiler")
    kapt("com.github.bumptech.glide:glide")
    kapt("com.google.dagger:dagger-compiler")
}

kapt.useBuildCache = true
kapt.arguments {
    mapOf(
            "dagger.formatGeneratedSource" to "disabled",
            "dagger.gradle.incremental" to "enabled"
    )
}

androidExtensions.isExperimental = true
