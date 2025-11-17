package com.example.schoolbustransport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.schoolbustransport.domain.model.User
import com.example.schoolbustransport.presentation.auth.*
import com.example.schoolbustransport.presentation.dashboard.AdminPanelScreen
import com.example.schoolbustransport.presentation.dashboard.ChatScreen
import com.example.schoolbustransport.presentation.dashboard.DashboardScreen
import com.example.schoolbustransport.presentation.dashboard.AddStudentScreen
import com.example.schoolbustransport.presentation.dashboard.AdminStudentsScreen
import com.example.schoolbustransport.presentation.dashboard.DriverMyTripScreen
import com.example.schoolbustransport.presentation.dashboard.ManageStudentsScreen
import com.example.schoolbustransport.presentation.dashboard.MessagesScreen
import com.example.schoolbustransport.presentation.dashboard.NewMessageScreen
import com.example.schoolbustransport.presentation.dashboard.TripReportsListScreen
import com.example.schoolbustransport.presentation.dashboard.ViewMyStudentsScreen
import com.example.schoolbustransport.presentation.notifications.NotificationsScreen
import com.example.schoolbustransport.presentation.profile.ProfileScreen
import com.example.schoolbustransport.presentation.schedule.*
import com.example.schoolbustransport.presentation.trip.AttendanceScreen
import com.example.schoolbustransport.presentation.trip.LiveTrackingScreen
import com.example.schoolbustransport.presentation.trip.TripListScreen
import com.example.schoolbustransport.presentation.trip.TripReportScreen
import com.example.schoolbustransport.ui.theme.SchoolBusTransportTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
        setContent {
            SchoolBusTransportTheme {
                // Notification permission (Android 13+)
                val notificationPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    com.google.accompanist.permissions.rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
                } else null

                LaunchedEffect(notificationPermission) {
                    if (notificationPermission != null) {
                        notificationPermission.launchPermissionRequest()
                    }
                }

                val authViewModel: AuthViewModel = hiltViewModel()
                val loginState by authViewModel.loginState.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                if (loginState is LoginState.Loading) {
                    LoadingScreen()
                } else {
                    AppNavHost(
                        navController = navController,
                        loginState = loginState,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    loginState: LoginState,
    authViewModel: AuthViewModel
) {
    val startDestination = if (loginState is LoginState.Success) {
        when {
            loginState.user.role.isBlank() -> "select_role_flow"
            loginState.user.phone.isNullOrBlank() -> "complete_profile_flow"
            else -> "app"
        }
    } else {
        "auth"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        navigation(startDestination = "login", route = "auth") {
            composable("login") {
                LoginScreen(navController = navController, viewModel = authViewModel)
            }
            composable("signup") {
                SignUpScreen(navController = navController, viewModel = authViewModel)
            }
        }

        navigation(startDestination = "select_role", route = "select_role_flow") {
            composable("select_role") {
                SelectRoleScreen(navController = navController, viewModel = authViewModel)
            }
        }

        navigation(startDestination = "complete_profile", route = "complete_profile_flow") {
            composable("complete_profile") {
                CompleteProfileScreen(navController = navController, viewModel = authViewModel)
            }
        }

        navigation(startDestination = "dashboard", route = "app") {
            val user = (loginState as? LoginState.Success)?.user
            if (user != null) {
                addAppDestinations(navController, user, authViewModel)
            }
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            when {
                loginState.user.role.isBlank() -> {
                    navController.navigate("select_role_flow") { popUpTo("auth") { inclusive = true } }
                }
                loginState.user.phone.isNullOrBlank() -> {
                    navController.navigate("complete_profile_flow") { popUpTo("select_role_flow") { inclusive = true } }
                }
                else -> {
                    navController.navigate("app") { 
                        popUpTo("auth") { inclusive = true }
                        popUpTo("select_role_flow") { inclusive = true }
                        popUpTo("complete_profile_flow") { inclusive = true }
                    }
                }
            }
        } else if (loginState is LoginState.Idle || loginState is LoginState.Error) {
            navController.navigate("auth") {
                popUpTo("app") { inclusive = true }
                popUpTo("select_role_flow") { inclusive = true }
                popUpTo("complete_profile_flow") { inclusive = true }
            }
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.addAppDestinations(
    navController: NavHostController, 
    user: User, 
    authViewModel: AuthViewModel
) {
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
        ScheduleScreen(navController = navController)
    }
    composable("notifications") {
        NotificationsScreen(navController = navController)
    }
    composable("profile") {
        ProfileScreen(navController = navController, authViewModel = authViewModel)
    }
    composable("add_student") {
        AddStudentScreen(navController = navController)
    }
    composable("view_my_students") {
        ViewMyStudentsScreen(navController = navController)
    }
    composable("trip_reports") {
        TripReportsListScreen(navController = navController)
    }
    composable("manage_students") {
        AdminStudentsScreen(navController = navController)
    }
    composable("manage_trips") {
        ManageTripsScreen(navController = navController)
    }
    composable("manage_buses") {
        ManageBusesScreen(navController = navController)
    }
    composable("manage_routes") {
        ManageRoutesScreen(navController = navController)
    }
    composable("manage_drivers") {
        ManageDriversScreen(navController = navController)
    }
    composable(
        route = "trip_report/{tripId}",
        arguments = listOf(
            navArgument("tripId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
        TripReportScreen(tripId = tripId, navController = navController)
    }
    composable("driver_my_trip") {
        DriverMyTripScreen(navController = navController)
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
