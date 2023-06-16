package com.example.write.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import com.example.util.Destinations
import com.example.util.model.Mood
import com.example.util.observeWithLifecycle
import com.example.write.WriteScreen
import com.example.write.WriteScreenEvent
import com.example.write.WriteScreenSideEffect
import com.example.write.WriteViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.kiwi.navigationcompose.typed.composable
import com.stevdzasan.messagebar.rememberMessageBarState

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.write(navigateUp: () -> Unit) {
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
                viewModel.onEvent(WriteScreenEvent.OnForeGround(diaryId!!))
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