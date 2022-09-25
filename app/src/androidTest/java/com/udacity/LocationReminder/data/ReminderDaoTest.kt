package com.udacity.LocationReminder.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {

    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertReminderIntoDatabase_and_getReminderById() {
        runBlockingTest {
            //Given
            val reminderData =
                ReminderDTO("EGYPT", "Visit the Pyramids",
                    "Pyramids", 29.9773, 31.1325)
            database.reminderDao().saveReminder(reminderData)

            //When
            val loaded = database.reminderDao().getReminderById(reminderData.id)

            //Result
            assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
            assertThat(loaded.id , `is`(reminderData.id))
            assertThat(loaded.title , `is`(reminderData.title))
            assertThat(loaded.description , `is`(reminderData.description))
            assertThat(loaded.latitude , `is`(reminderData.latitude))
            assertThat(loaded.longitude , `is`(reminderData.longitude))
            assertThat(loaded.location , `is`(reminderData.location))

        }
    }
}