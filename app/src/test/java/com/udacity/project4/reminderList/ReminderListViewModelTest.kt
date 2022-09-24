package com.udacity.project4.reminderList

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.source.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


// when i run on API 30 give me an error API level 30 is not available
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
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
    fun noData_showLoadingProgress() {
        runBlockingTest {
            //then
            mainCoroutineRule.pauseDispatcher()
            reminderListViewModel.loadReminders()

            //result
            assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isTrue()
        }
    }

    @Test
    fun noData_hideLoadingProgress() {
        runBlockingTest {
            //when
            reminderListViewModel.loadReminders()

            //result
            assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isFalse()
        }
    }

    @Test
    fun reminderListHasNotData_emptyResult() {
        runBlockingTest {
            //when
            fakeDataSource.deleteAllReminders()
            //then
            reminderListViewModel.loadReminders()
            //result
            assertThat(reminderListViewModel.remindersList.getOrAwaitValue().isEmpty()).isTrue()
            assertThat(reminderListViewModel.showNoData.getOrAwaitValue()).isTrue()
        }
    }


    @Test
    fun loadReminderData_with_data() {
        runBlockingTest {
            //when
            val reminderData =
                ReminderDTO("EGYPT", "Visit the Pyramids", "Pyramids", 29.9773, 31.1325)
            fakeDataSource.saveReminder(reminderData)

            //then
            reminderListViewModel.loadReminders()

            //result
            assertThat(reminderListViewModel.remindersList.getOrAwaitValue()
                .isEmpty()).isFalse()
            assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isFalse()
            assertThat(reminderListViewModel.showNoData.getOrAwaitValue()).isFalse()
        }
    }
}