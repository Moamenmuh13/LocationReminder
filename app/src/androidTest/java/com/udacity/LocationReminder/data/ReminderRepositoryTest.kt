package com.udacity.LocationReminder.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ReminderRepositoryTest {
    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initDatabase() {
        database =
            Room.inMemoryDatabaseBuilder(getApplicationContext(), RemindersDatabase::class.java)
                .allowMainThreadQueries()
                .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveReminder_getDataFromReminder() {
        runBlocking{

            //Given
            val reminderData =
                setReminderData()
            repository.saveReminder(reminderData)

            val reminder = repository.getReminder(reminderData.id)

            //Result
            assertThat(reminder is Result.Success, `is`(true))
            reminder as Result.Success

            assertThat(reminder.data.title, `is`(reminderData.title))
            assertThat(reminder.data.description, `is`(reminderData.description))
            assertThat(reminder.data.latitude, `is`(reminderData.latitude))
            assertThat(reminder.data.longitude, `is`(reminderData.longitude))
            assertThat(reminder.data.location, `is`(reminderData.location))
        }

    }

    @Test
    fun deleteReminder_getDataFromReminderById() {
        runBlocking {

            //Given
            val reminderData =
                setReminderData()
            repository.saveReminder(reminderData)
            repository.deleteAllReminders()

            val reminder = repository.getReminder(reminderData.id)

            //Result
            assertThat(reminder is Result.Error, `is`(true))
            reminder as Result.Error

            assertThat(reminder.message , `is`("Reminder not found!") )
        }
    }

    private fun setReminderData(): ReminderDTO {
        return ReminderDTO("EGYPT", "Visit the Pyramids",
            "Pyramids", 29.9773, 31.1325)
    }
}