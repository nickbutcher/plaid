package io.plaidapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.plaidapp.core.data.DataManager
import io.plaidapp.core.data.prefs.SourcesRepository
import io.plaidapp.core.designernews.data.login.LoginRepository
import org.junit.Rule
import org.junit.Test

/**
 * Tests for [HomeViewModel], with dependencies mocked.
 */
class HomeViewModelTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dataModel: DataManager = mock()
    private val loginRepository: LoginRepository = mock()
    private val sourcesRepository: SourcesRepository = mock()
    private val homeViewModel = HomeViewModel(dataModel, loginRepository, sourcesRepository)

    @Test
    fun logoutFromDesignerNews() {
        // When logging out from designer news
        homeViewModel.logoutFromDesignerNews()

        // Then logout is called
        verify(loginRepository).logout()
    }
}
