package com.example.deardiary.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.max

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    images: List<String>,
    size: Dp = 40.dp,
    spaceBetween: Dp = 10.dp,
    shape: CornerBasedShape = Shapes().small
) {
    BoxWithConstraints(modifier) {
        val numberOfVisibleImages = remember {
            max(0, this.maxWidth.div(size + spaceBetween).minus(1).toInt())
        }

        val remainingImages = remember { images.size.minus(numberOfVisibleImages) }

        Row {
            images.take(numberOfVisibleImages).forEach { image ->
                AsyncImage(
                    modifier = Modifier
                        .clip(shape)
                        .size(size),
                    model = ImageRequest.Builder(LocalContext.current).data(image).crossfade(true)
                        .build(),
                    contentDescription = "Gallery image"
                )

                Spacer(modifier = Modifier.width(spaceBetween))
            }

            if (remainingImages > 0) {
                LastImageOverlay(size = size, remainingImages = remainingImages, shape = shape)
            }
        }
    }

}

@Composable
fun LastImageOverlay(
    size: Dp, remainingImages: Int, shape: CornerBasedShape
) {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .clip(shape)
                .size(size),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}

        Text(
            text = "+$remainingImages",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}