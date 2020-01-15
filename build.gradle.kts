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
    id("com.diffplug.gradle.spotless") version "3.26.1"
}

buildscript {

    repositories {
        addCommonRepositories()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:3.6.0-rc01")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.KOTLIN}")
        classpath("com.google.gms:google-services:4.3.0")
        classpath("io.fabric.tools:gradle:1.28.0")
    }
}

spotless {
    val ktlintVer by extra("0.36.0")

    kotlin {
        target("**/*.kt")
        ktlint(ktlintVer)
        licenseHeaderFile("$rootDir/scripts/copyright.txt", "(package |import |@file:|object |@Suppress)")
    }
    kotlinGradle {
        // same as kotlin, but for .gradle.kts files (defaults to '*.gradle.kts')
        target("**/*.gradle.kts")

        ktlint(ktlintVer)

        licenseHeaderFile("$rootDir/scripts/copyright.txt", "(plugins |import |include)")
    }
}

subprojects {
    repositories {
        addCommonRepositories()
    }
}
