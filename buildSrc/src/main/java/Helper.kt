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

import java.io.ByteArrayOutputStream
import org.gradle.api.Project

fun Project.runCmd(vararg command: String) = try {
    val stdOut = ByteArrayOutputStream()
    exec {
        commandLine(*command)
        standardOutput = stdOut
    }
    stdOut.toString(Charsets.UTF_8.name()).trim()
} catch (e: Throwable) {
    e.printStackTrace()
    null
}
