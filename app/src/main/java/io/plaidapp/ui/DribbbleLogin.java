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
import io.plaidapp.data.api.dribbble.DribbbleAuthService;
import io.plaidapp.data.api.dribbble.model.AccessToken;
import io.plaidapp.data.api.dribbble.model.User;
import io.plaidapp.data.prefs.DribbblePrefs;
import io.plaidapp.ui.transitions.FabTransform;
import io.plaidapp.ui.transitions.MorphTransform;
import io.plaidapp.util.ScrimUtil;
import io.plaidapp.util.glide.CircleTransform;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DribbbleLogin extends Activity {

    private static final String STATE_LOGIN_FAILED = "loginFailed";

    boolean isDismissing = false;
    boolean isLoginFailed = false;
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.login_message) TextView message;
    @BindView(R.id.login) Button login;
    @BindView(R.id.loading) ProgressBar loading;
    @BindView(R.id.login_failed_message) TextView loginFailed;
    DribbblePrefs dribbblePrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dribbble_login);
        ButterKnife.bind(this);
        loading.setVisibility(View.GONE);
        dribbblePrefs = DribbblePrefs.get(this);

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
        dribbblePrefs.login(DribbbleLogin.this);
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
        final Call<User> authenticatedUser = dribbblePrefs.getApi().getAuthenticatedUser();
        authenticatedUser.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                final User user = response.body();
                dribbblePrefs.setLoggedInUser(user);
                final Toast confirmLogin = new Toast(getApplicationContext());
                final View v = LayoutInflater.from(DribbbleLogin.this).inflate(R.layout
                        .toast_logged_in_confirmation, null, false);
                ((TextView) v.findViewById(R.id.name)).setText(user.name.toLowerCase());
                // need to use app context here as the activity will be destroyed shortly
                Glide.with(getApplicationContext())
                        .load(user.avatar_url)
                        .placeholder(R.drawable.ic_player)
                        .transform(new CircleTransform(getApplicationContext()))
                        .into((ImageView) v.findViewById(R.id.avatar));
                v.findViewById(R.id.scrim).setBackground(ScrimUtil.makeCubicGradientScrimDrawable
                        (ContextCompat.getColor(DribbbleLogin.this, R.color.scrim),
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
                && DribbblePrefs.LOGIN_CALLBACK.equals(intent.getData().getAuthority())) {
            showLoading();
            getAccessToken(intent.getData().getQueryParameter("code"));
        }
    }

    private void getAccessToken(String code) {
        final DribbbleAuthService dribbbleAuthApi = new Retrofit.Builder()
                .baseUrl(DribbbleAuthService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create((DribbbleAuthService.class));

        final Call<AccessToken> accessTokenCall = dribbbleAuthApi.getAccessToken(BuildConfig
                        .DRIBBBLE_CLIENT_ID,
                BuildConfig.DRIBBBLE_CLIENT_SECRET,
                code);
        accessTokenCall.enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(Call<AccessToken> call, Response<AccessToken> response) {
                if (response.body() == null) {
                    showLoginFailed();
                    return;
                }
                isLoginFailed = false;
                dribbblePrefs.setAccessToken(response.body().access_token);
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
