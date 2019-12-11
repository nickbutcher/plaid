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

buildscript {
    apply(from = "repositories.gradle", to = this)

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.0-alpha06")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}")
        classpath("com.google.gms:google-services:${Versions.googleServices}")
        classpath("io.fabric.tools:gradle:${Versions.fabric}")
    }
}

plugins {
    id("com.diffplug.gradle.spotless") version "3.26.1"
}

subprojects {
    buildscript {
        apply(from = rootProject.file("repositories.gradle"))
    }

    apply(plugin = "com.diffplug.gradle.spotless")
    spotless {
        kotlin {
            target("**/*.kt")
            ktlint(Versions.ktlint)
            licenseHeaderFile(project.rootProject.file("scripts/copyright.kt"))
        }
    }
}
