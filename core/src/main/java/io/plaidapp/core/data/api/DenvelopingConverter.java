/*
 *   Copyright 2018 Google LLC
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

package io.plaidapp.core.data.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * A {@link retrofit2.Converter.Factory} which removes unwanted wrapping envelopes from API
 * responses.
 */
public class DenvelopingConverter extends Converter.Factory {

    final Gson gson;

    public DenvelopingConverter(@NonNull Gson gson) {
        this.gson = gson;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {

        // This converter requires an annotation providing the name of the payload in the envelope;
        // if one is not supplied then return null to continue down the converter chain.
        final String payloadName = getPayloadName(annotations);
        if (payloadName == null) return null;

        final TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return (Converter<ResponseBody, Object>) body -> {
            try (JsonReader jsonReader = gson.newJsonReader(body.charStream())) {
                jsonReader.beginObject();
                while (jsonReader.hasNext()) {
                    if (payloadName.equals(jsonReader.nextName())) {
                        return adapter.read(jsonReader);
                    } else {
                        jsonReader.skipValue();
                    }
                }
                return null;
            } finally {
                body.close();
            }
        };
    }

    private @Nullable String getPayloadName(Annotation[] annotations) {
        if (annotations == null) return null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof EnvelopePayload) {
                return ((EnvelopePayload) annotation).value();
            }
        }
        return null;
    }
}
