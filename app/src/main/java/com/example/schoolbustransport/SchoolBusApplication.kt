package com.example.schoolbustransport

import android.app.Application
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

/**
 * SchoolBusApplication - Custom Application class for School Bus Transport System
 * 
 * This class is the entry point for the application and is responsible for:
 * - Initializing Hilt dependency injection (@HiltAndroidApp annotation)
 * - Setting up Firebase services (Analytics, Crashlytics, FCM if configured)
 * - Configuring global application settings
 * - Logging app lifecycle events
 * 
 * The @HiltAndroidApp annotation triggers Hilt's code generation,
 * creating the Application-level dependency injection component.
 * 
 * Firebase Integration:
 * - Firebase Analytics is initialized for tracking user behavior
 * - Analytics collection is enabled in debug builds for testing
 * - App open events are logged automatically
 * 
 * Note: This class must be declared in AndroidManifest.xml as the application name.
 */
@HiltAndroidApp
class SchoolBusApplication : Application() {

    private lateinit var analytics: FirebaseAnalytics

    /**
     * Called when the application is starting, before any activity is created.
     * 
     * Initialization order:
     * 1. Call super.onCreate() to initialize base Application
     * 2. Initialize Firebase services
     * 3. Configure analytics settings
     * 4. Log app initialization event
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID

        // Initialize Firebase SDK
        FirebaseApp.initializeApp(this)

        // Initialize Firebase Analytics using KTX extensions
        analytics = Firebase.analytics

        // Enable Analytics collection in debug builds
        if (BuildConfig.DEBUG) {
            analytics.setAnalyticsCollectionEnabled(true)
        }

        // Log app opened event with metadata using the modern KTX API
        val bundle = Bundle().apply {
            putLong(FirebaseAnalytics.Param.SUCCESS, 1L)
            putString("app_version", BuildConfig.VERSION_NAME)
            putLong("timestamp", System.currentTimeMillis())
        }
        analytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle)

        Log.d(TAG, "App initialized - Firebase Analytics ready")
    }

    companion object {
        /**
         * Log tag for debugging and logging purposes
         */
        private const val TAG = "SchoolBusApp"
    }
}
