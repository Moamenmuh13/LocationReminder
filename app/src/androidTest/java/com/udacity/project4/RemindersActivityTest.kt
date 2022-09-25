package com.udacity.project4



import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner.get
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest {
    private lateinit var repository: ReminderDataSource
    private lateinit var application: Application


    @Before
    fun init() {

        //stop koin first
        stopKoin()
        application = getApplicationContext()
        val module = module {
            viewModel {
                RemindersListViewModel(
                    application,
                    get() as ReminderDataSource
                )
            }
            single {
                RemindersListViewModel(application, get() as ReminderDataSource)
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(application) }
        }

        //starting new koin module
        startKoin {
            modules(listOf(module))
        }

        repository = get() as ReminderDataSource

        //clear All data first
        runBlocking {
            repository.deleteAllReminders()
        }
    }
}