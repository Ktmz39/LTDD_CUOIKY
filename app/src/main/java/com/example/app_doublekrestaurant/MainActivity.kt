package com.example.app_doublekrestaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.app_doublekrestaurant.data.model.UserRole
import com.example.app_doublekrestaurant.navigation.AppNavigation
import com.example.app_doublekrestaurant.navigation.Screen
import com.example.app_doublekrestaurant.ui.auth.AuthViewModel
import com.example.app_doublekrestaurant.ui.theme.DoubleKRestaurantTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoubleKRestaurantTheme {
                val uiState by authViewModel.uiState.collectAsState()
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Smart start destination logic
                    if (uiState.isLoggedIn) {
                        // User is logged in, wait for role to be fetched
                        if (uiState.role != null) {
                            val startDest = if (uiState.role == UserRole.ADMIN || uiState.role == UserRole.STAFF) {
                                Screen.AdminDashboard.route
                            } else {
                                Screen.UserHome.route
                            }
                            AppNavigation(navController = navController, startDestination = startDest)
                        } else {
                            // Fetching role...
                            SplashScreen()
                        }
                    } else {
                        // Not logged in or state not yet determined
                        // If we are sure it's not logged in (e.g. after a short timeout or explicit null), show Login
                        // For now, let's show login as default if not explicitly logged in
                        AppNavigation(navController = navController, startDestination = Screen.Login.route)
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Color(0xFFAC2D00), modifier = Modifier.size(48.dp))
    }
}