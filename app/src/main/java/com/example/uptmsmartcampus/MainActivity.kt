package com.example.uptmsmartcampus

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.uptmsmartcampus.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private var oneTapClient: SignInClient? = null
    private lateinit var signInRequest: BeginSignInRequest

    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                try {
                    val credential = oneTapClient!!.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        // Sign-in successful, save authentication state
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    saveAuthenticationState(true)
                                    // Retrieve user information
                                    val currentUser = auth.currentUser
                                    val email = currentUser?.email
                                    val displayName = currentUser?.displayName
                                    val photoUrl = currentUser?.photoUrl?.toString()
                                    // Save user info
                                    saveUserInfo(displayName, email, photoUrl)
                                    showToast("Sign in Complete\n" + "Signed in as $displayName ($email)")
                                    // Start MainActivity2
                                    startActivity(Intent(this, MainActivity2::class.java))
                                } else {
                                    // Handle unsuccessful sign-in
                                    showToast("Sign-in failed. Please Use UPTM Student Email ${task.exception?.message}")
                                    saveAuthenticationState(false)
                                }
                            }
                    }
                } catch (e: ApiException) {
                    e.printStackTrace()
                    showToast("Google Sign-in failed due to an unknown error. Please try again later.")
                    saveAuthenticationState(false)
                }

            }
        }


                private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        oneTapClient = Identity.getSignInClient(this)
        sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)

        // Check if the user is already authenticated
        if (isUserAuthenticated()) {
            // If user is authenticated, launch MainActivity2 directly
            startActivity(Intent(this, MainActivity2::class.java))
            finish() // Finish MainActivity to prevent going back to it when pressing back
            return
        }

        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(true)
                    .build()
            )
            .build()

        binding.signInButton.setOnClickListener {
            // Check if the user is already authenticated
            if (!isUserAuthenticated()) {
                CoroutineScope(Dispatchers.Main).launch {
                    signIn()
                }
            } else {
                // User is already authenticated, navigate to MainActivity2 directly
                startActivity(Intent(this, MainActivity2::class.java))
            }
        }
        val btnUserManual = findViewById<ImageView>(R.id.userManualButton)
        btnUserManual.setOnClickListener {
            navigateToUserManual()
        }
    }

    private suspend fun signIn() {
        val result = oneTapClient?.beginSignIn(signInRequest)?.await()
        val intentSenderRequest = IntentSenderRequest.Builder(result!!.pendingIntent).build()
        activityResultLauncher.launch(intentSenderRequest)
    }

    private fun saveAuthenticationState(isLoggedIn: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isLoggedIn", isLoggedIn).apply()
    }

    private fun saveUserInfo(displayName: String?, email: String?, photoUrl: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("displayName", displayName)
        editor.putString("email", email)
        editor.putString("photoUrl", photoUrl)
        editor.apply()
    }

    private fun isUserAuthenticated(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }


    private fun navigateToUserManual() {
        val intent = Intent(this, UserManualFragment::class.java)
        startActivity(intent)
    }

}
