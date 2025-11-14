package com.example.schoolbustransport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.schoolbustransport.presentation.auth.AuthViewModel
import com.example.schoolbustransport.presentation.auth.LoginScreen
import com.example.schoolbustransport.presentation.auth.LoginState
import com.example.schoolbustransport.presentation.dashboard.* // Import all dashboard screens
import com.example.schoolbustransport.presentation.trip.AttendanceScreen
import com.example.schoolbustransport.presentation.trip.LiveTrackingScreen
import com.example.schoolbustransport.presentation.trip.TripListScreen
import com.example.schoolbustransport.ui.theme.SchoolBusTransportTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity - Entry point of the School Bus Transport application
 * 
 * This activity serves as the root of the application and handles:
 * - Authentication state management using AuthViewModel
 * - Navigation setup based on user login status
 * - Initial screen routing (Login, Loading, or Dashboard)
 * 
 * Architecture:
 * - Uses Hilt for dependency injection (@AndroidEntryPoint)
 * - Jetpack Compose for UI
 * - Navigation Component for screen routing
 * - StateFlow for reactive authentication state
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	/**
	 * Called when the activity is first created.
	 * Sets up the Compose UI and observes authentication state.
	 */
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			SchoolBusTransportTheme {
				// Get AuthViewModel instance via Hilt dependency injection
				val authViewModel: AuthViewModel = hiltViewModel()
				// Observe login state changes with lifecycle awareness
				val loginState by authViewModel.loginState.collectAsStateWithLifecycle()

				// Route to appropriate screen based on authentication state
				when (val state = loginState) {
					is LoginState.Success -> {
						// User is authenticated - navigate to main app
						AppNavigation(user = state.user)
					}
					is LoginState.Loading -> {
						// Show loading indicator during authentication check
						LoadingScreen()
					}
					else -> {
						// User is not authenticated - show login screen
						LoginScreen(viewModel = authViewModel)
					}
				}
			}
		}
	}
}

/**
 * AppNavigation - Main navigation graph for authenticated users
 * 
 * Defines all navigation routes and their parameters:
 * - Dashboard: Role-based home screen
 * - Live Tracking: Real-time bus location tracking with optional tripId
 * - Trips List: View and manage trips (role-filtered by backend)
 * - Messages: Conversation list
 * - New Message: Screen to start a new conversation
 * - Chat: Individual conversation screen
 * - Attendance: Mark/view attendance for a specific trip
 * - Admin Panel: Admin-only trip scheduling interface
 * - Manage Students: Student management screen
 * 
 * @param user The authenticated user object containing role and profile info
 */
@Composable
fun AppNavigation(user: com.example.schoolbustransport.domain.model.User) {
	val navController = rememberNavController()
	NavHost(navController = navController, startDestination = "dashboard") {
		// Main dashboard - shows role-specific menu options
		composable("dashboard") {
			DashboardScreen(user = user, navController = navController)
		}
		
		// Live tracking screen with optional tripId parameter
		// tripId is used to join a specific trip's websocket room for real-time updates
		composable(
			route = "live_tracking?tripId={tripId}",
			arguments = listOf(
				navArgument("tripId") { type = NavType.StringType; defaultValue = "" }
			)
		) { backStackEntry ->
			val tripId = backStackEntry.arguments?.getString("tripId").takeUnless { it.isNullOrBlank() }
			LiveTrackingScreen(navController = navController, tripId = tripId)
		}
		
		// Trips list - displays trips filtered by user role (admin sees all, driver sees assigned, parent sees child's trips)
		composable("trips_list") {
			TripListScreen(navController = navController)
		}
		
		// Messages screen - shows list of conversations
		composable("messages") {
			MessagesScreen(navController = navController)
		}

		// New message screen
		composable("new_message") {
			NewMessageScreen(navController = navController)
		}

		// Individual chat screen for a specific conversation
		composable("chat/{conversationId}") { backStackEntry ->
			val conversationId = backStackEntry.arguments?.getString("conversationId")
			ChatScreen(navController = navController, conversationId = conversationId)
		}

		// Attendance screen - allows drivers to mark attendance, parents/admins can view only
		// Backend enforces role-based permissions for updates
		composable(
			route = "attendance/{tripId}",
			arguments = listOf(navArgument("tripId") { type = NavType.StringType })
		) { backStackEntry ->
			val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
			AttendanceScreen(navController = navController, tripId = tripId)
		}

		// Admin-only scheduling panel for creating and managing trips
		composable("admin_panel") {
			AdminPanelScreen(navController = navController)
		}

		// Schedule/Calendar screen
		composable("schedule") {
			com.example.schoolbustransport.presentation.schedule.ScheduleScreen()
		}

		// Notifications screen
		composable("notifications") {
			com.example.schoolbustransport.presentation.notifications.NotificationsScreen()
		}

		// Profile screen
		composable("profile") {
			com.example.schoolbustransport.presentation.profile.ProfileScreen(user = user)
		}

		// Student management screen
		composable("manage_students") {
			ManageStudentsScreen()
		}
	}
}

/**
 * LoadingScreen - Displays a loading indicator
 * 
 * Used during authentication state checks and other async operations
 * to provide visual feedback to the user.
 */
@Composable
fun LoadingScreen() {
	Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
		CircularProgressIndicator()
	}
}
