package com.udacity.LocationReminder.saveReminders

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.getOrAwaitValue
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.EspressoIdlingResource
import com.udacity.util.DataBindingIdlingResource
import com.udacity.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.Is.`is`
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
import org.mockito.Mockito

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderListFragmentTest {

    private lateinit var viewModel: SaveReminderViewModel
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @Before
    fun init() {
        stopKoin()

        val myModule = module {
            viewModel {
                SaveReminderViewModel(
                    ApplicationProvider.getApplicationContext(),
                    get() as ReminderDataSource
                )
            }

            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(ApplicationProvider.getApplicationContext()) }
        }

        startKoin {
            androidContext(ApplicationProvider.getApplicationContext())
            modules(listOf(myModule))
        }

        viewModel = GlobalContext.get().koin.get()
    }

    @Before
    fun registerIdlingResources(): Unit {
        return IdlingRegistry.getInstance().run {
            register(EspressoIdlingResource.countingIdlingResource)
            register(dataBindingIdlingResource)
        }
    }

    @After
    fun unRegisterIdlingResources(): Unit {
        return IdlingRegistry.getInstance().run {
            unregister(EspressoIdlingResource.countingIdlingResource)
            unregister(dataBindingIdlingResource)
        }
    }

    @Test
    fun titleIsMissing() {
        val navController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.saveReminder)).perform(click())
    }

    private fun setReminderData(): ReminderDataItem {
        return ReminderDataItem("EGYPT", "Visit the Pyramids",
            "Pyramids", 29.9773, 31.1325)
    }

    @Test
    fun saveReminder_ValidData() {
        val navController = Mockito.mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)

        }
        onView(withId(R.id.reminderTitle)).perform(ViewActions.typeText(setReminderData().title))
        onView(withId(R.id.reminderDescription)).perform(ViewActions.typeText(setReminderData().description))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())

        assertThat(viewModel.showToast.getOrAwaitValue(), `is`("Failed to add reminder"))
    }
    @Test
    fun clickOnAddFab_navigatesToSaveReminderFragment() {
        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        Mockito.verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

}