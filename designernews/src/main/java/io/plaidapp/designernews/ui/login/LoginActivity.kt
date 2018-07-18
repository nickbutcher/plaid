/*
 * Copyright 2018 Google, Inc.
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
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import io.plaidapp.core.data.Result
import io.plaidapp.core.ui.transitions.FabTransform
import io.plaidapp.core.ui.transitions.MorphTransform
import io.plaidapp.core.util.ScrimUtil
import io.plaidapp.core.util.doAfterTextChanged
import io.plaidapp.core.util.exhaustive
import io.plaidapp.core.util.glide.GlideApp
import io.plaidapp.designernews.R
import io.plaidapp.designernews.provideViewModelFactory
import io.plaidapp.R as appR

class LoginActivity : AppCompatActivity() {

    private var container: ViewGroup? = null
    private var title: TextView? = null
    private var usernameLabel: TextInputLayout? = null
    private var username: EditText? = null
    private var passwordLabel: TextInputLayout? = null
    private var password: EditText? = null
    private var actionsContainer: FrameLayout? = null
    private var signup: Button? = null
    private var login: Button? = null
    private var loading: ProgressBar? = null

    private lateinit var viewModel: LoginViewModel

    internal val isLoginValid: Boolean
        get() = username?.length() != 0 && password?.length() != 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_designer_news_login)

        val factory = provideViewModelFactory(this)
        viewModel = ViewModelProviders.of(this, factory).get(LoginViewModel::class.java)

        viewModel.uiState.observe(this, Observer<Result<LoginUiModel>?> { loginUiModelResult ->
            when (loginUiModelResult) {
                is Result.Loading -> showLoading()
                is Result.Error -> showLoginFailed()
                is Result.Success<*> -> {
                    updateUiWithUser((loginUiModelResult as Result.Success<LoginUiModel>).data)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                null -> {
                    throw NullPointerException("LoginUiModel should not be null")
                }
            }.exhaustive
        })

        bindViews()
        if (!FabTransform.setup(this, container)) {
            MorphTransform.setup(
                this,
                container,
                ContextCompat.getColor(this, appR.color.background_light),
                resources.getDimensionPixelSize(appR.dimen.dialog_corners)
            )
        }

        loading?.visibility = View.GONE
        username?.doAfterTextChanged {
            enableLoginButtonIfLoginIsValid()
        }
        password?.apply {
            doAfterTextChanged {
                enableLoginButtonIfLoginIsValid()
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE && isLoginValid) {
                    login?.performClick()
                }
                false
            }
        }
    }

    private fun enableLoginButtonIfLoginIsValid() {
        login?.isEnabled = isLoginValid
    }

    private fun bindViews() {
        container = findViewById(R.id.container)
        title = findViewById(R.id.dialog_title)
        usernameLabel = findViewById(R.id.username_float_label)
        username = findViewById(R.id.username)
        passwordLabel = findViewById(R.id.password_float_label)
        password = findViewById(R.id.password)
        actionsContainer = findViewById(R.id.actions_container)
        signup = findViewById(R.id.signup)
        login = findViewById(R.id.login)
        loading = findViewById(appR.id.loading)
    }

    override fun onBackPressed() {
        dismiss(null)
    }

    fun doLogin(view: View) {
        viewModel.login(username!!.text.toString(), password!!.text.toString())
    }

    fun signup(view: View) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.designernews.co/users/new")
            )
        )
    }

    fun dismiss(view: View?) {
        setResult(Activity.RESULT_CANCELED)
        finishAfterTransition()
    }

    private fun updateUiWithUser(uiModel: LoginUiModel) {
        val v = LayoutInflater.from(this@LoginActivity)
            .inflate(appR.layout.toast_logged_in_confirmation, null, false)
        (v.findViewById<View>(appR.id.name) as TextView).text = uiModel.displayName
        // need to use app context here as the activity will be destroyed shortly
        if (uiModel.portraitUrl != null) {
            GlideApp.with(applicationContext)
                .load(uiModel.portraitUrl)
                .placeholder(appR.drawable.avatar_placeholder)
                .circleCrop()
                .transition(withCrossFade())
                .into(v.findViewById<View>(appR.id.avatar) as ImageView)
        }
        v.findViewById<View>(appR.id.scrim).background =
            ScrimUtil.makeCubicGradientScrimDrawable(
                ContextCompat.getColor(this@LoginActivity, appR.color.scrim),
                5, Gravity.BOTTOM
            )
        Toast(applicationContext).apply {
            view = v
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            duration = Toast.LENGTH_LONG
        }.show()
    }

    private fun showLoginFailed() {
        container?.let {
            Snackbar.make(it, appR.string.login_failed, Snackbar.LENGTH_SHORT).show()
        }
        showLogin()
        password?.requestFocus()
    }

    private fun showLoading() {
        TransitionManager.beginDelayedTransition(container)
        title?.visibility = View.GONE
        usernameLabel?.visibility = View.GONE
        passwordLabel?.visibility = View.GONE
        actionsContainer?.visibility = View.GONE
        loading?.visibility = View.VISIBLE
    }

    private fun showLogin() {
        TransitionManager.beginDelayedTransition(container)
        title?.visibility = View.VISIBLE
        usernameLabel?.visibility = View.VISIBLE
        passwordLabel?.visibility = View.VISIBLE
        actionsContainer?.visibility = View.VISIBLE
        loading?.visibility = View.GONE
    }
}
