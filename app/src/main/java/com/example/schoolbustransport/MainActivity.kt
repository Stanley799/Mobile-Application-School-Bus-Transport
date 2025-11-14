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
import com.example.schoolbustransport.presentation.dashboard.AdminPanelScreen
import com.example.schoolbustransport.presentation.dashboard.ChatScreen
import com.example.schoolbustransport.presentation.dashboard.DashboardScreen
import com.example.schoolbustransport.presentation.dashboard.ManageStudentsScreen
import com.example.schoolbustransport.presentation.dashboard.MessagesScreen
import com.example.schoolbustransport.presentation.dashboard.NewMessageScreen
import com.example.schoolbustransport.presentation.notifications.NotificationsScreen
import com.example.schoolbustransport.presentation.profile.ProfileScreen
import com.example.schoolbustransport.presentation.schedule.ScheduleScreen
import com.example.schoolbustransport.presentation.trip.AttendanceScreen
import com.example.schoolbustransport.presentation.trip.LiveTrackingScreen
import com.example.schoolbustransport.presentation.trip.TripListScreen
import com.example.schoolbustransport.ui.theme.SchoolBusTransportTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SchoolBusTransportTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val loginState by authViewModel.loginState.collectAsStateWithLifecycle()

                when (val state = loginState) {
                    is LoginState.Success -> {
                        AppNavigation(user = state.user)
                    }
                    is LoginState.Loading -> {
                        LoadingScreen()
                    }
                    else -> {
                        LoginScreen(viewModel = authViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(user: com.example.schoolbustransport.domain.model.User) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(user = user, navController = navController)
        }
        composable(
            route = "live_tracking?tripId={tripId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId").takeUnless { it.isNullOrBlank() }
            LiveTrackingScreen(navController = navController, tripId = tripId)
        }
        composable("trips_list") {
            TripListScreen(navController = navController)
        }
        composable("messages") {
            MessagesScreen(navController = navController)
        }
        composable("new_message") {
            NewMessageScreen(navController = navController)
        }
        composable("chat/{conversationId}") { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
            ChatScreen(navController = navController, conversationId = conversationId)
        }
        composable(
            route = "attendance/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            AttendanceScreen(navController = navController, tripId = tripId)
        }
        composable("admin_panel") {
            AdminPanelScreen(navController = navController)
        }
        composable("schedule") {
            ScheduleScreen()
        }
        composable("notifications") {
            NotificationsScreen()
        }
        composable("profile") {
            ProfileScreen()
        }
        composable("manage_students") {
            ManageStudentsScreen()
        }
        composable(
            route = "trip_report/{tripId}",
            arguments = listOf(
                navArgument("tripId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            com.example.schoolbustransport.presentation.trip.TripReportScreen(tripId = tripId, navController = navController)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
