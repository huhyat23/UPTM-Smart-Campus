package com.example.uptmsmartcampus

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.example.uptmsmartcampus.databinding.ActivityMain2Binding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity2 : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)

        // Retrieve user info from SharedPreferences
        val displayName = sharedPreferences.getString("displayName", null)
        val email = sharedPreferences.getString("email", null)
        val photoUrl = sharedPreferences.getString("photoUrl", null)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        drawerLayout = binding.drawerLayout
        setSupportActionBar(binding.toolbar)

        setupDrawerToggle()

        if (displayName == null || email == null || photoUrl == null) {
            // If user info is not available, navigate back to MainActivity for authentication
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
            return
        }

        // Set the title and user information in the navigation drawer header
        setNavigationDrawerHeader(displayName, email, photoUrl)

        binding.navView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
            updateTitle("Home")
            binding.navView.setCheckedItem(R.id.nav_home)
        }
        // Handling click event for btnUserManual
        val btnUserManual = findViewById<Button>(R.id.btnUserManual)
        btnUserManual.setOnClickListener {
            navigateToUserManual()
        }
    }

    private fun setupDrawerToggle() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                updateTitle("Home")
                replaceFragment(HomeFragment())
            }
            R.id.nav_profile -> {
                updateTitle("My Profile")
                replaceFragment(ProfileFragment())
            }
            R.id.nav_settings -> {
                updateTitle("Settings")
                replaceFragment(SettingsFragment())
            }
            R.id.nav_about -> {
                updateTitle("About")
                replaceFragment(AboutFragment())
            }
            R.id.nav_logout -> {
                showToast("Logout Successfully")
                logoutUser()
            }
        }

        // Close the drawer after handling the item click
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun updateTitle(title: String) {
        // Set the title in the toolbar
        supportActionBar?.title = title
    }

    private fun setNavigationDrawerHeader(displayName: String?, email: String?, photoUrl: String?) {
        val headerView = binding.navView.getHeaderView(0)

        val headerName = headerView.findViewById<TextView>(R.id.textViewName)
        headerName.text = displayName

        val headerEmail = headerView.findViewById<TextView>(R.id.textViewEmail)
        headerEmail.text = email

        val headerImage = headerView.findViewById<ImageView>(R.id.imageViewProfile)

        // Load user image using Glide with a placeholder
        Glide.with(this)
            .load(photoUrl)
            .circleCrop()
            .placeholder(R.drawable.uptm) // Set default image resource
            .error(R.drawable.uptm)
            .into(headerImage)
    }

    private fun replaceFragment(fragment: Fragment) {
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }


    private fun logoutUser() {
        val sharedPreferences = getSharedPreferences("AuthPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Navigate back to the login screen
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment !is HomeFragment) {
                updateTitle("Home")
                replaceFragment(HomeFragment())
                binding.navView.setCheckedItem(R.id.nav_home)
            } else {
                super.onBackPressed()
            }
        }
    }
    private fun navigateToUserManual() {
        val intent = Intent(this, UserManualFragment::class.java)
        startActivity(intent)
    }
}
