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

package io.plaidapp.designernews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Transition;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.plaidapp.core.util.ActivityHelper;
import io.plaidapp.core.designernews.data.poststory.PostStoryService;
import io.plaidapp.core.designernews.DesignerNewsPrefs;
import io.plaidapp.core.ui.transitions.FabTransform;
import io.plaidapp.core.ui.transitions.MorphTransform;
import io.plaidapp.core.ui.widget.BottomSheet;
import io.plaidapp.core.ui.widget.ObservableScrollView;
import io.plaidapp.core.util.Activities;
import io.plaidapp.core.util.AnimUtils;
import io.plaidapp.core.util.ImeUtils;
import io.plaidapp.core.util.ShortcutHelper;

public class PostNewDesignerNewsStory extends Activity {

    private BottomSheet bottomSheet;
    private ViewGroup bottomSheetContent;
    private TextView sheetTitle;
    private ObservableScrollView scrollContainer;
    private EditText title;
    private TextInputLayout urlLabel;
    private EditText url;
    private TextInputLayout commentLabel;
    private EditText comment;
    private Button post;
    private float appBarElevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_designer_news_story);
        bindResources();
        if (!FabTransform.setup(this, bottomSheetContent)) {
            MorphTransform.setup(this, bottomSheetContent,
                    ContextCompat.getColor(this, io.plaidapp.R.color.background_light), 0);
        }

        bottomSheet.registerCallback(new BottomSheet.Callbacks() {
            @Override
            public void onSheetDismissed() {
                // After a drag dismiss, finish without the shared element return transition as
                // it no longer makes sense.  Let the launching window know it's a drag dismiss so
                // that it can restore any UI used as an entering shared element
                setResult(Activities.DesignerNews.PostStory.RESULT_DRAG_DISMISSED);
                finish();
            }
        });

        scrollContainer.setListener(scrollY -> {
            if (scrollY != 0
                    && sheetTitle.getTranslationZ() != appBarElevation) {
                sheetTitle.animate()
                        .translationZ(appBarElevation)
                        .setStartDelay(0L)
                        .setDuration(80L)
                        .setInterpolator(AnimUtils.getFastOutSlowInInterpolator
                                (PostNewDesignerNewsStory.this))
                        .start();
            } else if (scrollY == 0 && sheetTitle.getTranslationZ() == appBarElevation) {
                sheetTitle.animate()
                        .translationZ(0f)
                        .setStartDelay(0L)
                        .setDuration(80L)
                        .setInterpolator(AnimUtils.getFastOutSlowInInterpolator
                                (PostNewDesignerNewsStory.this))
                        .start();
            }
        });

        // check for share intent
        if (isShareIntent()) {
            ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);
            url.setText(intentReader.getText());
            title.setText(intentReader.getSubject());
        }
        if (!hasSharedElementTransition()) {
            // when launched from share or app shortcut there is no shared element transition so
            // animate up the bottom sheet to establish the spatial model i.e. that it can be
            // dismissed downward
            overridePendingTransition(R.anim.post_story_enter, R.anim.post_story_exit);
            bottomSheetContent.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    bottomSheetContent.getViewTreeObserver().removeOnPreDrawListener(this);
                    bottomSheetContent.setTranslationY(bottomSheetContent.getHeight());
                    bottomSheetContent.animate()
                            .translationY(0f)
                            .setStartDelay(120L)
                            .setDuration(240L)
                            .setInterpolator(AnimUtils.getLinearOutSlowInInterpolator
                                    (PostNewDesignerNewsStory.this));
                    return false;
                }
            });
        }
        ShortcutHelper.reportPostUsed(this);
    }

    private void bindResources() {
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheet.setOnClickListener(view -> dismiss());
        bottomSheetContent = findViewById(R.id.bottom_sheet_content);
        sheetTitle = findViewById(R.id.title);
        scrollContainer = findViewById(R.id.scroll_container);
        title = findViewById(R.id.new_story_title);
        title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                titleTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        urlLabel = findViewById(R.id.new_story_url_label);
        url = findViewById(R.id.new_story_url);
        url.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                urlTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        url.setOnEditorActionListener(this::onEditorAction);
        commentLabel = findViewById(R.id.new_story_comment_label);
        comment = findViewById(R.id.new_story_comment);
        comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                commentTextChanged(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        comment.setOnEditorActionListener(this::onEditorAction);
        post = findViewById(R.id.new_story_post);
        post.setOnClickListener(view -> postNewStory());
        appBarElevation = getResources().getDimensionPixelSize(io.plaidapp.R.dimen.z_app_bar);
    }

    @Override
    protected void onPause() {
        // customize window animations
        overridePendingTransition(R.anim.post_story_enter, R.anim.post_story_exit);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    protected void dismiss() {
        if (!hasSharedElementTransition()) {
            bottomSheetContent.animate()
                    .translationY(bottomSheetContent.getHeight())
                    .setDuration(160L)
                    .setInterpolator(AnimUtils.getFastOutLinearInInterpolator
                            (PostNewDesignerNewsStory.this))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            finish();
                        }
                    });
        } else {
            finishAfterTransition();
        }
    }

    protected void titleTextChanged(CharSequence text) {
        setPostButtonState();
    }

    protected void urlTextChanged(CharSequence text) {
        final boolean emptyUrl = TextUtils.isEmpty(text);
        comment.setEnabled(emptyUrl);
        commentLabel.setEnabled(emptyUrl);
        comment.setFocusableInTouchMode(emptyUrl);
        setPostButtonState();
    }

    protected void commentTextChanged(CharSequence text) {
        final boolean emptyComment = TextUtils.isEmpty(text);
        url.setEnabled(emptyComment);
        urlLabel.setEnabled(emptyComment);
        url.setFocusableInTouchMode(emptyComment);
        setPostButtonState();
    }

    protected void postNewStory() {
        if (DesignerNewsPrefs.get(this).isLoggedIn()) {
            ImeUtils.hideIme(title);
            Intent postIntent = new Intent(PostStoryService.ACTION_POST_NEW_STORY, null,
                    this, PostStoryService.class);
            postIntent.putExtra(PostStoryService.EXTRA_STORY_TITLE, title.getText().toString());
            postIntent.putExtra(PostStoryService.EXTRA_STORY_URL, url.getText().toString());
            postIntent.putExtra(PostStoryService.EXTRA_STORY_COMMENT, comment.getText().toString());
            postIntent.putExtra(PostStoryService.EXTRA_BROADCAST_RESULT,
                    getIntent().getBooleanExtra(PostStoryService.EXTRA_BROADCAST_RESULT, false));
            startService(postIntent);
            setResult(Activities.DesignerNews.PostStory.RESULT_POSTING);
            finishAfterTransition();
        } else {
            Intent login = ActivityHelper.intentTo(Activities.DesignerNews.Login.INSTANCE);
            MorphTransform.addExtras(login, ContextCompat.getColor(this, io.plaidapp.R.color.designer_news), 0);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    this, post, getString(io.plaidapp.R.string.transition_designer_news_login));
            startActivity(login, options.toBundle());
        }
    }

    private boolean isShareIntent() {
        return getIntent() != null && Intent.ACTION_SEND.equals(getIntent().getAction());
    }

    private boolean hasSharedElementTransition() {
        Transition transition = getWindow().getSharedElementEnterTransition();
        return (transition != null && !transition.getTargets().isEmpty());
    }

    private void setPostButtonState() {
        post.setEnabled(!TextUtils.isEmpty(title.getText())
                       && (!TextUtils.isEmpty(url.getText())
                            || !TextUtils.isEmpty(comment.getText())));
    }

    protected boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            postNewStory();
            return true;
        }
        return false;
    }

}
