package com.udacity.project4.reminderList

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.source.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


//Cant run it on API Level 30 *ERROR CODE* API level 30 is not available
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class ReminderListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @Before
    fun setupTheRepository() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(getApplicationContext(), fakeDataSource)
    }

    @Test
    fun reminderListHasNoData_showLoadingProgress() {
        reminderListViewModel.showLoading.value = false
        runBlockingTest {
            //Given
            mainCoroutineRule.pauseDispatcher()
            reminderListViewModel.loadReminders()

            reminderListViewModel.showLoading.value = true

            //result
            assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        }

    }

    @Test
    fun reminderListHasNoData_hideLoadingProgress() {
        reminderListViewModel.showLoading.value = true
        runBlockingTest {
            //Given
            reminderListViewModel.loadReminders()
            reminderListViewModel.showLoading.value = false
            //result
            assertThat(reminderListViewModel.showLoading.getOrAwaitValue(),
                `is`(false))
        }
    }

    @Test
    fun reminderListHasNotData_emptyResult() {
        reminderListViewModel.showLoading.value = true
        runBlockingTest {
            //Given
            fakeDataSource.deleteAllReminders()
            //When
            reminderListViewModel.loadReminders()
            reminderListViewModel.showLoading.value = false
            //result
            assertThat(reminderListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
            assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
        }
    }

    @Test
    fun loadReminderData_whenNoDataAvailable() {
        fakeDataSource.setReturnError(true)
        reminderListViewModel.loadReminders()


        assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Error"))
    }

    @Test
    fun loadReminderData_withData() {
        reminderListViewModel.showLoading.value = true
        runBlockingTest {
            //Given
            fakeDataSource.saveReminder(setReminderData())

            //When
            reminderListViewModel.loadReminders()
            reminderListViewModel.showLoading.value = false

            //result
            assertThat(reminderListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(false))
            assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))
            assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(false))
        }
    }

    private fun setReminderData(): ReminderDTO {
        return ReminderDTO("EGYPT", "Visit the Pyramids",
            "Pyramids", 29.9773, 31.1325)
    }
}