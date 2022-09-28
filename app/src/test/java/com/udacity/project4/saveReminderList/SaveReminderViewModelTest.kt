package com.udacity.project4.saveReminderList

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.data.source.FakeDataSource
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
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
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @Test
    fun saveReminder_enterValidData() {
        //Given
        val reminderItem = setReminderData()
        mainCoroutineRule.pauseDispatcher()

        //when
        saveReminderViewModel.validateAndSaveReminder(reminderItem)
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))


        //result
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }

    @Test
    fun saveReminder_titleIsMissing() {
        //Given
        val reminderList = ReminderDataItem(
            "", "Visit the Pyramids",
            "Pyramids", 29.9773, 31.1325)
        //when
        saveReminderViewModel.validateAndSaveReminder(reminderList)

        //result
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    @Test
    fun saveReminder_locationIsMissing() {
        //Given
        val reminderList = ReminderDataItem(
            "Pyramids", "Visit the Pyramids",
            "", 29.9773, 31.1325)

        //when
        saveReminderViewModel.validateAndSaveReminder(reminderList)

        //result
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), notNullValue())
    }

    private fun setReminderData(): ReminderDataItem {
        return ReminderDataItem(
            "EGYPT", "Visit the Pyramids",
            "Pyramids", 29.9773, 31.1325)
    }
}