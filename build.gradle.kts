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

buildscript {

    Config.run { repositories.deps() }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.gradle_plugin}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.google.gms:google-services:${Versions.googleServices}")
        classpath("io.fabric.tools:gradle:${Versions.fabric}")
    }

}

//plugins {
//    id 'com.diffplug.gradle.spotless' version '3.14.0'
//}
//

//subprojects {
//    buildscript {
//        apply from: rootProject.file('repositories.gradle')
//    }
//
//    apply plugin: 'com.diffplug.gradle.spotless'
//    spotless {
//        kotlin {
//            target '**/*.kt'
//            ktlint(versions.ktlint)
//            licenseHeaderFile project.rootProject.file('scripts/copyright.kt')
//        }
//    }
//}
subprojects {

//    repositories {
//        jcenter()
//    }
//    plugins {
//        kotlin("android")
//    }
//
//    dependencies {
//        implementation("com.google.android.material:material:${Versions.material}")
//        implementation("com.google.dagger:dagger:${Versions.dagger}")
//        implementation("androidx.core:core-ktx:${Versions.coreKtx}")
//        implementation("com.squareup.retrofit2:retrofit:${Versions.retrofit}")
//        implementation("com.squareup.retrofit2:converter-gson:${Versions.retrofit}")
//        implementation("androidx.lifecycle:lifecycle-viewmodel:${Versions.lifecycle}")
//        implementation("androidx.lifecycle:lifecycle-extensions:${Versions.lifecycle}")
//        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}")
//    }
//
//    tasks {
//        compileKotlin {
//            kotlinOptions {
//                allWarningsAsErrors = true
//            }
//        }
//    }
    apply(plugin = "dependencies")
}