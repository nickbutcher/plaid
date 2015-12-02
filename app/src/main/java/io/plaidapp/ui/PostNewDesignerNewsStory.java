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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;
import io.plaidapp.R;
import io.plaidapp.data.api.designernews.PostStoryService;
import io.plaidapp.data.prefs.DesignerNewsPrefs;
import io.plaidapp.ui.transitions.FabDialogMorphSetup;
import io.plaidapp.ui.widget.BottomSheet;
import io.plaidapp.ui.widget.ObservableScrollView;
import io.plaidapp.util.ImeUtils;

public class PostNewDesignerNewsStory extends Activity {

    public static final int RESULT_DRAG_DISMISSED = 3;
    public static final int RESULT_POSTING = 4;

    @Bind(R.id.bottom_sheet) BottomSheet bottomSheet;
    @Bind(R.id.bottom_sheet_content) ViewGroup bottomSheetContent;
    @Bind(R.id.title) TextView sheetTitle;
    @Bind(R.id.scroll_container) ObservableScrollView scrollContainer;
    @Bind(R.id.new_story_title) EditText title;
    @Bind(R.id.new_story_url_label) TextInputLayout urlLabel;
    @Bind(R.id.new_story_url) EditText url;
    @Bind(R.id.new_story_comment_label) TextInputLayout commentLabel;
    @Bind(R.id.new_story_comment) EditText comment;
    @Bind(R.id.new_story_post) Button post;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_new_designer_news_story);
        ButterKnife.bind(this);
        FabDialogMorphSetup.setupSharedEelementTransitions(this, bottomSheetContent, 0);

        bottomSheet.addListener(new BottomSheet.Listener() {
            @Override
            public void onDragDismissed() {
                // After a drag dismiss, finish without the shared element return transition as
                // it no longer makes sense.  Let the launching window know it's a drag dismiss so
                // that it can restore any UI used as an entering shared element
                setResult(RESULT_DRAG_DISMISSED);
                finish();
            }

            @Override
            public void onDrag(int top) { /* no-op */ }
        });

        scrollContainer.setListener(new ObservableScrollView.OnScrollListener() {
            @Override
            public void onScrolled(int scrollY) {
                if (scrollY != 0
                        && sheetTitle.getTranslationZ() != appBarElevation) {
                    sheetTitle.animate()
                            .translationZ(appBarElevation)
                            .setStartDelay(0L)
                            .setDuration(80L)
                            .setInterpolator(AnimationUtils.loadInterpolator
                                    (PostNewDesignerNewsStory.this, android.R.interpolator
                                            .fast_out_slow_in))
                            .start();
                } else if (scrollY == 0 && sheetTitle.getTranslationZ() == appBarElevation) {
                    sheetTitle.animate()
                            .translationZ(0f)
                            .setStartDelay(0L)
                            .setDuration(80L)
                            .setInterpolator(AnimationUtils.loadInterpolator
                                    (PostNewDesignerNewsStory.this,
                                            android.R.interpolator.fast_out_slow_in))
                            .start();
                }
            }
        });

        // check for share intent
        if (isShareIntent()) {
            ShareCompat.IntentReader intentReader = ShareCompat.IntentReader.from(this);
            url.setText(intentReader.getText());
            title.setText(intentReader.getSubject());

            // when receiving a share there is no shared element transition so animate up the
            // bottom sheet to establish the spatial model i.e. that it can be dismissed downward
            overridePendingTransition(R.anim.post_story_enter, R.anim.fade_out_rapidly);
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
                            .setInterpolator(AnimationUtils.loadInterpolator(
                                    PostNewDesignerNewsStory.this,
                                    android.R.interpolator.linear_out_slow_in));
                    return false;
                }
            });
        }
    }

    @Override
    protected void onPause() {
        // customize window animations
        overridePendingTransition(0, R.anim.fade_out_rapidly);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (isShareIntent()) {
            bottomSheetContent.animate()
                    .translationY(bottomSheetContent.getHeight())
                    .setDuration(160L)
                    .setInterpolator(AnimationUtils.loadInterpolator(
                            PostNewDesignerNewsStory.this,
                            android.R.interpolator.fast_out_linear_in))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            finishAfterTransition();
                        }
                    });
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.bottom_sheet)
    protected void dismiss() {
        finishAfterTransition();
    }

    @OnTextChanged(R.id.new_story_title)
    protected void titleTextChanged(CharSequence text) {
        setPostButtonState();
    }

    @OnTextChanged(R.id.new_story_url)
    protected void urlTextChanged(CharSequence text) {
        final boolean emptyUrl = TextUtils.isEmpty(text);
        comment.setEnabled(emptyUrl);
        commentLabel.setEnabled(emptyUrl);
        comment.setFocusableInTouchMode(emptyUrl);
        setPostButtonState();
    }

    @OnTextChanged(R.id.new_story_comment)
    protected void commentTextChanged(CharSequence text) {
        final boolean emptyComment = TextUtils.isEmpty(text);
        url.setEnabled(emptyComment);
        urlLabel.setEnabled(emptyComment);
        url.setFocusableInTouchMode(emptyComment);
        setPostButtonState();
    }

    @OnClick(R.id.new_story_post)
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
            setResult(RESULT_POSTING);
            finishAfterTransition();
        } else {
            Intent login = new Intent(this, DesignerNewsLogin.class);
            login.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_COLOR,
                    ContextCompat.getColor(this, R.color.designer_news));
            login.putExtra(FabDialogMorphSetup.EXTRA_SHARED_ELEMENT_START_CORNER_RADIUS, 0);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    this, post, getString(R.string.transition_designer_news_login));
            startActivity(login, options.toBundle());
        }
    }

    private boolean isShareIntent() {
        return getIntent() != null && Intent.ACTION_SEND.equals(getIntent().getAction());
    }

    private void setPostButtonState() {
        post.setEnabled(!TextUtils.isEmpty(title.getText())
                       && (!TextUtils.isEmpty(url.getText())
                            || !TextUtils.isEmpty(comment.getText())));
    }

    @OnEditorAction({ R.id.new_story_url, R.id.new_story_comment })
    protected boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            postNewStory();
            return true;
        }
        return false;
    }

}
