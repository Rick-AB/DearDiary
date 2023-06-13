@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPagerApi::class, ExperimentalPagerApi::class
)

package com.example.deardiary.presentation.screens.write

import android.net.Uri
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.deardiary.R
import com.example.deardiary.domain.model.GalleryImage
import com.example.deardiary.domain.model.GalleryState
import com.example.deardiary.domain.model.Mood
import com.example.deardiary.domain.model.rememberGalleryState
import com.example.deardiary.presentation.components.ClickableIcon
import com.example.deardiary.presentation.components.DisplayAlertDialog
import com.example.deardiary.presentation.components.Gallery
import com.example.deardiary.util.toInstant
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun WriteScreen(
    writeScreenState: WriteScreenState,
    pagerState: PagerState,
    messageBarState: MessageBarState,
    galleryState: GalleryState,
    moodName: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onCloseIconClick: () -> Unit,
    onImagesSelected: (List<Uri>) -> Unit,
    onRemoveImage: (GalleryImage) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    navigateUp: () -> Unit
) {
    var selectedGalleryImage by remember { mutableStateOf<GalleryImage?>(null) }

    LaunchedEffect(key1 = writeScreenState.mood) {
        pagerState.scrollToPage(writeScreenState.mood.ordinal)
    }

    Scaffold(topBar = {
        WriteTopAppBar(
            moodName = moodName,
            diaryId = writeScreenState.diaryId,
            diaryTitle = writeScreenState.title,
            initialDate = writeScreenState.initialDate?.toInstant() ?: Instant.now(),
            date = writeScreenState.date.toInstant(),
            onDateSelected = onDateSelected,
            onTimeSelected = onTimeSelected,
            onCloseIconClick = onCloseIconClick,
            onDeleteConfirmed = onDeleteConfirmed,
            onNavClick = navigateUp
        )
    }) {
        ContentWithMessageBar(messageBarState = messageBarState) {
            Box(modifier = Modifier.padding(it)) {
                WriteBody(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    pagerState = pagerState,
                    galleryState = galleryState,
                    title = writeScreenState.title,
                    description = writeScreenState.description,
                    onTitleChanged = onTitleChanged,
                    onDescriptionChanged = onDescriptionChanged,
                    onImagesSelected = onImagesSelected,
                    onImageClick = { selectedGalleryImage = it },
                    onSaveClick = onSaveClick
                )
            }
        }

        AnimatedVisibility(visible = selectedGalleryImage != null) {
            val closeImage: () -> Unit = { selectedGalleryImage = null }
            Dialog(onDismissRequest = closeImage) {
                if (selectedGalleryImage != null) {
                    ZoomableImage(
                        selectedGalleryImage = selectedGalleryImage!!,
                        onCloseClick = closeImage,
                        onDeleteClick = {
                            onRemoveImage(selectedGalleryImage!!)
                            closeImage()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WriteBody(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    galleryState: GalleryState,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageClick: (GalleryImage) -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val allMoods = remember { Mood.values() }

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            HorizontalPager(count = allMoods.size, state = pagerState) { page ->
                AsyncImage(
                    modifier = Modifier.size(120.dp),
                    model = ImageRequest.Builder(context)
                        .data(allMoods[page].icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Mood"
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = onTitleChanged,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified,
                    placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                ),
                placeholder = { Text(text = stringResource(id = R.string.title)) },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    scope.launch {
                        scrollState.animateScrollTo(Int.MAX_VALUE)
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                })
            )

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                onValueChange = onDescriptionChanged,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified,
                    placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                ),
                placeholder = { Text(text = stringResource(id = R.string.tell_me_about_it)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 24.dp)
                .fillMaxWidth()
        ) {
            Gallery(
                modifier = Modifier.align(Alignment.Start),
                state = galleryState,
                size = 60.dp,
                shape = Shapes().medium,
                editable = true,
                onAddIconClick = focusManager::clearFocus,
                onImagesSelected = onImagesSelected,
                onImageClick = onImageClick
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                enabled = title.isNotEmpty() && description.isNotEmpty(),
                onClick = onSaveClick,
                shape = Shapes().small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}

@Composable
fun WriteTopAppBar(
    diaryId: String?,
    diaryTitle: String,
    moodName: String,
    initialDate: Instant,
    date: Instant,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onCloseIconClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onNavClick: () -> Unit
) {
    val dateDialogState = rememberSheetState()
    val timeDialogState = rememberSheetState()
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }
    val showCloseIcon = remember(date) {
        initialDate.truncatedTo(ChronoUnit.MINUTES)
            .compareTo(date.truncatedTo(ChronoUnit.MINUTES)) != 0
    }

    CenterAlignedTopAppBar(title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = moodName, style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                )
            )

            Text(
                text = formatter.format(date),
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
            )
        }
    }, navigationIcon = {
        ClickableIcon(
            imageVector = Icons.Default.ArrowBack,
            tint = MaterialTheme.colorScheme.onSurface,
            onClick = onNavClick
        )
    }, actions = {
        if (showCloseIcon) {
            ClickableIcon(
                imageVector = Icons.Default.Close,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onCloseIconClick
            )
        } else {
            ClickableIcon(
                imageVector = Icons.Default.DateRange,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = dateDialogState::show
            )
        }

        if (diaryId != null) {
            OverFlowMenu(diaryTitle = diaryTitle, onDeleteConfirmed = onDeleteConfirmed)
        }
    })

    CalendarDialog(state = dateDialogState, selection = CalendarSelection.Date { localDate ->
        onDateSelected(localDate)
        timeDialogState.show()
    })

    ClockDialog(state = timeDialogState, selection = ClockSelection.HoursMinutes { hours, minutes ->
        val localTime = LocalTime.of(hours, minutes)
        onTimeSelected(localTime)
    })
}

@Composable
fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = maxOf(1f, minOf(scale * zoom, 5f))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.imageUri.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClick) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Icon")
                Text(text = "Close")
            }
            Button(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Icon")
                Text(text = "Delete")
            }
        }
    }
}

@Composable
fun OverFlowMenu(
    diaryTitle: String, onDeleteConfirmed: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    val onDismiss: () -> Unit = { expanded = false }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        MenuItem(menuTextRes = R.string.delete,
            onDismiss = onDismiss,
            onClick = { isDialogVisible = true })
    }

    if (isDialogVisible) {
        DisplayAlertDialog(
            title = "Delete",
            message = "Are you sure you want to permanently delete this diary note '${diaryTitle}'?",
            closeDialog = { isDialogVisible = false },
            onPositiveButtonClick = onDeleteConfirmed
        )
    }

    ClickableIcon(
        imageVector = Icons.Default.MoreVert, tint = MaterialTheme.colorScheme.onSurface
    ) { expanded = !expanded }
}

@Composable
fun MenuItem(
    @StringRes menuTextRes: Int,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(text = {
        Text(
            text = stringResource(id = menuTextRes),
            style = MaterialTheme.typography.titleMedium
        )
    }, onClick = { onDismiss(); onClick() }, contentPadding = PaddingValues(16.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun WritePrev() {
    WriteBody(pagerState = rememberPagerState(),
        galleryState = rememberGalleryState(),
        title = "",
        description = "",
        onTitleChanged = {},
        onDescriptionChanged = {},
        onImagesSelected = {},
        onImageClick = {},
        onSaveClick = {}
    )
}

