package com.udacity.util


import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingResource
import java.util.*

class DataBindingIdlingResource : IdlingResource {

    private val idlingCallback = mutableListOf<IdlingResource.ResourceCallback>()


    private val id = UUID.randomUUID().toString()

    private var wasNotIdle = false

    lateinit var activity: FragmentActivity


    override fun getName(): String = "DataBinding $id"

    override fun isIdleNow(): Boolean {

        val idle = !getBinding().any { it.hasPendingBindings() }
        @Suppress("LiftReturnOrAssignment")
        if (idle) {
            if (wasNotIdle) {
                // notify observers to avoid espresso race detector
                idlingCallback.forEach { it.onTransitionToIdle() }
            }
            wasNotIdle = false
        } else {
            wasNotIdle = true
            // check next frame
            activity.findViewById<View>(android.R.id.content).postDelayed({
                isIdleNow
            }, 16)
        }
        return idle
    }


    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {

        idlingCallback.add(callback!!)
    }

    private fun getBinding(): List<ViewDataBinding> {
        val fragment = (activity as? FragmentActivity)
            ?.supportFragmentManager
            ?.fragments

        val binding = fragment?.mapNotNull {
            it.view?.getBinding()
        }

        val childrenBinding = fragment?.flatMap {
            it.childFragmentManager.fragments
        }?.mapNotNull {
            it.view?.getBinding()
        }

        return binding!! + childrenBinding!!
    }

    private fun View.getBinding(): ViewDataBinding? = DataBindingUtil.getBinding(this)

}

fun DataBindingIdlingResource.monitorActivity(activityScenario: ActivityScenario<out FragmentActivity>) {
    activityScenario.onActivity {
        this.activity = it
    }
}

fun DataBindingIdlingResource.monitorFragment(activityScenario: FragmentScenario<out Fragment>) {
    activityScenario.onFragment {
        this.activity = it.requireActivity()
    }
}