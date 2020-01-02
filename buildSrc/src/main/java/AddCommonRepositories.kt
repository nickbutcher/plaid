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

import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.kotlin.dsl.maven

fun RepositoryHandler.addCommonRepositories() {
    google().content {
        includeGroupByRegex("com.android.*")
        includeGroupByRegex("androidx.*")

        includeGroup("androidx.lifecycle")
        includeGroup("androidx.core")
        includeGroup("com.google.firebase")
        includeGroup("com.google.android.gms")
        includeGroup("com.google.android.material")
        includeGroup("com.google.gms")

        includeGroup("zipflinger")
    }

    jcenter().content {
        includeGroupByRegex("com.google.*")
        includeGroupByRegex("com.sun.*")
        includeGroupByRegex("com.squareup.*")
        includeGroupByRegex("com.jakewharton.*")
        includeGroupByRegex("com.googlecode.*")
        includeGroupByRegex("org.jetbrains.*")
        includeGroupByRegex("org.codehaus.*")
        includeGroupByRegex("org.apache.*")
        includeGroupByRegex("net.sf.*")
        includeGroupByRegex("javax.*")
        includeGroupByRegex("org.ow2.*")

        includeGroup("com.github.bumptech.glide")
        includeGroup("com.ibm.icu")
        includeGroup("com.nhaarman.mockitokotlin2")
        includeGroup("commons-io")
        includeGroup("commons-codec")
        includeGroup("commons-logging")
        includeGroup("it.unimi.dsi")
        includeGroup("junit")
        includeGroup("me.eugeniomarletti.kotlin.metadata")
        includeGroup("net.bytebuddy")
        includeGroup("net.java")
        includeGroup("org.abego.treelayout")
        includeGroup("org.antlr")
        includeGroup("org.bouncycastle")
        includeGroup("org.checkerframework")
        includeGroup("org.glassfish")
        includeGroup("org.glassfish.jaxb")
        includeGroup("org.hamcrest")
        includeGroup("org.jvnet.staxex")
        includeGroup("org.jsoup")
        includeGroup("org.mockito")
        includeGroup("org.objenesis")
        includeGroup("org.sonatype.oss")
        includeGroup("org.xerial")
        includeGroup("net.ltgt.gradle.incap")

        includeGroup("de.undercouch")
        includeGroup("org.jdom")

        excludeGroup("com.google.firebase")
        excludeGroup("com.google.android.gms")
        excludeGroup("com.google.android.material")
    }

    maven("https://dl.bintray.com/kotlin/kotlin-eap/").content {
        includeGroupByRegex("org.jetbrains.*")
    }

    maven("https://maven.fabric.io/public").content {
        includeGroupByRegex("io.fabric.*")
        includeGroupByRegex("com.crashlytics.*")
    }
}
