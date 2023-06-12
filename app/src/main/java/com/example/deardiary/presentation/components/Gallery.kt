@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.deardiary.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.deardiary.domain.model.GalleryImage
import com.example.deardiary.domain.model.GalleryState
import kotlin.math.max

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    editable: Boolean = false,
    state: GalleryState,
    size: Dp = 40.dp,
    spaceBetween: Dp = 10.dp,
    shape: CornerBasedShape = Shapes().small,
    onAddIconClick: () -> Unit = {},
    onImagesSelected: (List<Uri>) -> Unit = {},
    onImageClick: (GalleryImage) -> Unit = {}
) {
    BoxWithConstraints(modifier) {
        val context = LocalContext.current
        val imagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
        ) { uris ->
            onImagesSelected(uris)
            uris.forEach {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }

        val numberOfVisibleImages by remember {
            derivedStateOf {
                val nonImageComponents = if (editable) 2 else 1
                max(0, this.maxWidth.div(size + spaceBetween).minus(nonImageComponents).toInt())
            }
        }

        val remainingImages by remember {
            derivedStateOf {
                state.images.size.minus(numberOfVisibleImages)
            }
        }

        Row {
            if (editable) {
                GalleryImageOverlay(
                    size = size,
                    text = "+",
                    shape = shape,
                    onClick = {
                        onAddIconClick()
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }

            state.images.take(numberOfVisibleImages).forEach { galleryImage ->
                AsyncImage(
                    modifier = Modifier
                        .clip(shape)
                        .size(size)
                        .clickable(enabled = editable, onClick = { onImageClick(galleryImage) }),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(galleryImage.imageUri)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Gallery image"
                )

                Spacer(modifier = Modifier.width(spaceBetween))
            }

            if (remainingImages > 0) {
                GalleryImageOverlay(size = size, text = "+$remainingImages", shape = shape)
            }
        }
    }

}

@Composable
fun GalleryImageOverlay(
    size: Dp,
    text: String,
    shape: CornerBasedShape,
    onClick: (() -> Unit)? = null
) {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            enabled = onClick != null,
            onClick = onClick ?: {},
            modifier = Modifier
                .clip(shape)
                .size(size),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}

        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}