package com.example.deardiary.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.deardiary.data.database.ImageDatabase
import com.example.deardiary.data.database.ImageToDeleteDao
import com.example.deardiary.data.database.ImageToUploadDao
import com.example.deardiary.presentation.navigation.Destinations
import com.example.deardiary.presentation.navigation.NavGraph
import com.example.deardiary.presentation.ui.theme.DearDiaryTheme
import com.example.deardiary.util.Constants.APP_ID
import com.example.deardiary.util.retryImageToDeleteFromFirebase
import com.example.deardiary.util.retryImageUploadToFirebase
import com.google.firebase.FirebaseApp
import com.kiwi.navigationcompose.typed.createRoutePattern
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var imagesDatabase: ImageDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this)
        setContent {
            DearDiaryTheme {
                NavGraph(
                    startDestination = getStartDestination(),
                    navHostController = rememberNavController()
                )
            }
        }

        cleanUpCheck(
            imageToUploadDao = imagesDatabase.imageToUploadDao(),
            imageToDeleteDao = imagesDatabase.imageToDeleteDao(),
            scope = lifecycleScope
        )
    }

    private fun cleanUpCheck(
        imageToUploadDao: ImageToUploadDao,
        imageToDeleteDao: ImageToDeleteDao,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            val imagesToUpload = imageToUploadDao.getImagesToUpload()
            imagesToUpload.forEach { imageToUpload ->
                launch {
                    retryImageUploadToFirebase(
                        imageToUpload = imageToUpload,
                        coroutineScope = scope,
                        onSuccess = { imageToUploadDao.removeImage(imageToUpload.id) }
                    )
                }
            }

            val imagesToDelete = imageToDeleteDao.getImagesToDelete()
            imagesToDelete.forEach { imageToDelete ->
                launch {
                    retryImageToDeleteFromFirebase(
                        imageToDelete = imageToDelete,
                        coroutineScope = scope,
                        onSuccess = { imageToDeleteDao.removeImage(imageToDelete.id) }
                    )
                }
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
