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
    id("com.android.application")
    kotlin("android")
    id("kotlin-android-extensions")
    kotlin("kapt")
    id("io.fabric")
    id("com.google.gms.google-services")
}

apply(from = "$rootDir/dependencies.gradle.kts")

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

    dynamicFeatures = hashSetOf(
            ":about",
            ":designernews",
            ":dribbble",
            ":search"
    )

    dataBinding.isEnabled = true

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.appcompat:appcompat:${Versions.appcompat}")
    implementation("androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}")
    implementation("com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics}")
    implementation("com.github.bumptech.glide:glide:${Versions.glide}")
    implementation("com.github.bumptech.glide:recyclerview-integration:${Versions.glide}")
    implementation("com.google.firebase:firebase-core:${Versions.firebase}")

    kapt("com.google.dagger:dagger-compiler:${Versions.dagger}")
}

kapt.useBuildCache = true
