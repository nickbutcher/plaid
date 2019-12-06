/*
 *   Copyright 2019 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
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

        buildConfigField("String", "DESIGNER_NEWS_CLIENT_ID", "${designer_news_client_id}")
        buildConfigField("String",
                "DESIGNER_NEWS_CLIENT_SECRET", "${designer_news_client_secret}")

        buildConfigField("String",
                "PRODUCT_HUNT_DEVELOPER_TOKEN", "${product_hunt_developer_token}")

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

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(project(":bypass"))

    implementation("com.google.code.gson:gson:${Versions.gson}")
    implementation("androidx.browser:browser:${Versions.androidx}")
    implementation("androidx.room:room-runtime:${Versions.room}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}")
    implementation("com.android.support:palette-v7:${Versions.supportLibrary}")
    implementation("com.android.support:support-dynamic-animation:${Versions.supportLibrary}")
    implementation("com.github.bumptech.glide:glide:${Versions.glide}")
    implementation("com.squareup.okhttp3:okhttp:${Versions.okhttp}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}")
    implementation("org.jsoup:jsoup:${Versions.jsoup}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    kapt("com.github.bumptech.glide:compiler:${Versions.glide}")
    kapt("com.google.dagger:dagger-compiler:${Versions.dagger}")
}

kapt.useBuildCache = true
kapt.arguments {
    mapOf(
            "dagger.formatGeneratedSource" to "disabled",
            "dagger.gradle.incremental" to "enabled"
    )
}

androidExtensions.isExperimental = true