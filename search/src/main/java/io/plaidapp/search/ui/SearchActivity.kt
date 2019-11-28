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

package io.plaidapp.search.ui

import android.app.SearchManager
import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.text.style.StyleSpan
import android.transition.Transition
import android.transition.TransitionInflater
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.ViewStub
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckedTextView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.TransitionRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.set
import androidx.core.text.toSpannable
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import io.plaidapp.core.dagger.qualifier.IsPocketInstalled
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.feed.FeedAdapter
import io.plaidapp.core.ui.expandPopularItems
import io.plaidapp.core.ui.recyclerview.InfiniteScrollListener
import io.plaidapp.core.ui.recyclerview.SlideInItemAnimator
import io.plaidapp.core.util.Activities
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.ImeUtils
import io.plaidapp.core.util.ShortcutHelper
import io.plaidapp.core.util.TransitionUtils
import io.plaidapp.search.R
import io.plaidapp.search.dagger.Injector
import io.plaidapp.search.ui.transitions.CircularReveal
import javax.inject.Inject

/**
 * Allows the user to input a search term and searches in Dribbbble and Designer News for posts
 * matching it.
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var searchBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var progress: ProgressBar
    private lateinit var results: RecyclerView
    private lateinit var container: ViewGroup
    private lateinit var resultsContainer: ViewGroup
    private lateinit var fab: ImageButton
    private lateinit var confirmSaveContainer: ViewGroup
    private lateinit var saveDribbble: CheckedTextView
    private lateinit var saveDesignerNews: CheckedTextView
    private lateinit var saveConfirmed: Button
    private lateinit var scrim: View
    private lateinit var resultsScrim: View
    private var columns: Int = 0
    private var noResults: TextView? = null
    private val transitions = SparseArray<Transition>()
    private var focusQuery = true

    private lateinit var feedAdapter: FeedAdapter

    @Inject
    lateinit var viewModel: SearchViewModel

    @JvmField
    @field:[Inject IsPocketInstalled]
    var pocketInstalled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        bindResources()
        setupSearchView()
        setupInsets()

        Injector.inject(this)

        feedAdapter = FeedAdapter(this, columns, pocketInstalled, ColorUtils.isDarkTheme(this))

        viewModel.searchResults.observe(this, Observer { searchUiModel ->
            if (searchUiModel.items.isNotEmpty()) {
                if (results.visibility != View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(
                        container,
                        getTransition(R.transition.search_show_results)
                    )
                    progress.visibility = View.GONE
                    results.visibility = View.VISIBLE
                    fab.visibility = View.VISIBLE
                }
                val items = searchUiModel.items
                expandPopularItems(items, columns)
                feedAdapter.items = items
            } else {
                TransitionManager.beginDelayedTransition(
                    container, getTransition(io.plaidapp.core.R.transition.auto)
                )
                progress.visibility = View.GONE
                setNoResultsVisibility(View.VISIBLE)
            }
        })

        viewModel.searchProgress.observe(this, Observer { progress ->
            if (progress.isLoading) {
                feedAdapter.dataStartedLoading()
            } else {
                feedAdapter.dataFinishedLoading()
            }
        })

        val shotPreloadSizeProvider = ViewPreloadSizeProvider<Shot>()

        setExitSharedElementCallback(FeedAdapter.createSharedElementReenterCallback(this))
        val layoutManager = GridLayoutManager(this, columns)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return feedAdapter.getItemColumnSpan(position)
            }
        }
        val shotPreloader = RecyclerViewPreloader(this, feedAdapter, shotPreloadSizeProvider, 4)

        results.apply {
            this.adapter = feedAdapter
            itemAnimator = SlideInItemAnimator()
            this.layoutManager = layoutManager
            addOnScrollListener(object :
                InfiniteScrollListener(layoutManager) {
                override fun onLoadMore() {
                    viewModel.loadMore()
                }

                override fun isDataLoading(): Boolean {
                    return viewModel.searchProgress.value?.isLoading ?: false
                }
            })
            setHasFixedSize(true)

            addOnScrollListener(shotPreloader)
        }
        setupTransitions()
        onNewIntent(intent)
        ShortcutHelper.reportSearchUsed(this)
    }

    private fun bindResources() {
        searchBack = findViewById(R.id.searchback)
        searchBack.setOnClickListener { dismiss() }
        searchView = findViewById(R.id.search_view)
        progress = findViewById(android.R.id.empty)
        results = findViewById(R.id.search_results)
        container = findViewById(R.id.container)
        resultsContainer = findViewById(R.id.results_container)
        fab = findViewById(R.id.fab)
        fab.setOnClickListener { save() }
        confirmSaveContainer = findViewById(R.id.confirm_save_container)
        val toggleSave = { view: View -> toggleSaveCheck(view as CheckedTextView) }
        saveDribbble = findViewById(R.id.save_dribbble)
        saveDribbble.setOnClickListener(toggleSave)
        saveDesignerNews = findViewById(R.id.save_designer_news)
        saveDesignerNews.setOnClickListener(toggleSave)
        saveConfirmed = findViewById(R.id.save_confirmed)
        saveConfirmed.setOnClickListener { doSave() }
        scrim = findViewById(R.id.scrim)
        scrim.setOnClickListener { dismiss() }
        resultsScrim = findViewById(R.id.results_scrim)
        resultsScrim.setOnClickListener { hideSaveConfirmation() }
        columns = resources.getInteger(io.plaidapp.core.R.integer.num_columns)
    }

    private fun setupInsets() {
        container.apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            val stableFabMarginBottom = fab.marginBottom
            setOnApplyWindowInsetsListener { _, insets ->
                updatePadding(
                    top = insets.systemWindowInsetTop,
                    left = insets.systemWindowInsetLeft,
                    right = insets.systemWindowInsetRight
                )
                results.updatePadding(bottom = insets.systemWindowInsetBottom)
                fab.updateLayoutParams<MarginLayoutParams> {
                    bottomMargin = stableFabMarginBottom + insets.systemWindowInsetBottom
                }
                confirmSaveContainer.updateLayoutParams<MarginLayoutParams> {
                    bottomMargin = stableFabMarginBottom + insets.systemWindowInsetBottom
                }
                insets
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasExtra(SearchManager.QUERY)) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            if (!TextUtils.isEmpty(query)) {
                searchView.setQuery(query, false)
                searchFor(query)
            }
        }
    }

    override fun onBackPressed() {
        if (confirmSaveContainer.visibility == View.VISIBLE) {
            hideSaveConfirmation()
        } else {
            dismiss()
        }
    }

    override fun onPause() {
        // needed to suppress the default window animation when closing the activity
        overridePendingTransition(0, 0)
        super.onPause()
    }

    override fun onEnterAnimationComplete() {
        if (focusQuery) {
            // focus the search view once the enter transition finishes
            searchView.requestFocus()
            ImeUtils.showIme(searchView)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FeedAdapter.REQUEST_CODE_VIEW_SHOT ->
                // by default we focus the search field when entering this screen. Don't do that
                // when returning from viewing a search result.
                focusQuery = false
        }
    }

    private fun dismiss() {
        // clear the background else the touch ripple moves with the translation which looks bad
        searchBack.background = null
        finishAfterTransition()
    }

    private fun save() {
        // show the save confirmation bubble
        TransitionManager.beginDelayedTransition(
            resultsContainer, getTransition(R.transition.search_show_confirm)
        )
        fab.visibility = View.INVISIBLE
        confirmSaveContainer.visibility = View.VISIBLE
        resultsScrim.visibility = View.VISIBLE
    }

    private fun doSave() {

        val saveData = Intent()

        saveData.putExtra(Activities.Search.EXTRA_QUERY, searchView.query.toString())
        saveData.putExtra(Activities.Search.EXTRA_SAVE_DRIBBBLE, saveDribbble.isChecked)
        saveData.putExtra(Activities.Search.EXTRA_SAVE_DESIGNER_NEWS, saveDesignerNews.isChecked)
        setResult(Activities.Search.RESULT_CODE_SAVE, saveData)
        dismiss()
    }

    private fun hideSaveConfirmation() {
        if (confirmSaveContainer.visibility == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(
                resultsContainer, getTransition(R.transition.search_hide_confirm)
            )
            confirmSaveContainer.visibility = View.GONE
            resultsScrim.visibility = View.GONE
            fab.visibility = results.visibility
        }
    }

    private fun toggleSaveCheck(ctv: CheckedTextView) = ctv.toggle()

    internal fun clearResults() {
        TransitionManager.beginDelayedTransition(
            container,
            getTransition(io.plaidapp.core.R.transition.auto)
        )
        feedAdapter.items = emptyList()
        viewModel.clearResults()
        results.visibility = View.GONE
        progress.visibility = View.GONE
        fab.visibility = View.GONE
        confirmSaveContainer.visibility = View.GONE
        resultsScrim.visibility = View.GONE
        setNoResultsVisibility(View.GONE)
    }

    private fun setNoResultsVisibility(visibility: Int) {
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults =
                    (findViewById<View>(R.id.stub_no_search_results) as ViewStub).inflate() as TextView
                noResults?.apply {
                    setOnClickListener {
                        searchView.setQuery("", false)
                        searchView.requestFocus()
                        ImeUtils.showIme(searchView)
                    }
                }
            }
            val message = String.format(
                getString(R.string.no_search_results), searchView.query.toString()
            ).toSpannable()
            message[message.indexOf('“') + 1, message.length - 1] = StyleSpan(Typeface.ITALIC)

            noResults?.apply { text = message }
        }
        noResults?.apply { this.visibility = visibility }
    }

    internal fun searchFor(query: String) {
        clearResults()
        progress.visibility = View.VISIBLE
        ImeUtils.hideIme(searchView)
        searchView.clearFocus()
        viewModel.searchFor(query)
    }

    private fun getTransition(@TransitionRes transitionId: Int): Transition? {
        var transition: Transition? = transitions.get(transitionId)
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId)
            transitions.put(transitionId, transition)
        }
        return transition
    }

    private fun setupSearchView() {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            // hint, inputType & ime options seem to be ignored from XML! Set in code
            queryHint = getString(R.string.search_hint)
            inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            imeOptions = imeOptions or EditorInfo.IME_ACTION_SEARCH or
                EditorInfo.IME_FLAG_NO_EXTRACT_UI or EditorInfo.IME_FLAG_NO_FULLSCREEN
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    searchFor(query)
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    if (TextUtils.isEmpty(query)) {
                        clearResults()
                    }
                    return true
                }
            })
            setOnQueryTextFocusChangeListener { _, hasFocus ->
                if (hasFocus && confirmSaveContainer.visibility == View.VISIBLE) {
                    hideSaveConfirmation()
                }
            }
        }
    }

    private fun setupTransitions() {
        // grab the position that the search icon transitions in *from*
        // & use it to configure the return transition
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementStart(
                sharedElementNames: List<String>,
                sharedElements: List<View>?,
                sharedElementSnapshots: List<View>
            ) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    val searchIcon = sharedElements[0]
                    if (searchIcon.id != R.id.searchback) return
                    val centerX = (searchIcon.left + searchIcon.right) / 2
                    val hideResults = TransitionUtils.findTransition(
                        window.returnTransition as TransitionSet,
                        CircularReveal::class.java, R.id.results_container
                    ) as CircularReveal?
                    hideResults?.setCenter(Point(centerX, 0))
                }
            }
        })
    }
}
