package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)

        firebaseAuth = FirebaseAuth.getInstance()
        checkingCurrentUser(firebaseAuth)

        binding.loginBtn.setOnClickListener { launchingSignFlow() }
    }

    private fun checkingCurrentUser(firebaseAuth: FirebaseAuth) {
        if (firebaseAuth.currentUser != null) {
            navigateToReminderActivity()
        }
    }

    private fun navigateToReminderActivity() {
        startActivity(Intent(this, RemindersActivity::class.java))
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SING_IN_REQUEST_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG,
                    "Sign in Successfully ${FirebaseAuth.getInstance().currentUser?.displayName} ")
                navigateToReminderActivity()
            } else
                Log.d(TAG, "Sign in Failed ${response?.error?.errorCode} ")
        }

    }

    private fun launchingSignFlow() {

        val provider = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(provider)
                .build(),
            SING_IN_REQUEST_CODE
        )
    }

    companion object {
        const val TAG = "AuthActivity"
        const val SING_IN_REQUEST_CODE = 1001
    }
}
