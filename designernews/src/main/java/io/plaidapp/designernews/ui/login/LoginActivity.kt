/*
 * Copyright 2018 Google LLC.
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

package io.plaidapp.designernews.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import io.plaidapp.R as appR
import io.plaidapp.core.R as coreR
import io.plaidapp.core.ui.transitions.FabTransform
import io.plaidapp.core.ui.transitions.MorphTransform
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.delegates.contentView
import io.plaidapp.core.util.doAfterTextChanged
import io.plaidapp.core.util.event.EventObserver
import io.plaidapp.designernews.R
import io.plaidapp.designernews.dagger.inject
import io.plaidapp.designernews.databinding.ActivityDesignerNewsLoginBinding
import io.plaidapp.designernews.databinding.ToastLoggedInConfirmationBinding
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    private val binding by contentView<LoginActivity, ActivityDesignerNewsLoginBinding>(
        R.layout.activity_designer_news_login
    )

    @Inject
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        inject(this)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.uiState.observe(this, Observer {
            val uiModel = it ?: return@Observer

            if (uiModel.showProgress) {
                beginDelayedTransition()
            }

            if (uiModel.showError != null && !uiModel.showError.consumed) {
                uiModel.showError.consume()?.let { showLoginFailed(it) }
            }
            if (uiModel.showSuccess != null && !uiModel.showSuccess.consumed) {
                uiModel.showSuccess.consume()?.let { updateUiWithUser(it) }
                setResult(Activity.RESULT_OK)
                finish()
            }
        })

        viewModel.openUrl.observe(this, EventObserver { openUrl(it) })

        if (!FabTransform.setup(this, binding.container)) {
            MorphTransform.setup(
                this,
                binding.container,
                ColorUtils.getThemeColor(this, coreR.attr.colorSurface),
                resources.getDimensionPixelSize(appR.dimen.dialog_corners)
            )
        }

        binding.username.doAfterTextChanged {
            viewModel.loginDataChanged(it.toString(), binding.password.text.toString())
        }
        binding.password.apply {
            doAfterTextChanged {
                viewModel.loginDataChanged(binding.username.text.toString(), it.toString())
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.login(
                        binding.username.text.toString(),
                        binding.password.text.toString()
                    )
                }
                false
            }
        }
        binding.login.setOnClickListener {
            viewModel.login(binding.username.text.toString(), binding.password.text.toString())
        }
        binding.frame.setOnClickListener {
            dismiss()
        }
    }

    override fun onBackPressed() {
        dismiss()
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    private fun dismiss() {
        setResult(Activity.RESULT_CANCELED)
        finishAfterTransition()
    }

    private fun updateUiWithUser(model: LoginResultUiModel) {
        val toastBinding =
            ToastLoggedInConfirmationBinding.inflate(LayoutInflater.from(this@LoginActivity))
                .apply {
                    uiModel = model
                }

        // need to use app context here as the activity will be destroyed shortly
        Toast(applicationContext).apply {
            view = toastBinding.root
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            duration = Toast.LENGTH_LONG
        }.show()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Snackbar.make(binding.container, errorString, Snackbar.LENGTH_SHORT).show()
        beginDelayedTransition()
        binding.password.requestFocus()
    }

    private fun beginDelayedTransition() {
        TransitionManager.beginDelayedTransition(binding.container)
    }
}
