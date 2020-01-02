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

package io.plaidapp.core.data.api

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import javax.inject.Inject
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit

/**
 * A [retrofit2.Converter.Factory] which removes unwanted wrapping envelopes from API responses.
 */
class DeEnvelopingConverter @Inject constructor(internal val gson: Gson) : Converter.Factory() {

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, Any>? {

        // This converter requires an annotation providing the name of the payload in the envelope;
        // if one is not supplied then return null to continue down the converter chain.
        val payloadName = getPayloadName(annotations) ?: return null

        val adapter: TypeAdapter<*> = gson.getAdapter(TypeToken.get(type))
        return Converter { body: ResponseBody ->
            gson.newJsonReader(body.charStream()).use { jsonReader ->
                jsonReader.beginObject()
                while (jsonReader.hasNext()) {
                    if (payloadName == jsonReader.nextName()) {
                        return@use adapter.read(jsonReader)
                    } else {
                        jsonReader.skipValue()
                    }
                }
                return@use null
            }
        }
    }

    private fun getPayloadName(annotations: Array<Annotation>?): String? {
        val annotation = annotations?.firstOrNull { it is EnvelopePayload }
        return if (annotation != null) {
            (annotation as EnvelopePayload).value
        } else {
            null
        }
    }
}
