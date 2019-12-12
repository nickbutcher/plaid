/*
 * Copyright 2015 Google LLC.
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

plugins {
    id( "com.android.application")
    id( "io.fabric")
    id( "kotlin-android")
    id( "kotlin-android-extensions")
    id( "kotlin-kapt")
    id("base")
}

apply(from = "../shared_dependencies.gradle.kts")
apply(from = "../test_dependencies.gradle.kts")

android {
    compileSdkVersion(Versions.compileSdk)

    defaultConfig {
        applicationId = Names.applicationId
        minSdkVersion(Versions.minSdk)
        targetSdkVersion(Versions.targetSdk)
        versionCode = getCommitCount()
        versionName = "1.1.0"
        base.archivesBaseName = "plaid"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resConfig("en")
        manifestPlaceholders["crashlyticsEnabled"] = false
    }

    dataBinding {
        isEnabled = true
    }

    buildTypes {
        getByName("release") {
            // There"s a Dex Splitter issue when enabling DataBinding & proguard in dynamic features
            // The temporary workaround is to disable shrinking
            isMinifyEnabled= false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro")
            manifestPlaceholders["crashlyticsEnabled"] = true
        }
        getByName("debug") {
            (this as ExtensionAware).extra["alwaysUpdateBuildId"] = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    dynamicFeatures = mutableSetOf(":about", ":designernews", ":dribbble", ":search")
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

// query git for the commit count to automate versioning.
fun getCommitCount() = 100 +
        Integer.parseInt(Runtime.getRuntime().exec("git rev-list --count HEAD", emptyArray(), rootProject.rootDir).run {
            inputStream.bufferedReader().use { it.readText() }
        }.trim())

// Must be applied after dependencies. See https://stackoverflow.com/a/38018985
apply(plugin = "com.google.gms.google-services")
