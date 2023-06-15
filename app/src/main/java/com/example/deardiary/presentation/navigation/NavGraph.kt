@file:OptIn(
    ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class
)

package com.example.deardiary.presentation.navigation

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.deardiary.R
import com.example.deardiary.domain.model.Mood
import com.example.deardiary.presentation.components.DisplayAlertDialog
import com.example.deardiary.presentation.screens.authentication.AuthScreen
import com.example.deardiary.presentation.screens.authentication.AuthScreenEvent
import com.example.deardiary.presentation.screens.authentication.AuthScreenSideEffect
import com.example.deardiary.presentation.screens.authentication.AuthViewModel
import com.example.deardiary.presentation.screens.home.HomeScreen
import com.example.deardiary.presentation.screens.home.HomeScreenDialogState
import com.example.deardiary.presentation.screens.home.HomeScreenEvent
import com.example.deardiary.presentation.screens.home.HomeScreenSideEffect
import com.example.deardiary.presentation.screens.home.HomeViewModel
import com.example.deardiary.presentation.screens.write.WriteScreen
import com.example.deardiary.presentation.screens.write.WriteScreenEvent
import com.example.deardiary.presentation.screens.write.WriteScreenSideEffect
import com.example.deardiary.presentation.screens.write.WriteViewModel
import com.example.deardiary.util.observeWithLifecycle
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.popUpTo
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.time.Duration.Companion.milliseconds

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

private fun NavGraphBuilder.authentication(navigateToHome: () -> Unit) {
    composable<Destinations.Authentication> {
        val viewModel: AuthViewModel = hiltViewModel()
        val oneTapSignInState = rememberOneTapSignInState()
        val loadingState = viewModel.loadingState.value || oneTapSignInState.opened
        val messageBarState = rememberMessageBarState()

        viewModel.sideEffect.observeWithLifecycle {
            when (it) {
                is AuthScreenSideEffect.Error -> messageBarState.addError(it.exception)
                is AuthScreenSideEffect.LoginFailed -> messageBarState.addError(Exception(it.message))
                is AuthScreenSideEffect.LoginSuccess -> {
                    messageBarState.addSuccess(it.message)
                    delay(1500.milliseconds)
                    navigateToHome()
                }
            }
        }

        AuthScreen(
            loading = loadingState,
            oneTapSignInState = oneTapSignInState,
            messageBarState = messageBarState,
            onClick = { oneTapSignInState.open() },
            onTokenReceived = { viewModel.onEvent(AuthScreenEvent.Login(it)) },
            onDialogDismissed = { messageBarState.addError(Exception(it)) }
        )
    }
}

private fun NavGraphBuilder.home(navigateToWrite: (String?) -> Unit, navigateToAuth: () -> Unit) {
    composable<Destinations.Home> {
        val context = LocalContext.current
        val viewModel: HomeViewModel = hiltViewModel()
        var dialogState by remember { mutableStateOf(HomeScreenDialogState.NONE) }
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val homeScreenState by viewModel.homeState.collectAsStateWithLifecycle()

        viewModel.sideEffect.observeWithLifecycle {
            when (it) {
                HomeScreenSideEffect.SignedOut -> navigateToAuth()
                HomeScreenSideEffect.DiariesDeleted -> scope.launch { drawerState.close() }
                is HomeScreenSideEffect.Error -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        HomeScreen(
            homeScreenState = homeScreenState,
            drawerState = drawerState,
            signingOut = viewModel.signingOut.value,
            deletingDiaries = viewModel.deletingDiaries.value,
            dateSelected = viewModel.dateFilterAsFlow.value != null,
            onSignOutClick = { dialogState = HomeScreenDialogState.SIGN_OUT },
            onDeleteAllClick = { dialogState = HomeScreenDialogState.DELETE_DIARIES },
            onMenuClick = { scope.launch { drawerState.open() } },
            onDateSelected = { viewModel.onEvent(HomeScreenEvent.OnDateSelected(it)) },
            onDateReset = { viewModel.onEvent(HomeScreenEvent.OnDateSelected(null)) },
            navigateToWrite = navigateToWrite
        )

        when (dialogState) {
            HomeScreenDialogState.NONE -> {}
            HomeScreenDialogState.SIGN_OUT -> {
                DisplayAlertDialog(
                    title = stringResource(id = R.string.sign_out),
                    message = stringResource(id = R.string.sign_out_prompt),
                    closeDialog = { dialogState = HomeScreenDialogState.NONE },
                    onPositiveButtonClick = { viewModel.onEvent(HomeScreenEvent.SignOut) }
                )
            }

            HomeScreenDialogState.DELETE_DIARIES -> {
                DisplayAlertDialog(
                    title = stringResource(id = R.string.delete_all_diaries),
                    message = stringResource(id = R.string.delete_all_diaries_prompt),
                    closeDialog = { dialogState = HomeScreenDialogState.NONE },
                    onPositiveButtonClick = { viewModel.onEvent(HomeScreenEvent.DeleteAllDiaries) }
                )
            }
        }
    }
}

private fun NavGraphBuilder.write(navigateUp: () -> Unit) {
    composable<Destinations.Write> {
        val viewModel: WriteViewModel = hiltViewModel()
        val pagerState = rememberPagerState()
        val messageBarState = rememberMessageBarState()
        val uiState = viewModel.uiState
        val galleryState = viewModel.galleryState
        val allMoods = remember { Mood.values() }
        val moodName =
            remember(pagerState.currentPage) { allMoods[pagerState.currentPage].name.uppercase() }

        viewModel.sideEffect.observeWithLifecycle {
            when (it) {
                is WriteScreenSideEffect.SaveFailure -> messageBarState.addError(Exception(it.message))
                is WriteScreenSideEffect.DeleteFailure -> messageBarState.addError(Exception(it.message))
                WriteScreenSideEffect.SaveSuccess -> navigateUp()
                WriteScreenSideEffect.DeleteSuccess -> navigateUp()
            }
        }

        LaunchedEffect(key1 = Unit) {
            if (diaryId != null) {
                viewModel.onEvent(WriteScreenEvent.OnForeGround(diaryId))
            }
        }

        WriteScreen(
            writeScreenState = uiState,
            pagerState = pagerState,
            messageBarState = messageBarState,
            galleryState = galleryState,
            moodName = moodName,
            onTitleChanged = { viewModel.onEvent(WriteScreenEvent.OnTitleChanged(it)) },
            onDescriptionChanged = { viewModel.onEvent(WriteScreenEvent.OnDescriptionChanged(it)) },
            onDateSelected = { viewModel.onEvent(WriteScreenEvent.OnDateChanged(it)) },
            onTimeSelected = { viewModel.onEvent(WriteScreenEvent.OnTimeChanged(it)) },
            onCloseIconClick = { viewModel.onEvent((WriteScreenEvent.ResetDate)) },
            onImagesSelected = { viewModel.onEvent(WriteScreenEvent.OnImagesSelected(it)) },
            onRemoveImage = { viewModel.onEvent(WriteScreenEvent.OnRemoveImage(it)) },
            onSaveClick = {
                viewModel.onEvent(WriteScreenEvent.OnMoodChanged(allMoods[pagerState.currentPage]))
                viewModel.onEvent(WriteScreenEvent.OnSaveClick)
            },
            onDeleteConfirmed = { viewModel.onEvent(WriteScreenEvent.OnDelete) },
            navigateUp = navigateUp
        )
    }
}