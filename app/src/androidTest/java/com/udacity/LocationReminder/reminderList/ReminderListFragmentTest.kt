package com.udacity.LocationReminder.reminderList

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.util.DataBindingIdlingResource
import com.udacity.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {


    private lateinit var viewModel: RemindersListViewModel
    private lateinit var repository: ReminderDataSource
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun init() {
        stopKoin()

        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(getApplicationContext()) }
        }

        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(myModule))
        }

        repository = GlobalContext.get().koin.get()
        viewModel = GlobalContext.get().koin.get()


        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Before
    fun registerIdlingResources() {
        return IdlingRegistry.getInstance().run {
            register(EspressoIdlingResource.countingIdlingResource)
            register(dataBindingIdlingResource)
        }
    }

    @After
    fun unRegisterIdlingResources() {
        return IdlingRegistry.getInstance().run {
            unregister(EspressoIdlingResource.countingIdlingResource)
            unregister(dataBindingIdlingResource)
        }
    }


    private fun setReminderData(): ReminderDTO {
        return ReminderDTO("EGYPT", "Visit the Pyramids",
            "Pyramids", 29.9773, 31.1325)
    }

    @Test
    fun setupUI_of_reminderList() {
        runBlocking {
            repository.saveReminder(setReminderData())
        }
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!, navController) }
        dataBindingIdlingResource.monitorFragment(scenario)


        onView(withText(R.string.no_data)).check(matches(not(isDisplayed())))

        onView(withText(setReminderData().title)).check(matches(isDisplayed()))
        onView(withText(setReminderData().description)).check(matches(isDisplayed()))
        onView(withText(setReminderData().location)).check(matches(isDisplayed()))
    }


    @Test
    fun noReminders_shows_noData() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))

        Thread.sleep(3000)
    }

}

