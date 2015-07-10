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

package com.example.android.plaid.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.plaid.R;
import com.example.android.plaid.data.api.AuthInterceptor;
import com.example.android.plaid.data.api.designernews.DesignerNewsService;
import com.example.android.plaid.data.api.designernews.model.AccessToken;
import com.example.android.plaid.data.prefs.DesignerNewsPrefs;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DesignerNewsLogin extends Activity {

    boolean isDismissing = false;
    private ViewGroup container;

    SharedElementCallback sharedElementEnterCallback = new SharedElementCallback() {
        @Override
        public View onCreateSnapshotView(Context context, Parcelable snapshot) {
            // grab the saved fab snapshot and pass it to the below via a View
            View view = new View(context);
            view.setBackground(new BitmapDrawable(context.getResources(), (Bitmap) snapshot));
            return view;
        }

        @Override
        public void onSharedElementStart(List<String> sharedElementNames,
                                         List<View> sharedElements,
                                         List<View> sharedElementSnapshots) {
            // grab the fab snapshot and fade it out/in (depending on if we are entering or exiting)
            for (int i = 0; i < sharedElements.size(); i++) {
                if (sharedElements.get(i) == container) {
                    View snapshot = sharedElementSnapshots.get(i);
                    BitmapDrawable fabSnapshot = (BitmapDrawable) snapshot.getBackground();
                    fabSnapshot.setBounds(0, 0, snapshot.getWidth(), snapshot.getHeight());
                    container.getOverlay().clear();
                    container.getOverlay().add(fabSnapshot);
                    if (!isDismissing) {
                        // fab -> login: fade out the fab snapshot
                        ObjectAnimator.ofInt(fabSnapshot, "alpha", 0).setDuration(100).start();
                    } else {
                        // login -> fab: fade in the fab snapshot toward the end of the transition
                        fabSnapshot.setAlpha(0);
                        ObjectAnimator fadeIn = ObjectAnimator.ofInt(fabSnapshot, "alpha", 255)
                                .setDuration(150);
                        fadeIn.setStartDelay(150);
                        fadeIn.start();
                    }
                    forceSharedElementLayout();
                    break;
                }
            }
        }
    };
    private TextView message;
    private Button login;
    private ProgressBar loading;
    private DesignerNewsPrefs designerNewsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_designer_news_login);
        setEnterSharedElementCallback(sharedElementEnterCallback);

        container = (ViewGroup) findViewById(R.id.container);
        message = (TextView) findViewById(R.id.login_message);
        login = (Button) findViewById(R.id.login);
        loading = (ProgressBar) findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        designerNewsPrefs = new DesignerNewsPrefs(getApplicationContext());
    }

    public void doLogin(View view) {
        showLoading();
        getAccessToken();
    }

    private void showLoading() {
        TransitionManager.beginDelayedTransition(container);
        message.setVisibility(View.GONE);
        login.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void showLogin() {
        TransitionManager.beginDelayedTransition(container);
        message.setVisibility(View.VISIBLE);
        login.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    private void getAccessToken() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .build();

        DesignerNewsService designerNewsService = restAdapter.create((DesignerNewsService.class));

        designerNewsService.login("nickbutcher@gmail.com", "yourpasswordhere", "", new
                Callback<AccessToken>() {
                    @Override
                    public void success(AccessToken accessToken, Response response) {
                        designerNewsPrefs.setAccessToken(accessToken.access_token);
                        // TODO show a proper success
                        Toast.makeText(getApplicationContext(), "Logged in to designer news", Toast
                                .LENGTH_LONG).show();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(getClass().getCanonicalName(), error.getMessage(), error);
                        Toast.makeText(getApplicationContext(), "Log in failed: " + error
                                .getResponse()
                                .getStatus(), Toast.LENGTH_LONG).show();
                        DesignerNewsLogin.this.finishAfterTransition();
                    }
                });
    }

    private void showLoggedInUser() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(DesignerNewsService.ENDPOINT)
                .setRequestInterceptor(new AuthInterceptor(designerNewsPrefs.getAccessToken()))
                .build();

        DesignerNewsService designerNewsService = restAdapter.create((DesignerNewsService.class));
//        designerNewsService.getAuthenticatedUser(new Callback<User>() {
//            @Override
//            public void success(User user, Response response) {
//                designerNewsPrefs.setLoggedInUser(user);
//                Toast confirmLogin = new Toast(getApplicationContext());
//                View v = LayoutInflater.from(DesignerNewsLogin.this).inflate(R.layout
// .toast_dribbble_logged_in, null, false);
//                ((TextView) v.findViewById(R.id.name)).setText(user.name);
//                // need to use app context here as the activity will be destroyed shortly
//                Glide.with(getApplicationContext())
//                        .load(user.avatar_url)
//                        .transform(new CircleTransform(getApplicationContext()))
//                        .into((ImageView) v.findViewById(R.id.avatar));
//                v.findViewById(R.id.scrim).setBackground(ScrimUtil
// .makeCubicGradientScrimDrawable(getColor(R.color.scrim), 5, Gravity.BOTTOM));
//                confirmLogin.setView(v);
//                confirmLogin.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
//                confirmLogin.setDuration(Toast.LENGTH_LONG);
//                confirmLogin.show();
//            }
//
//            @Override
//            public void failure(RetrofitError error) {
//            }
//        });
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

    private void forceSharedElementLayout() {
        int widthSpec = View.MeasureSpec.makeMeasureSpec(container.getWidth(),
                View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(container.getHeight(),
                View.MeasureSpec.EXACTLY);
        container.measure(widthSpec, heightSpec);
        container.layout(container.getLeft(), container.getTop(), container.getRight(), container
                .getBottom());
    }
}
