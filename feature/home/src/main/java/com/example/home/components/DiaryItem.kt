package com.example.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.home.R
import com.example.ui.GalleryImage
import com.example.ui.components.Gallery
import com.example.ui.rememberGalleryState
import com.example.ui.theme.Elevation
import com.example.util.fetchImagesFromFirebase
import com.example.util.model.Diary
import com.example.util.model.Mood
import com.example.util.toInstant
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DiaryHolder(diary: Diary, onClick: (String) -> Unit) {
    val localDensity = LocalDensity.current
    val galleryState = rememberGalleryState()
    var lineHeight by remember { mutableStateOf(14.dp) }
    var galleryOpen by remember { mutableStateOf(false) }
    var downloadingImages by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = galleryOpen, key2 = galleryState.images.size) {
        if (galleryOpen && galleryState.images.isEmpty()) {
            downloadingImages = true
            fetchImagesFromFirebase(
                imagesPath = diary.images,
                onImageDownloaded = { imageUri, imagePath ->
                    galleryState.addImage(
                        GalleryImage(
                            imageUri,
                            imagePath
                        )
                    )
                }
            )
        }

        if (galleryState.images.size == diary.images.size) downloadingImages = false
    }

    Row(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember(::MutableInteractionSource),
        ) { onClick(diary._id.toHexString()) }
    ) {
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            tonalElevation = Elevation.Level1,
            modifier = Modifier.size(width = 2.dp, height = lineHeight)
        ) {}

        Spacer(modifier = Modifier.width(20.dp))
        Surface(
            modifier = Modifier
                .clip(Shapes().medium)
                .onGloballyPositioned {
                    lineHeight = with(localDensity) { it.size.height.toDp() + 14.dp }
                },
            tonalElevation = Elevation.Level1
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DiaryHeader(mood = Mood.valueOf(diary.mood), time = diary.date.toInstant())
                Text(
                    text = diary.description,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                if (diary.images.isNotEmpty()) {
                    val text = when {
                        !galleryOpen && !downloadingImages -> stringResource(id = R.string.show_gallery)
                        galleryOpen && downloadingImages -> stringResource(id = R.string.loading)
                        else -> stringResource(id = R.string.hide_gallery)
                    }

                    ShowGalleryButton(
                        text = text,
                        onClick = { galleryOpen = !galleryOpen }
                    )
                }

                AnimatedVisibility(
                    visible = galleryOpen && !downloadingImages,
                    enter = fadeIn() + expandVertically(
                        animationSpec = SpringSpec(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Gallery(state = galleryState)
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryHeader(mood: Mood, time: Instant) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(mood.containerColor)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = mood.icon),
                contentDescription = "mood icon",
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Text(
            text = formatter.format(time),
            color = mood.contentColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ShowGalleryButton(text: String, onClick: () -> Unit) {


    TextButton(onClick = onClick) {
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }

}

@Preview(showBackground = true)
@Composable
fun DiaryHolderPrev() {
    val diary = Diary().apply {
        mood = Mood.Happy.name
        title = "Happy"
        description =
            "Hercle, ventus camerarius!, impositio! Cacula, urbs, et xiphias. Hercle, ventus camerarius!, impositio! Cacula, urbs, et xiphias. Hercle, ventus camerarius!, impositio! Cacula, urbs, et xiphias. Hercle, ventus camerarius!, impositio! Cacula, urbs, et xiphias. Hercle, ventus camerarius!, impositio! Cacula, urbs, et xiphias."
    }
    DiaryHolder(diary = diary, onClick = {})
}