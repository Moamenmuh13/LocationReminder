package com.udacity.project4.locationreminders.reminderslist

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import org.koin.android.ext.android.bind
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var firebaseAuthUI: AuthUI
    private lateinit var binding: FragmentRemindersBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        firebaseAuthUI = AuthUI.getInstance()
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
            binding.refreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }
//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }


    private fun userSignOut(it: Task<Void>) {
        val alertDialog = AlertDialog.Builder(requireContext())
        val builder = alertDialog.setTitle("Are you sure you wants to leave")
            .setMessage("If you  logged out all your data will erase")
            .setPositiveButton("Exit") { _, _ ->
                logout(it)
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                Snackbar.make(binding.reminderListFragment,
                    "Thanks for staying ❤",
                    Snackbar.LENGTH_INDEFINITE).show()
                dialogInterface.dismiss()
            }
        builder.show()

    }

    private fun logout(it: Task<Void>) {
        if (it.isSuccessful) {
            Toast.makeText(
                requireContext(),
                "We will miss you try to come back again 😥 👋 ",
                Toast.LENGTH_LONG
            ).show()
            startActivity(Intent(requireActivity(), AuthenticationActivity::class.java))
            _viewModel.removeAllReminders()
            requireActivity().finish()
        } else {
            Snackbar.make(requireView(),
                getString(R.string.logout_failed),
                Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                firebaseAuthUI.signOut(requireContext())
                    .addOnCompleteListener {
                        userSignOut(it)
                    }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

}
