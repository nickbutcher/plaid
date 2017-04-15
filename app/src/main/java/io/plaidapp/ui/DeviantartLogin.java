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

package io.plaidapp.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.plaidapp.BuildConfig;
import io.plaidapp.R;
import io.plaidapp.data.api.deviantart.DeviantartAuthService;
import io.plaidapp.data.api.deviantart.model.AccessToken;
import io.plaidapp.data.api.deviantart.model.User;
import io.plaidapp.data.prefs.DeviantartPrefs;
import io.plaidapp.ui.transitions.FabTransform;
import io.plaidapp.ui.transitions.MorphTransform;
import io.plaidapp.util.ScrimUtil;
import io.plaidapp.util.glide.CircleTransform;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeviantartLogin extends Activity {

    private static final String STATE_LOGIN_FAILED = "loginFailed";

    boolean isDismissing = false;
    boolean isLoginFailed = false;
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.login_message) TextView message;
    @BindView(R.id.login) Button login;
    @BindView(R.id.loading) ProgressBar loading;
    @BindView(R.id.login_failed_message) TextView loginFailed;
    DeviantartPrefs deviantartPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deviantart_login);
        ButterKnife.bind(this);
        loading.setVisibility(View.GONE);
        deviantartPrefs = DeviantartPrefs.get(this);

        if (!FabTransform.setup(this, container)) {
            MorphTransform.setup(this, container,
                    ContextCompat.getColor(this, R.color.background_light),
                    getResources().getDimensionPixelSize(R.dimen.dialog_corners));
        }

        if (savedInstanceState != null) {
            isLoginFailed = savedInstanceState.getBoolean(STATE_LOGIN_FAILED, false);
            loginFailed.setVisibility(isLoginFailed ? View.VISIBLE : View.GONE);
        }

        checkAuthCallback(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkAuthCallback(intent);
    }

    public void doLogin(View view) {
        showLoading();
        deviantartPrefs.login(DeviantartLogin.this);
    }

    public void dismiss(View view) {
        isDismissing = true;
        setResult(Activity.RESULT_CANCELED);
        finishAfterTransition();
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(STATE_LOGIN_FAILED, isLoginFailed);
    }

    void showLoginFailed() {
        isLoginFailed = true;
        showLogin();
        loginFailed.setVisibility(View.VISIBLE);
    }

    void showLoggedInUser() {
        final Call<User> authenticatedUser = deviantartPrefs.getApi().getAuthenticatedUser();
        authenticatedUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
//                Log.d("user", user.toString());
                deviantartPrefs.setLoggedInUser(user);
                final Toast confirmLogin = new Toast(getApplicationContext());
                final View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout
                        .toast_deviantart_loggied_in, null, false);
                ((TextView) v.findViewById(R.id.name)).setText(user.username.toLowerCase());
                // need to use app context here as the activity will be destroyed shortly
                Glide.with(getApplicationContext())
                        .load(user.usericon)
                        .placeholder(R.drawable.ic_player)
                        .transform(new CircleTransform(getApplicationContext()))
                        .into((ImageView) v.findViewById(R.id.avatar));
                v.findViewById(R.id.scrim).setBackground(ScrimUtil.makeCubicGradientScrimDrawable
                        (ContextCompat.getColor(DeviantartLogin.this, R.color.scrim),
                                5, Gravity.BOTTOM));
                confirmLogin.setView(v);
                confirmLogin.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                confirmLogin.setDuration(Toast.LENGTH_LONG);
                confirmLogin.show();
            }

            @Override public void onFailure(Call<User> call, Throwable t) { }
        });
    }

    private void showLoading() {
        TransitionManager.beginDelayedTransition(container);
        message.setVisibility(View.GONE);
        login.setVisibility(View.GONE);
        loginFailed.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void showLogin() {
        TransitionManager.beginDelayedTransition(container);
        message.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void checkAuthCallback(Intent intent) {
        if (intent != null
                && intent.getData() != null
                && !TextUtils.isEmpty(intent.getData().getAuthority())
                && DeviantartPrefs.LOGIN_CALLBACK.equals(intent.getData().getAuthority())) {
            showLoading();
            getAccessToken(intent.getData().getQueryParameter("code"));
        }
    }

    private void getAccessToken(String code) {
        final DeviantartAuthService deviantartAuthApi = new Retrofit.Builder()
                .baseUrl(DeviantartAuthService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((DeviantartAuthService.class));

        final Call<AccessToken> accessTokenCall =
                deviantartAuthApi.getAccessToken("authorization_code", 6019,
                BuildConfig.DEVIANTART_CLIENT_SECRET,
                "foshizzle://deviantart-auth-callback",
                code);

        Log.d("getToken", "clientid: "+BuildConfig.DEVIANTART_CLIENT_ID+" secret: "+ BuildConfig.DEVIANTART_CLIENT_SECRET+
                " authCode: "+code+" redirect: "+"foshizzle://deviantart-auth-callback");

    //https://www.deviantart.com/oauth2/token?grant_type=authorization_code&client_id=6019&client_secret=eb01c03aab183d488810a5d748e975d5&redirect_uri=foshizzle://deviantart-auth-callback&code=d38b58d5294d54d82eb87f54180addb9a9dfd0c4

        accessTokenCall.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (response.body() == null) {
                    showLoginFailed();
                    Log.d("responseBody", "null");
                    return;
                }
                Log.d("responseBody", response.body().toString());
                isLoginFailed = false;
                deviantartPrefs.setAccessToken(response.body().access_token);
                showLoggedInUser();
                setResult(Activity.RESULT_OK);
                finishAfterTransition();
            }

            @Override
            public void onFailure(Call<AccessToken> call, Throwable t) {
                Log.e(getClass().getCanonicalName(), t.getMessage(), t);
                showLoginFailed();
            }
        });
    }
}
