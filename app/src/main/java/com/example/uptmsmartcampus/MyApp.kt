package com.example.uptmsmartcampus

import android.app.Application
import com.google.firebase.BuildConfig
import timber.log.Timber

class MyApp : Application() {

    lateinit var userPreferences: UserPreferencesRepository

    companion object {
        private val TAG: String =
            GLOBAL_TAG + " " + MyApp::class.java.simpleName
        @get:Synchronized
        lateinit var instance: MyApp
        private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this


        userPreferences = UserPreferencesRepository.getInstance(this)


        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val theme = userPreferences.appTheme
        ThemeChanger().invoke(theme)

    }
}