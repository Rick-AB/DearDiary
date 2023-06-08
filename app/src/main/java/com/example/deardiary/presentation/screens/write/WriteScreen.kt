@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalPagerApi::class,
    ExperimentalPagerApi::class
)

package com.example.deardiary.presentation.screens.write

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.deardiary.R
import com.example.deardiary.domain.model.Mood
import com.example.deardiary.presentation.components.ClickableIcon
import com.example.deardiary.presentation.components.DisplayAlertDialog
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WriteScreen(
    writeScreenState: WriteScreenState,
    pagerState: PagerState,
    messageBarState: MessageBarState,
    moodName: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    onCloseIconClick: () -> Unit,
    onSaveClick: () -> Unit,
    navigateUp: () -> Unit
) {
    LaunchedEffect(key1 = writeScreenState.mood) {
        pagerState.scrollToPage(writeScreenState.mood.ordinal)
    }

    Scaffold(
        topBar = {
            WriteTopAppBar(
                moodName = moodName,
                diaryId = writeScreenState.diaryId,
                diaryTitle = writeScreenState.title,
                initialDate = writeScreenState.initialDate?.toInstant() ?: Instant.now(),
                date = writeScreenState.date.toInstant(),
                onDateSelected = onDateSelected,
                onTimeSelected = onTimeSelected,
                onCloseIconClick = onCloseIconClick,
                onNavClick = navigateUp
            )
        }
    ) {
        ContentWithMessageBar(messageBarState = messageBarState) {
            Box(modifier = Modifier.padding(it)) {
                WriteBody(
                    modifier = Modifier.padding(top = 30.dp),
                    pagerState = pagerState,
                    title = writeScreenState.title,
                    description = writeScreenState.description,
                    onTitleChanged = onTitleChanged,
                    onDescriptionChanged = onDescriptionChanged,
                    onSaveClick = onSaveClick
                )
            }
        }
    }
}

@Composable
fun WriteBody(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val allMoods = remember { Mood.values() }

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
                keyboardActions = KeyboardActions(onNext = {})
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
                keyboardActions = KeyboardActions(onNext = {})
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        ) {
            Button(
                enabled = title.isNotEmpty() && description.isNotEmpty(),
                onClick = onSaveClick,
                shape = Shapes().small,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
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
    onNavClick: () -> Unit
) {
    val dateDialogState = rememberSheetState()
    val timeDialogState = rememberSheetState()
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }
    val showCloseIcon = remember(date) { initialDate.compareTo(date) != 0 }

    CenterAlignedTopAppBar(
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = moodName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = formatter.format(date),
                    style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                )
            }
        },
        navigationIcon = {
            ClickableIcon(
                imageVector = Icons.Default.ArrowBack,
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onNavClick
            )
        },
        actions = {
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
                OverFlowMenu(diaryId = diaryId, diaryTitle = diaryTitle, onDeleteConfirmed = {})
            }
        }
    )

    CalendarDialog(
        state = dateDialogState,
        selection = CalendarSelection.Date { localDate ->
            onDateSelected(localDate)
            timeDialogState.show()
        }
    )

    ClockDialog(
        state = timeDialogState,
        selection = ClockSelection.HoursMinutes { hours, minutes ->
            val localTime = LocalTime.of(hours, minutes)
            onTimeSelected(localTime)
        }
    )
}

@Composable
fun OverFlowMenu(
    diaryId: String,
    diaryTitle: String,
    onDeleteConfirmed: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isDialogVisible by remember { mutableStateOf(false) }
    val onDismiss: () -> Unit = { expanded = false }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        MenuItem(
            menuTextRes = R.string.delete,
            onDismiss = onDismiss,
            onClick = { isDialogVisible = true }
        )
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
        imageVector = Icons.Default.MoreVert,
        tint = MaterialTheme.colorScheme.onSurface
    ) { expanded = !expanded }
}

@Composable
fun MenuItem(
    @StringRes menuTextRes: Int,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(id = menuTextRes),
                style = MaterialTheme.typography.titleMedium
            )
        },
        onClick = { onDismiss(); onClick() },
        contentPadding = PaddingValues(16.dp)
    )
}


@Preview(showBackground = true)
@Composable
fun WritePrev() {
    WriteBody(
        pagerState = rememberPagerState(),
        title = "",
        description = "",
        onTitleChanged = {},
        onDescriptionChanged = {},
        onSaveClick = {}
    )
}

