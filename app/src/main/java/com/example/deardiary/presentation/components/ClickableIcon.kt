package com.example.deardiary.presentation.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ClickableIcon(
    imageVector: ImageVector,
    contentDescription: String = "",
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(imageVector = imageVector, contentDescription = contentDescription, tint = tint)
    }
}