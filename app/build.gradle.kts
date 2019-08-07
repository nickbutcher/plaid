/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jetbrains.kotlin.kapt3.base.Kapt.kapt

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    kotlin("kapt")
    id("io.fabric")
    id("com.google.gms.google-services")
}

android {
    compileSdkVersion(Versions.compileSdk)

    defaultConfig {
        applicationId = Names.applicationId
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = 100 + (runCmd("git", "rev-list", "--count", "HEAD")?.toIntOrNull() ?: 1)
        versionName = "1.1.0"

        setProperty("archivesBaseName", "plaid")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resConfig("en")
        manifestPlaceholders = mapOf(
                "crashlyticsEnabled" to false
        )

        javaCompileOptions {
            annotationProcessorOptions {
                arguments = mapOf("dagger.gradle.incremental" to "true")
            }
        }
    }

//    dataBinding {
//        enabled true
//    }
//
    buildTypes {
        getByName("release") {
            // There's a Dex Splitter issue when enabling DataBinding & proguard in dynamic features
            // The temporary workaround is to disable shrinking
            isMinifyEnabled = false
            manifestPlaceholders = mapOf(
                    "crashlyticsEnabled" to true
            )

            proguardFiles(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
            )

        }
        getByName("debug") {
            (this as ExtensionAware).extra["alwaysUpdateBuildId"] = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    dynamicFeatures = mutableSetOf(
            ":about",
            ":designernews",
            ":dribbble",
            ":search"
    )
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}")
    implementation("com.google.firebase:firebase-core:${Versions.firebase}")
    implementation("com.github.bumptech.glide:glide:${Versions.glide}")
    implementation("com.github.bumptech.glide:recyclerview-integration:${Versions.glide}")

    kapt("com.google.dagger:dagger-compiler:${Versions.dagger}")
}

kapt {
    useBuildCache = true
}

//// Must be applied after dependencies. See https://stackoverflow.com/a/38018985
//plugins {
//    id("com.google.gms.google-services")
//}