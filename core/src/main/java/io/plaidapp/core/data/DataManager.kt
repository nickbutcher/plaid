/*
 * Copyright 2019 Google, Inc.
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

package io.plaidapp.core.data

import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem
import io.plaidapp.core.designernews.data.DesignerNewsSearchSourceItem.Companion.SOURCE_DESIGNER_NEWS_POPULAR
import io.plaidapp.core.designernews.domain.LoadStoriesUseCase
import io.plaidapp.core.designernews.domain.SearchStoriesUseCase
import io.plaidapp.core.dribbble.data.DribbbleSourceItem
import io.plaidapp.core.dribbble.data.ShotsRepository
import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.core.dribbble.data.api.model.Shot
import io.plaidapp.core.dribbble.data.api.model.User
import io.plaidapp.core.producthunt.data.ProductHuntSourceItem.Companion.SOURCE_PRODUCT_HUNT
import io.plaidapp.core.producthunt.domain.LoadPostsUseCase
import io.plaidapp.core.ui.filter.FiltersChangedCallback
import io.plaidapp.core.util.exhaustive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * Data class mapping the key based on which we're requesting data and the page
 */
private data class InFlightRequestData(val key: String, val page: Int)

/**
 * Responsible for loading data from the various sources. Instantiating classes are responsible for
 * providing the {code onDataLoaded} method to do something with the data.
 */
class DataManager @Inject constructor(
    private val loadStories: LoadStoriesUseCase,
    private val loadPosts: LoadPostsUseCase,
    private val searchStories: SearchStoriesUseCase,
    private val shotsRepository: ShotsRepository,
    private val sourcesRepository: SourcesRepository,
    private val dispatcherProvider: CoroutinesDispatcherProvider
) : DataLoadingSubject {

    private val parentJob = SupervisorJob()
    private val scope = CoroutineScope(dispatcherProvider.computation + parentJob)

    private val parentJobs = mutableMapOf<InFlightRequestData, Job>()

    private val loadingCount = AtomicInteger(0)
    private var loadingCallbacks = mutableListOf<DataLoadingSubject.DataLoadingCallbacks>()
    private var onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>? = null
    private lateinit var pageIndexes: MutableMap<String, Int>

    private val filterListener = object : FiltersChangedCallback() {
        override fun onFiltersChanged(changedFilter: SourceItem) {
            if (changedFilter.active) {
                loadSource(changedFilter)
            } else { // filter deactivated
                val key = changedFilter.key
                parentJobs.filter { it.key.key == key }.forEach { job ->
                    job.value.cancel()
                    parentJobs.remove(job.key)
                }
                // clear the page index for the source
                pageIndexes[key] = 0
            }
        }
    }

    init {
        sourcesRepository.registerFilterChangedCallback(filterListener)
        // build a map of source keys to pages initialized to 0
        pageIndexes = sourcesRepository.getSourcesSync().map { it.key to 0 }.toMap().toMutableMap()
    }

    fun setOnDataLoadedCallback(
        onDataLoadedCallback: OnDataLoadedCallback<List<PlaidItem>>?
    ) {
        this.onDataLoadedCallback = onDataLoadedCallback
    }

    private fun onDataLoaded(data: List<PlaidItem>) {
        onDataLoadedCallback?.onDataLoaded(data)
    }

    suspend fun loadMore() = withContext(dispatcherProvider.computation) {
        sourcesRepository.getSources().forEach { loadSource(it) }
    }

    fun cancelLoading() {
        parentJobs.values.forEach { it.cancel() }
        parentJobs.clear()
    }

    private fun loadSource(source: SourceItem) {
        if (source.active) {
            loadStarted()
            val page = getNextPageIndex(source.key)
            // TODO each source data loading should be delegated to a different object
            // specialized in loading that specific type of data
            val data = InFlightRequestData(source.key, page)
            when (source.key) {
                SOURCE_DESIGNER_NEWS_POPULAR -> {
                    parentJobs[data] = launchLoadDesignerNewsStories(data)
                }
                SOURCE_PRODUCT_HUNT -> {
                    parentJobs[data] = launchLoadProductHunt(data)
                }
                else -> if (source is DribbbleSourceItem) {
                    parentJobs[data] = loadDribbbleSearch(source, data)
                } else if (source is DesignerNewsSearchSourceItem) {
                    parentJobs[data] = loadDesignerNewsSearch(source, data)
                }
            }
        }
    }

    private fun getNextPageIndex(dataSource: String): Int {
        var nextPage = 1 // default to one – i.e. for newly added sources
        if (pageIndexes.containsKey(dataSource)) {
            nextPage = pageIndexes.getValue(dataSource) + 1
        }
        pageIndexes[dataSource] = nextPage
        return nextPage
    }

    private fun sourceIsEnabled(key: String): Boolean {
        return pageIndexes[key] != 0
    }

    private fun sourceLoaded(
        data: List<PlaidItem>?,
        source: String,
        request: InFlightRequestData
    ) {
        loadFinished()
        if (data != null && !data.isEmpty() && sourceIsEnabled(source)) {
            setPage(data, request.page)
            setDataSource(data, source)
            onDataLoaded(data)
        }
        parentJobs.remove(request)
    }

    private fun loadFailed(request: InFlightRequestData) {
        loadFinished()
        parentJobs.remove(request)
    }

    private fun launchLoadDesignerNewsStories(data: InFlightRequestData) = scope.launch {
        val result = loadStories(data.page)
        when (result) {
            is Result.Success -> sourceLoaded(
                result.data,
                SOURCE_DESIGNER_NEWS_POPULAR,
                data
            )
            is Result.Error -> loadFailed(data)
        }.exhaustive
    }

    private fun loadDesignerNewsSearch(
        sourceItem: DesignerNewsSearchSourceItem,
        data: InFlightRequestData
    ) = scope.launch {
        val result = searchStories(sourceItem.key, data.page)
        when (result) {
            is Result.Success -> sourceLoaded(result.data, sourceItem.key, data)
            is Result.Error -> loadFailed(data)
        }.exhaustive
    }

    private fun loadDribbbleSearch(source: DribbbleSourceItem, data: InFlightRequestData) =
        scope.launch {
            //val result = shotsRepository.search(source.query, data.page)

            val player = User(
                id = 1L,
                name = "Nick Butcher",
                username = "nickbutcher",
                avatarUrl = "www.prettyplaid.nb"
            )

            val shots = listOf(
                Shot(
                    id = 5312628L,
                    title = "Motion design doesn't have to be hard",
                    description = "People often tell me that designing motion is complicated, or that choosing the right values is ambiguous. I contend that in areas most important to a UI, motion design can and should be simple. Read more in my latest blog post for Google Design \"Motion design doesn't have to be hard\": https://medium.com/google-design/motion-design-doesnt-have-to-be-hard-33089196e6c2",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/515528/screenshots/5312628/medium_motion_article_-_social_asset_-_800x600.gif"),
                    likesCount = 608,
                    viewsCount = 20472,
                    user = User(
                        id = 1L,
                        name = "Jonas Naimark",
                        username = "jonasnaimark",
                        avatarUrl = "https://cdn.dribbble.com/users/515528/avatars/normal/SelfPortraitNew.jpg?1394035572"
                    )
                ),
                Shot(
                    id = 6325914L,
                    title = "Google Ad Settings illustrations",
                    description = "One of nearly 500 Illustrations I created for Google Ad Settings",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/314661/screenshots/6325914/adsettings_games-02_4x.png"),
                    user = player
                ),
                Shot(
                    id = 5697331L,
                    title = "Material studies",
                    description = "Launching an app for the first time is a great opportunity to highlight branding through motion. It was fun to explore how motion could express the brand of each Material study. In addition to animated logos, the way a UI builds after launch helps set the tone for the app. For example, Owl's energetic brand is conveyed in motion by using emphasized stagger and overshoot. In contrast, Fortnightly's subtle brand calls for short durations with no overshoot and minimal stagger. Read more about how to theme your app using motion here. All credit to Material's visual design team for creating these amazing studies!",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/515528/screenshots/5697331/logos_dribbble_zoomed_2.mov.gif"),
                    likesCount = 693,
                    viewsCount = 21929,
                    user = User(
                        id = 1L,
                        name = "Jonas Naimark",
                        username = "jonasnaimark",
                        avatarUrl = "https://cdn.dribbble.com/users/515528/avatars/normal/SelfPortraitNew.jpg?1394035572"
                    )
                ),
                Shot(
                    id = 5490638L,
                    title = "UXU Posters",
                    description = "A couple of posters designed for UXU, Google's annual internal UX conference. The Posters represent themes of community, learning, and inspiration.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/765587/screenshots/5490638/jgabbard_02_uxu-posters-sized.png"),
                    viewsCount = 11063,
                    likesCount = 489,
                    user = User(
                        id = 2L,
                        name = "Justin Gabbard",
                        username = "jgabbs",
                        avatarUrl = "https://cdn.dribbble.com/users/765587/avatars/normal/cb14a28de505ef5ad3dec5ec21cd9fe6.jpg?1509207574"
                    )
                ),
                Shot(
                    id = 6220034L,
                    title = "iOS Google Drive Redesign",
                    description = "I'm excited to announce the newly redesigned Google Drive app on iOS! This is the first major redesign for Drive's app in a long time, and it was much needed.\n" +
                        "\n" +
                        "With research and a robust design process, we extensively tested both internally and externally to settle on our design decisions. Some of the highlights include – making it easier to find important files with the Home tab and a major overall performance improvement.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/185678/screenshots/6220034/final-comp_5.gif"),
                    user = player
                ),
                Shot(
                    id = 6090151L,
                    title = "Guide to Material Motion in After Effects",
                    description = "In an ongoing effort to make the case that Motion Design Doesn't Have to be Hard, I'm excited to share my latest article: Guide to Material Motion in After Effects. It outlines my workflow for animating Material style transitions. I've also included my AE file so you can see what a project looks like using this workflow.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/515528/screenshots/6090151/motion_sticker_sheet_-_social_asset_-_800x600.gif"),
                    user = player
                ),
                Shot(
                    id = 6068973L,
                    title = "See all your trips",
                    description = "We just updated Google Travel with all your potential trips, upcoming trips, tracked flights, and more all in one spot! I created a fun animated illustration to help educate users on this new feature.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/129177/screenshots/6068973/yourtrips_backpack_final_2x.gif"),
                    user = player
                ),
                Shot(
                    id = 5849757L,
                    title = "Welcome to Wear OS",
                    description = "Intro animation I made for the on-boarding experience on Wear OS. This was part of a bigger set which I created for the rebranding of Android Wear to Wear Os by Google. I tried to keep these illustrations simple and minimalistic and let motion tell the story rather than rely on complex imagery.\n" +
                        "\n" +
                        "Last year I got to work and learn a lot about on-boarding experiences and how might we use illustration and animation to improve this journey by making it more consistent, delightful and less ambiguous.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/374672/screenshots/5849757/wear_intro.gif"),
                    user = player
                ),
                Shot(
                    id = 5695902L,
                    title = "Google Translate Web UI",
                    description = "Here's a closer look on the updated UI elements for Google Translate. Features that were previously hidden, such as Document translation, Saved translations and Community, are now front and center. Check it out on our new website!",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/502247/screenshots/5695902/dribbble_shot_2_2x.png"),
                    user = player
                ),
                Shot(
                    id = 5662051L,
                    title = "Reducing gender bias in Google Translate",
                    description = "It's been a busy couple of weeks here at Google Translate. On the heels of the launch of our brand new website, today we took a first step towards reducing gender bias in our translations by providing feminine and masculine translations for some gender-neutral words and phrases. Read more about our latest launch on the Google Keyword blog ",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/27716/screenshots/5662051/gender-gif-lossless.gif"),
                    user = player
                ),
                Shot(
                    id = 5624935L,
                    title = "Google Translate Redesign",
                    description = "We redesigned the Google Translate website! Being an essential tool for communicating across languages, our goal for the redesign was more than just a reskin. We wanted to improve the discoverability of our features and create a consistent experience on all surfaces. Happy to be part of such an awesome project!\n" +
                        "\n" +
                        "Check out the redesign on our website. \n" +
                        "Read about it on our blog.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/502247/screenshots/5624935/final_composition_option_4_.gif"),
                    user = player
                ),
                Shot(
                    id = 5609096L,
                    title = "Google's product excellence principles",
                    description = "We have always been a company that cares deeply about our users, product excellence is a shared responsibility for all of us. It's a culture, not a checklist. \n" +
                        "Set of illustrations represents the 3 core principles of Google's internal product excellence framework: crafted execution, simple design and focused utility. \n" +
                        "Motion design by Richard Hawkins. @Richard Hawkins",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/129991/screenshots/5609096/pe_pillars_large__1_.gif"),
                    user = player
                ),
                Shot(
                    id = 5520125L,
                    title = "Google Account illustration set",
                    description = "Google Privacy Policy was updated with new illustrations and clearer language to better describe the information that is collected, why it's collected, and how a user can control it. \n" +
                        "Happy to have been a part of this awesome project. \n" +
                        "https://www.behance.net/gallery/69625321/Google-Account-Illustrations",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/129991/screenshots/5520125/set_part_1.png"),
                    user = player
                ),
                Shot(
                    id = 5490402L,
                    title = "UXU Posters",
                    description = "Had lots of fun doing these posters for UXU, Google’s internal UX conference happening every year. Huge thanks to @Homer Rutledge for the lovely animation!",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/108482/screenshots/5490402/geometrieva_uxu.gif"),
                    user = player
                ),
                Shot(
                    id = 5490383L,
                    title = "UXU Posters",
                    description = "Poster designs for UXU, Google's annual internal UX conference. Each poster represents one theme: community, inspiration, and learning.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/1667465/screenshots/5490383/uxu_poster_art.png"),
                    user = player
                ),
                Shot(
                    id = 5490322L,
                    title = "UXU",
                    description = "Together with some fellow Google designers, we created an installation for the annual UXU conference, an internal event. Each poster represents a pillar of the Google design community. Here's \"creativity\" with the addition of a little animation.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/314661/screenshots/5490322/uxu_creativity_dgearity_animation_800x600.gif"),
                    user = player
                ),
                Shot(
                    id = 5371934L,
                    title = "Welcome to your Pixel 3",
                    description = "The Pixel 3 was announced today! I had the pleasure of working on another exciting release. This was an animation I created welcoming users to the phone as they begin set up their device.\n" +
                        "\n" +
                        "Design by Dominic Flask",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/492711/screenshots/5371934/pixel_welcome_01.gif"),
                    user = player
                ),
                Shot(
                    id = 5269234L,
                    title = "Chrome New Tab Page",
                    description = "It was an amazing opportunity to work on one of the most prominent surfaces in Chrome, the \"new tab page\". In addition to the Material UI, we also wanted to design a surface that reflects the user and allows them to make Chrome “their” browser. You can now upload/select custom backgrounds and customize your shortcuts with edit/add/remove functionality for quick navigation to common sites.",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/3747/screenshots/5269234/ntp-dribble-detail-com.gif"),
                    user = player
                ),
                Shot(
                    id = 5217905L,
                    title = "Chrome Desktop Omnibox",
                    description = "This last week has been an exciting one for Chrome as it turned 10...and got a brand new look! It was a pleasure contributing to the redesign of the Omnibox (desktop’s search bar + address bar) while also bringing a little Google smarts to this surface.\n" +
                        "\n" +
                        "You can now get answers directly in the address bar without having to open a new tab—from rich results on public figures or sporting events, to instant answers like the local weather a translation. My last and favorite, you can now search for a site in the Omnibox and Chrome will tell you if it’s already open and let you jump straight to it with “Switch to tab.”",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/3747/screenshots/5217905/chrome_omnibox-final_sm.gif"),
                    user = player
                ),
                Shot(
                    id = 5192951L,
                    title = "Google The Three Respects",
                    description = "The Play Illustration team recently worked with the internal UX Community & Culture team on a set of stickers that illustrate Google’s company values, The Three Respects.\n" +
                        "\n" +
                        "We wanted to share some images of the final product, two sets of three stickers which highlight each respect:\n" +
                        "\n" +
                        "Respect the User \n" +
                        "Respect the Opportunity \n" +
                        "Respect Each Other\n" +
                        "\n" +
                        "These stickers were created to be shared company-wide to remind everyone of these core values.\n" +
                        "\n" +
                        "Art Direction/Design: Darlene Gibson \n" +
                        "Illustration: Matthew Hollister, Justin Gabbard",
                    images = Images(hidpi = "https://cdn.dribbble.com/users/765587/screenshots/5192951/samples-all.gif"),
                    user = player
                )
            )

            shotsRepository.cache(shots)

            val result = Result.Success(shots)

            //when (result) {
            //    is Result.Success ->
            sourceLoaded(result.data, source.key, data)
                //is Result.Error -> loadFailed(data)
            //}.exhaustive
        }

    private fun launchLoadProductHunt(data: InFlightRequestData) = scope.launch {
        // this API's paging is 0 based but this class (& sorting) is 1 based so adjust locally
        val result = loadPosts(data.page - 1)
        when (result) {
            is Result.Success -> sourceLoaded(result.data, SOURCE_PRODUCT_HUNT, data)
            is Result.Error -> loadFailed(data)
        }.exhaustive
    }

    override fun registerCallback(callback: DataLoadingSubject.DataLoadingCallbacks) {
        loadingCallbacks.add(callback)
    }

    private fun loadStarted() {
        if (0 == loadingCount.getAndIncrement()) {
            dispatchLoadingStartedCallbacks()
        }
    }

    private fun loadFinished() {
        if (0 == loadingCount.decrementAndGet()) {
            dispatchLoadingFinishedCallbacks()
        }
    }

    private fun setPage(items: List<PlaidItem>, page: Int) {
        items.forEach {
            it.page = page
        }
    }

    private fun setDataSource(items: List<PlaidItem>, dataSource: String) {
        for (item in items) {
            item.dataSource = dataSource
        }
    }

    private fun dispatchLoadingStartedCallbacks() {
        loadingCallbacks.forEach {
            it.dataStartedLoading()
        }
    }

    private fun dispatchLoadingFinishedCallbacks() {
        loadingCallbacks.forEach {
            it.dataFinishedLoading()
        }
    }
}
