package com.example.deardiary.presentation.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate


@Composable
fun DateHeader(localeDate: LocalDate, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = String.format("%02d", localeDate.dayOfMonth),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light)
            )

            Text(
                text = localeDate.dayOfWeek.toString().take(3),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light)

            )
        }


        Spacer(modifier = Modifier.width(14.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = localeDate.month.toString().lowercase().replaceFirstChar(Char::titlecase),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Light)
            )

            Text(
                text = localeDate.year.toString(),
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DateHeaderPrev() {
    DateHeader(localeDate = LocalDate.now())
}