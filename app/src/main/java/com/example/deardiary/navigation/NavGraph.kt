@file:OptIn(
    ExperimentalSerializationApi::class
)

package com.example.deardiary.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.auth.navigation.authentication
import com.example.home.navigation.home
import com.example.util.Destinations
import com.example.write.navigation.write
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popUpTo
import kotlinx.serialization.ExperimentalSerializationApi

@Composable
fun NavGraph(startDestination: String, navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = startDestination) {
        authentication {
            navHostController.navigate(Destinations.Home) {
                popUpTo<Destinations.Authentication> { inclusive = true }
            }
        }

        home(
            navigateToWrite = { navHostController.navigate(Destinations.Write(it)) },
            navigateToAuth = {
                navHostController.navigate(Destinations.Authentication) {
                    popUpTo<Destinations.Home> { inclusive = true }
                }
            }
        )

        write(navigateUp = { navHostController.navigateUp() })
    }
}