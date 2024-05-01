package com.example.uptmsmartcampus

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber
import com.google.firebase.firestore.FirebaseFirestore


class SettingsFragment : Fragment() {

    private var themePosition: Int? = null
    private var arrayTheme: Array<String>? = null

    // Initialize Firebase Authentication
    private val auth = FirebaseAuth.getInstance()

    // Initialize Firebase Firestore
    private val db = FirebaseFirestore.getInstance()

    private lateinit var btnEditInfo: Button
    private lateinit var textViewStudentId: TextView
    private lateinit var textViewICNumber: TextView

    private lateinit var btnSwitch: Button

    private val userPrefs: UserPreferencesRepository by lazy {
        MyApp.instance.userPreferences
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateTitle("Settings")

        btnSwitch = view.findViewById(R.id.btnSwitch)

        initTheme()

        btnSwitch.setOnClickListener {
            showThemeDialog()
        }

        btnEditInfo = view.findViewById(R.id.btnEditInfo)

        btnEditInfo.setOnClickListener {
            showEditDialog()
        }

        // Initialize TextViews
        textViewStudentId = view.findViewById(R.id.textViewStudentId)
        textViewICNumber = view.findViewById(R.id.textViewICNumber)

        // Retrieve and display data from Firebase Firestore
        retrieveDataFromFirebase()
    }

    private fun initTheme() {
        Timber.tag(TAG).d("initTheme")

        arrayTheme = resources.getStringArray(R.array.themes)

        themePosition = when (userPrefs.appTheme) {
            Theme.LIGHT_MODE -> 0
            Theme.DARK_MODE -> 1
            else -> 2
        }

        btnSwitch.text = arrayTheme!![themePosition!!]
    }

    private fun setTheme() {
        Timber.tag(TAG).d("setTheme")

        btnSwitch.text = arrayTheme!![themePosition!!]

        Timber.tag(TAG).d("Theme ${arrayTheme!![themePosition!!]}")

        userPrefs.updateTheme(
            when (themePosition) {
                0 -> Theme.LIGHT_MODE
                1 -> Theme.DARK_MODE
                else -> Theme.FOLLOW_SYSTEM
            }
        )
    }

    private fun showThemeDialog() {
        Timber.tag(TAG).d("showThemeDialog")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.label_select_theme)
            .setSingleChoiceItems(R.array.themes, themePosition!!) { _, i ->
                themePosition = i
                Timber.tag(TAG).d("Theme selected Pos : $themePosition")
                setTheme()
            }.show()
    }

    companion object {
        private val TAG: String =
            GLOBAL_TAG + " " + SettingsFragment::class.java.simpleName
    }

    private fun updateTitle(title: String) {
        // Set the title in the toolbar
        requireActivity().title = title
    }

    private fun saveDataToFirebase(newStudentId: String, newICNumber: String) {
        // Check if fields are not empty
        if (newStudentId.isNotEmpty() && newICNumber.isNotEmpty()) {
            // Check if user is authenticated
            auth.currentUser?.let { user ->
                // Create a HashMap to store data
                val data = hashMapOf(
                    "studentId" to newStudentId,
                    "icNumber" to newICNumber
                )

                // Add data to Firebase Firestore with the UID of the authenticated user
                db.collection("users")
                    .document(user.uid) // Use UID of the authenticated user as document ID
                    .set(data)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Data saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding document", e)
                        Toast.makeText(requireContext(), "Failed to save data", Toast.LENGTH_SHORT)
                            .show()
                    }
            } ?: run {
                // User not authenticated, handle accordingly
                Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }


    private fun retrieveDataFromFirebase() {
        // Check if user is authenticated
        auth.currentUser?.let { user ->
            // Retrieve data from Firestore for the authenticated user
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Document exists, retrieve data
                        val studentId = document.getString("studentId")
                        val icNumber = document.getString("icNumber")

                        // Update TextViews with retrieved data
                        textViewStudentId.setText(studentId)
                        textViewICNumber.setText(icNumber)
                    } else {
                        // Document does not exist
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting document", exception)
                }
        }
    }

    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_info, null)
        val editTextNewStudentId = dialogView.findViewById<EditText>(R.id.editTextNewStudentId)
        val editTextNewICNumber = dialogView.findViewById<EditText>(R.id.editTextNewICNumber)

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Info")
            .setPositiveButton("Save") { dialog, _ ->
                val newStudentId = editTextNewStudentId.text.toString().trim()
                val newICNumber = editTextNewICNumber.text.toString().trim()

                // Save data to Firebase Firestore
                saveDataToFirebase(newStudentId, newICNumber)

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.create().show()
    }

}