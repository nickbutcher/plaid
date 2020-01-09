/*
 * Copyright 2019 Google LLC.
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

package io.plaidapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.util.ViewPreloadSizeProvider
import io.plaidapp.R
import io.plaidapp.core.dagger.qualifier.IsPocketInstalled
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.feed.FeedAdapter
import io.plaidapp.core.feed.FeedProgressUiModel
import io.plaidapp.core.feed.FeedUiModel
import io.plaidapp.core.ui.ConnectivityChecker
import io.plaidapp.core.ui.HomeGridItemAnimator
import io.plaidapp.core.ui.filter.FilterAdapter
import io.plaidapp.core.ui.filter.FilterAnimator
import io.plaidapp.core.ui.filter.SourcesHighlightUiModel
import io.plaidapp.core.ui.filter.SourcesUiModel
import io.plaidapp.core.ui.recyclerview.InfiniteScrollListener
import io.plaidapp.core.util.Activities
import io.plaidapp.core.util.AnimUtils
import io.plaidapp.core.util.ColorUtils
import io.plaidapp.core.util.ViewUtils
import io.plaidapp.core.util.drawableToBitmap
import io.plaidapp.core.util.event.Event
import io.plaidapp.core.util.intentTo
import io.plaidapp.dagger.inject
import io.plaidapp.ui.recyclerview.FilterTouchHelperCallback
import io.plaidapp.ui.recyclerview.GridItemDividerDecoration
import javax.inject.Inject

/**
 * Main entry point to Plaid.
 *
 * Displays home feed.
 */
class HomeActivity : AppCompatActivity() {

    private var columns = 0
    private var filtersAdapter = FilterAdapter()
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var drawer: DrawerLayout

    private lateinit var toolbar: Toolbar
    private lateinit var grid: RecyclerView
    private lateinit var loading: ProgressBar
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var filtersList: RecyclerView

    // data
    @Inject
    lateinit var sourcesRepository: SourcesRepository

    @Inject
    @JvmField
    var connectivityChecker: ConnectivityChecker? = null

    @Inject
    lateinit var viewModel: HomeViewModel

    @IsPocketInstalled
    @JvmField
    var pocketInstalled = false

    private val noFiltersEmptyText by lazy {
        val view = findViewById<ViewStub>(R.id.stub_no_filters).inflate() as TextView
        // create the no filters empty text

        val emptyText = getText(R.string.no_filters_selected) as SpannedString
        val ssb = SpannableStringBuilder(emptyText)
        val annotations = emptyText.getSpans(0, emptyText.length, Annotation::class.java)

        annotations?.forEach { annotation ->
            if (annotation.key == "src") {
                // image span markup
                val id = annotation.getResId(this@HomeActivity)
                if (id != 0) {
                    ssb.setSpan(
                        ImageSpan(this, id, ImageSpan.ALIGN_BASELINE),
                        emptyText.getSpanStart(annotation),
                        emptyText.getSpanEnd(annotation),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } else if (annotation.key == "foregroundColor") {

                // foreground color span markup
                val id = annotation.getResId(this@HomeActivity)
                if (id != 0) {
                    ssb.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(this, id)),
                        emptyText.getSpanStart(annotation),
                        emptyText.getSpanEnd(annotation),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        with(view) {
            text = ssb
            setOnClickListener {
                drawer.openDrawer(GravityCompat.END)
            }
            view
        }
    }

    private val noConnection by lazy {
        findViewById<ViewStub>(R.id.stub_no_connection).inflate() as ImageView
    }

    private val toolbarElevation = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            // we want the grid to scroll over the top of the toolbar but for the toolbar items
            // to be clickable when visible. To achieve this we play games with elevation. The
            // toolbar is laid out in front of the grid but when we scroll, we lower it's elevation
            // to allow the content to pass in front (and reset when scrolled to top of the grid)
            if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                gridLayoutManager.findFirstVisibleItemPosition() == 0 &&
                gridLayoutManager.findViewByPosition(0)!!.top == grid.paddingTop &&
                toolbar.translationZ != 0f
            ) {
                // at top, reset elevation
                toolbar.translationZ = 0f
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING && toolbar.translationZ != -1f) {
                // grid scrolled, lower toolbar to allow content to pass in front
                toolbar.translationZ = -1f
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        bindResources()

        inject(this)

        feedAdapter = FeedAdapter(this, columns, pocketInstalled, ColorUtils.isDarkTheme(this))

        connectivityChecker?.apply {
            lifecycle.addObserver(this)
            connectedStatus.observe(this@HomeActivity, Observer<Boolean> {
                if (it) {
                    handleNetworkConnected()
                } else {
                    handleNoNetworkConnection()
                }
            })
        } ?: handleNoNetworkConnection()

        initViewModelObservers()

        drawer.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

        setSupportActionBar(toolbar)

        if (savedInstanceState == null) {
            animateToolbar()
        }
        setExitSharedElementCallback(FeedAdapter.createSharedElementReenterCallback(this))

        setupGrid()

        // drawer layout treats fitsSystemWindows specially so we have to handle insets ourselves
        drawer.setOnApplyWindowInsetsListener { _, insets ->
            handleDrawerInsets(insets)
            insets.consumeSystemWindowInsets()
        }

        setupTaskDescription()

        filtersList.apply {
            adapter = filtersAdapter
            itemAnimator = FilterAnimator()
        }

        val callback = FilterTouchHelperCallback(filtersAdapter, this)
        ItemTouchHelper(callback).also {
            it.attachToRecyclerView(filtersList)
        }
        checkEmptyState()
    }

    private fun initViewModelObservers() {
        viewModel.sources.observe(this@HomeActivity, Observer<SourcesUiModel> {
            filtersAdapter.submitList(it.sourceUiModels)
            if (it.highlightSources != null) {
                val highlightUiModel = (it.highlightSources as Event<SourcesHighlightUiModel>)
                    .consume()
                if (highlightUiModel != null) {
                    highlightPosition(highlightUiModel)
                }
            }
        })

        viewModel.feedProgress.observe(this@HomeActivity, Observer<FeedProgressUiModel> {
            if (it.isLoading) {
                feedAdapter.dataStartedLoading()
            } else {
                feedAdapter.dataFinishedLoading()
            }
        })

        viewModel.getFeed(columns).observe(this@HomeActivity, Observer<FeedUiModel> {
            feedAdapter.items = it.items
            checkEmptyState()
        })
    }

    private fun bindResources() {
        drawer = findViewById(R.id.drawer)
        toolbar = findViewById(R.id.toolbar)
        grid = findViewById(R.id.grid)
        filtersList = findViewById(R.id.filters)
        loading = findViewById(android.R.id.empty)

        columns = resources.getInteger(R.integer.num_columns)
    }

    private fun setupGrid() {
        gridLayoutManager = GridLayoutManager(this@HomeActivity, columns).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return feedAdapter.getItemColumnSpan(position)
                }
            }
        }
        val infiniteScrollListener =
            object : InfiniteScrollListener(gridLayoutManager) {
                override fun onLoadMore() {
                    viewModel.loadData()
                }

                override fun isDataLoading(): Boolean {
                    val uiModel = viewModel.feedProgress.value
                    return uiModel?.isLoading ?: false
                }
            }

        val shotPreloadSizeProvider = ViewPreloadSizeProvider<Shot>()
        val shotPreloader = RecyclerViewPreloader(
            this@HomeActivity,
            feedAdapter,
            shotPreloadSizeProvider,
            4
        )

        with(grid) {
            layoutManager = gridLayoutManager
            adapter = feedAdapter
            addOnScrollListener(toolbarElevation)
            addOnScrollListener(infiniteScrollListener)
            setHasFixedSize(true)
            addItemDecoration(
                GridItemDividerDecoration(
                    this@HomeActivity, R.dimen.divider_height,
                    R.color.divider
                )
            )
            itemAnimator = HomeGridItemAnimator()
            addOnScrollListener(shotPreloader)
        }
    }

    private fun handleDrawerInsets(insets: WindowInsets) {
        // inset the toolbar down by the status bar height
        val lpToolbar = (toolbar.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin += insets.systemWindowInsetTop
            leftMargin += insets.systemWindowInsetLeft
            rightMargin += insets.systemWindowInsetRight
        }
        toolbar.layoutParams = lpToolbar

        // inset the grid top by statusbar+toolbar & the bottom by the navbar (don't clip)
        grid.setPadding(
            grid.paddingLeft + insets.systemWindowInsetLeft, // landscape
            insets.systemWindowInsetTop + ViewUtils.getActionBarSize(this@HomeActivity),
            grid.paddingRight + insets.systemWindowInsetRight, // landscape
            grid.paddingBottom + insets.systemWindowInsetBottom
        )

        // we place a background behind the status bar to combine with it's semi-transparent
        // color to get the desired appearance.  Set it's height to the status bar height
        val statusBarBackground = findViewById<View>(R.id.status_bar_background)
        val lpStatus = (statusBarBackground.layoutParams as FrameLayout.LayoutParams).apply {
            height = insets.systemWindowInsetTop
        }
        statusBarBackground.layoutParams = lpStatus

        // inset the filters list for the status bar / navbar
        // need to set the padding end for landscape case
        val ltr = filtersList.layoutDirection == View.LAYOUT_DIRECTION_LTR
        with(filtersList) {
            setPaddingRelative(
                paddingStart,
                paddingTop + insets.systemWindowInsetTop,
                paddingEnd + if (ltr) {
                    insets.systemWindowInsetRight
                } else {
                    0
                },
                paddingBottom + insets.systemWindowInsetBottom
            )
        }

        // clear this listener so insets aren't re-applied
        drawer.setOnApplyWindowInsetsListener(null)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (data == null || resultCode != Activity.RESULT_OK ||
            !data.hasExtra(Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID)
        ) {
            return
        }

        // When reentering, if the shared element is no longer on screen (e.g. after an
        // orientation change) then scroll it into view.
        val sharedShotId = data.getLongExtra(
            Activities.Dribbble.Shot.RESULT_EXTRA_SHOT_ID,
            -1L
        )
        if (sharedShotId != -1L && // returning from a shot
            feedAdapter.items.isNotEmpty() && // grid populated
            grid.findViewHolderForItemId(sharedShotId) == null
        ) { // view not attached
            val position = feedAdapter.getItemPosition(sharedShotId)
            if (position == RecyclerView.NO_POSITION) return

            // delay the transition until our shared element is on-screen i.e. has been laid out
            postponeEnterTransition()
            grid.apply {
                addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                    override fun onLayoutChange(
                        v: View,
                        l: Int,
                        t: Int,
                        r: Int,
                        b: Int,
                        oL: Int,
                        oT: Int,
                        oR: Int,
                        oB: Int
                    ) {
                        removeOnLayoutChangeListener(this)
                        startPostponedEnterTransition()
                    }
                })
                scrollToPosition(position)
            }
            toolbar.translationZ = -1f
        }
    }

    private fun setupToolbar() {
        toolbar.inflateMenu(R.menu.main)
        val toggleTheme = toolbar.menu.findItem(R.id.menu_theme)
        val actionView = toggleTheme.actionView

        (actionView as AppCompatCheckBox?)?.apply {
            setButtonDrawable(R.drawable.asl_theme)
            isChecked = ColorUtils.isDarkTheme(this@HomeActivity)
            jumpDrawablesToCurrentState()
            setOnCheckedChangeListener { _, isChecked ->
                // delay to allow the toggle anim to run
                postDelayed(
                    {
                        AppCompatDelegate.setDefaultNightMode(
                            if (isChecked)
                                AppCompatDelegate.MODE_NIGHT_YES
                            else
                                AppCompatDelegate.MODE_NIGHT_NO
                        )
                        delegate.applyDayNight()
                    },
                    800L
                )
            }
            TooltipCompat.setTooltipText(this, getString(R.string.theme))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        setupToolbar()
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.menu_designer_news_login)
            .setTitle(
                if (viewModel.isDesignerNewsUserLoggedIn()) {
                    R.string.designer_news_log_out
                } else {
                    R.string.designer_news_login
                }
            )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_filter -> {
                drawer.openDrawer(GravityCompat.END)
                true
            }
            R.id.menu_search -> {
                val searchMenuView = toolbar.findViewById<View>(R.id.menu_search)
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    this, searchMenuView,
                    getString(R.string.transition_search_back)
                ).toBundle()
                startActivityForResult(intentTo(Activities.Search), RC_SEARCH, options)
                true
            }
            R.id.menu_designer_news_login -> {
                if (!viewModel.isDesignerNewsUserLoggedIn()) {
                    startActivity(intentTo(Activities.DesignerNews.Login))
                } else {
                    viewModel.logoutFromDesignerNews()
                    // TODO something better than a toast
                    Toast.makeText(
                        applicationContext, R.string.designer_news_logged_out,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            R.id.menu_about -> {
                startActivity(
                    intentTo(Activities.About),
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle()
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            RC_SEARCH -> {
                // reset the search icon which we hid
                toolbar.findViewById<View>(R.id.menu_search)?.alpha = 1f

                if (resultCode == Activities.Search.RESULT_CODE_SAVE && data != null) {
                    with(data) {
                        val query = getStringExtra(Activities.Search.EXTRA_QUERY) as String
                        val isDribbble =
                            getBooleanExtra(Activities.Search.EXTRA_SAVE_DRIBBBLE, false)
                        val isDesignerNews =
                            getBooleanExtra(Activities.Search.EXTRA_SAVE_DESIGNER_NEWS, false)
                        viewModel.addSources(query, isDribbble, isDesignerNews)
                    }
                }
            }
        }
    }

    private fun checkEmptyState() {
        if (feedAdapter.items.isEmpty()) {
            // if grid is empty check whether we're loading or if no filters are selected
            if (sourcesRepository.getActiveSourcesCount() > 0 && connectivityChecker != null) {
                connectivityChecker?.connectedStatus?.value?.let {
                    loading.visibility = View.VISIBLE
                    setNoFiltersEmptyTextVisibility(View.GONE)
                }
            } else {
                loading.visibility = View.GONE
                setNoFiltersEmptyTextVisibility(View.VISIBLE)
            }
            toolbar.translationZ = 0f
        } else {
            loading.visibility = View.GONE
            setNoFiltersEmptyTextVisibility(View.GONE)
        }
    }

    private fun setNoFiltersEmptyTextVisibility(visibility: Int) {
        noFiltersEmptyText.visibility = visibility
    }

    @Suppress("DEPRECATION")
    private fun setupTaskDescription() {
        val overviewIcon = drawableToBitmap(this, applicationInfo.icon)
        setTaskDescription(
            ActivityManager.TaskDescription(
                getString(R.string.app_name),
                overviewIcon,
                ContextCompat.getColor(this, R.color.primary)
            )
        )
        overviewIcon?.recycle()
    }

    private fun animateToolbar() {
        // this is gross but toolbar doesn't expose it's children to animate them :(
        val t = toolbar.getChildAt(0)
        if (t is TextView) {
            t.apply {
                /*
                Fade in and space out the title.
                Animating the letterSpacing performs horribly so fake it by setting the desired
                letterSpacing then animating the scaleX.

                ¯\_(ツ)_/¯
                 */
                alpha = 0f
                scaleX = 0.8f

                animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .setStartDelay(300)
                    .setDuration(900).interpolator =
                    AnimUtils.getFastOutSlowInInterpolator(this@HomeActivity)
            }
        }
    }

    /**
     * Highlight the new source(s) by:
     * 1. opening the drawer
     * 2. scrolling new source(s) into view
     * 3. flashing new source(s) background
     * 4. closing the drawer (if user hasn't interacted with it)
     */
    private fun highlightPosition(uiModel: SourcesHighlightUiModel) {
        val closeDrawerRunnable = { drawer.closeDrawer(GravityCompat.END) }
        drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {

            // if the user interacts with the filters while it's open then don't auto-close
            private val filtersTouch = View.OnTouchListener { _, _ ->
                drawer.removeCallbacks(closeDrawerRunnable)
                false
            }

            override fun onDrawerOpened(drawerView: View) {
                // scroll to the new item(s)
                filtersList.apply {
                    smoothScrollToPosition(uiModel.scrollToPosition)
                    setOnTouchListener(filtersTouch)
                }
                filtersAdapter.highlightPositions(uiModel.highlightPositions)
            }

            @SuppressLint("ClickableViewAccessibility")
            override fun onDrawerClosed(drawerView: View) {
                // reset
                filtersList.setOnTouchListener(null)
                drawer.removeDrawerListener(this)
            }

            override fun onDrawerStateChanged(newState: Int) {
                // if the user interacts with the drawer manually then don't auto-close
                if (newState == DrawerLayout.STATE_DRAGGING) {
                    drawer.removeCallbacks(closeDrawerRunnable)
                }
            }
        })
        drawer.openDrawer(GravityCompat.END)
        drawer.postDelayed(closeDrawerRunnable, 2000L)
    }

    private fun handleNoNetworkConnection() {
        loading.visibility = View.GONE
        (getDrawable(R.drawable.avd_no_connection) as AnimatedVectorDrawable).apply {
            noConnection.setImageDrawable(this)
            start()
        }
    }

    private fun handleNetworkConnected() {
        if (feedAdapter.items.isNotEmpty()) return

        TransitionManager.beginDelayedTransition(drawer)
        noConnection.visibility = View.GONE
        loading.visibility = View.VISIBLE
        viewModel.loadData()
    }

    companion object {

        private const val RC_SEARCH = 0
        private const val RC_NEW_DESIGNER_NEWS_LOGIN = 5
    }
}

/**
 * Get the resource identifier for an annotation.
 * @param context The context this should be executed in.
 */
fun Annotation.getResId(context: Context): Int {
    return context.resources.getIdentifier(value, null, context.packageName)
}
