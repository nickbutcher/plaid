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

package io.plaidapp.designernews.ui.login;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import io.plaidapp.core.data.Result;
import io.plaidapp.core.designernews.ui.login.LoginUiModel;
import io.plaidapp.core.designernews.ui.login.LoginViewModel;
import io.plaidapp.core.ui.transitions.FabTransform;
import io.plaidapp.core.ui.transitions.MorphTransform;
import io.plaidapp.core.util.ScrimUtil;
import io.plaidapp.core.util.glide.GlideApp;
import io.plaidapp.designernews.InjectionKt;
import io.plaidapp.designernews.R;
import io.plaidapp.designernews.ui.ViewModelFactory;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class DesignerNewsLogin extends AppCompatActivity {

    private ViewGroup container;
    private TextView title;
    private TextInputLayout usernameLabel;
    private EditText username;
    private TextInputLayout passwordLabel;
    private EditText password;
    private FrameLayout actionsContainer;
    private Button signup;
    private Button login;
    private ProgressBar loading;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer_news_login);

        ViewModelFactory factory = InjectionKt.provideViewModelFactory(this);
        viewModel = ViewModelProviders.of(this, factory).get(LoginViewModel.class);

        viewModel.getUiState().observe(this, loginUiModelResult -> {
            if (loginUiModelResult instanceof Result.Loading) {
                showLoading();
            } else if (loginUiModelResult instanceof Result.Error) {
                showLoginFailed();
            } else if (loginUiModelResult instanceof Result.Success) {
                updateUiWithUser(((Result.Success<LoginUiModel>) loginUiModelResult).getData());
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        bindViews();
        if (!FabTransform.setup(this, container)) {
            MorphTransform.setup(this, container,
                    ContextCompat.getColor(this, io.plaidapp.R.color.background_light),
                    getResources().getDimensionPixelSize(io.plaidapp.R.dimen.dialog_corners));
        }

        loading.setVisibility(View.GONE);
        username.addTextChangedListener(loginFieldWatcher);
        // the primer checkbox messes with focus order so force it
        username.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                password.requestFocus();
                return true;
            }
            return false;
        });
        password.addTextChangedListener(loginFieldWatcher);
        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && isLoginValid()) {
                login.performClick();
                return true;
            }
            return false;
        });
    }

    private void bindViews() {
        container = findViewById(R.id.container);
        title = findViewById(R.id.dialog_title);
        usernameLabel = findViewById(R.id.username_float_label);
        username = findViewById(R.id.username);
        passwordLabel = findViewById(R.id.password_float_label);
        password = findViewById(R.id.password);
        actionsContainer = findViewById(R.id.actions_container);
        signup = findViewById(R.id.signup);
        login = findViewById(R.id.login);
        loading = findViewById(io.plaidapp.R.id.loading);
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    public void doLogin(View view) {
        viewModel.login(username.getText().toString(), password.getText().toString());
    }

    public void signup(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.designernews.co/users/new")));
    }

    public void dismiss(View view) {
        setResult(Activity.RESULT_CANCELED);
        finishAfterTransition();
    }

    boolean isLoginValid() {
        return username.length() > 0 && password.length() > 0;
    }

    private void updateUiWithUser(LoginUiModel uiModel) {
        final Toast confirmLogin = new Toast(getApplicationContext());
        final View v = LayoutInflater.from(DesignerNewsLogin.this)
                .inflate(io.plaidapp.R.layout.toast_logged_in_confirmation, null, false);
        ((TextView) v.findViewById(io.plaidapp.R.id.name)).setText(uiModel.getDisplayName());
        // need to use app context here as the activity will be destroyed shortly
        if (uiModel.getPortraitUrl() != null) {
            GlideApp.with(getApplicationContext())
                    .load(uiModel.getPortraitUrl())
                    .placeholder(io.plaidapp.R.drawable.avatar_placeholder)
                    .circleCrop()
                    .transition(withCrossFade())
                    .into((ImageView) v.findViewById(io.plaidapp.R.id.avatar));
        }
        v.findViewById(io.plaidapp.R.id.scrim).setBackground(ScrimUtil
                .makeCubicGradientScrimDrawable(
                        ContextCompat.getColor(DesignerNewsLogin.this,
                                io.plaidapp.R.color.scrim),
                        5, Gravity.BOTTOM));
        confirmLogin.setView(v);
        confirmLogin.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
        confirmLogin.setDuration(Toast.LENGTH_LONG);
        confirmLogin.show();
    }

    void showLoginFailed() {
        Snackbar.make(container, io.plaidapp.R.string.login_failed, Snackbar.LENGTH_SHORT).show();
        showLogin();
        password.requestFocus();
    }

    private TextWatcher loginFieldWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            login.setEnabled(isLoginValid());
        }
    };

    private void showLoading() {
        TransitionManager.beginDelayedTransition(container);
        title.setVisibility(View.GONE);
        usernameLabel.setVisibility(View.GONE);
        passwordLabel.setVisibility(View.GONE);
        actionsContainer.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void showLogin() {
        TransitionManager.beginDelayedTransition(container);
        title.setVisibility(View.VISIBLE);
        usernameLabel.setVisibility(View.VISIBLE);
        passwordLabel.setVisibility(View.VISIBLE);
        actionsContainer.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }
}
