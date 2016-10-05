
package io.plaidapp.data.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A {@see RequestInterceptor} that adds an auth token to requests
 */
public class MaterialUpAuthInterceptor implements Interceptor {

    private final String accessToken;

    public MaterialUpAuthInterceptor(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request().newBuilder()
                .addHeader("Authorization",  "Token token="+accessToken)
                .build();
        return chain.proceed(request);
    }
}
