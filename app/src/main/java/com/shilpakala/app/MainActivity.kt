package com.shilpakala.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shilpakala.app.data.Routes
import com.shilpakala.app.ui.gallery.GalleryScreen
import com.shilpakala.app.ui.home.HomeScreen
import com.shilpakala.app.ui.studio.StudioScreen
import com.shilpakala.app.ui.theme.ShilpaKalaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShilpaKalaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Routes.Home
                    ) {
                        composable(Routes.Home) {
                            HomeScreen(
                                onOpenStudio = { navController.navigate(Routes.Studio) },
                                onMyGallery = { navController.navigate(Routes.Gallery) }
                            )
                        }
                        composable(Routes.Studio) {
                            StudioScreen(onBack = { navController.popBackStack() })
                        }
                        composable(Routes.Gallery) {
                            GalleryScreen(
                                onBack = { navController.popBackStack() },
                                onOpenStudio = { navController.navigate(Routes.Studio) }
                            )
                        }
                    }
                }
            }
        }
    }
}
