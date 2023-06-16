package com.example.home.navigation

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import com.example.home.HomeScreen
import com.example.home.HomeScreenDialogState
import com.example.home.HomeScreenEvent
import com.example.home.HomeScreenSideEffect
import com.example.home.HomeViewModel
import com.example.home.R
import com.example.ui.components.DisplayAlertDialog
import com.example.util.Destinations
import com.example.util.observeWithLifecycle
import com.kiwi.navigationcompose.typed.composable
import kotlinx.coroutines.launch

fun NavGraphBuilder.home(navigateToWrite: (String?) -> Unit, navigateToAuth: () -> Unit) {
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
