package com.udacity.project4.saveReminderList

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.source.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setupTheRepository() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun saveReminder_enterValidData() {
        //when
        val savedReminderDataItem = ReminderDataItem(
            "test", "test", "test", 22.5492551, 24.0156831)

        //then
        saveReminderViewModel.saveReminder(savedReminderDataItem)

        //result
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun saveReminder_withoutEnteringTitle() {
        //when
        val savedReminderData = ReminderDataItem(
            "", "test", "test", 22.5492551, 24.0156831
        )
        //then
        saveReminderViewModel.validateAndSaveReminder(savedReminderData)

        //result
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun saveReminder_withoutSelectingLocation() {
        //when
        val savedReminderData = ReminderDataItem(
            "test", "test", "", 22.5492551, 24.0156831
        )
        //then
        saveReminderViewModel.validateAndSaveReminder(savedReminderData)

        //result
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }
}