package com.example.deardiary.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.deardiary.presentation.navigation.Destinations
import com.example.deardiary.presentation.navigation.NavGraph
import com.example.deardiary.presentation.ui.theme.DearDiaryTheme
import com.example.deardiary.util.Constants.APP_ID
import com.kiwi.navigationcompose.typed.createRoutePattern
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.serialization.ExperimentalSerializationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            DearDiaryTheme {
                NavGraph(
                    startDestination = getStartDestination(),
                    navHostController = rememberNavController()
                )
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun getStartDestination(): String {
        val currentUser = App.create(APP_ID).currentUser
        return if (currentUser != null && currentUser.loggedIn) createRoutePattern<Destinations.Home>()
        else createRoutePattern<Destinations.Authentication>()
    }
}
