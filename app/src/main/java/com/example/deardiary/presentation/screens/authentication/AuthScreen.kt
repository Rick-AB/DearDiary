package com.example.deardiary.presentation.screens.authentication

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.deardiary.R
import com.example.deardiary.util.Constants.CLIENT_ID
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    loading: Boolean,
    oneTapSignInState: OneTapSignInState,
    messageBarState: MessageBarState,
    onClick: () -> Unit,
    onTokenReceived: (String) -> Unit,
    onDialogDismissed: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
            .statusBarsPadding()
    ) {
        ContentWithMessageBar(messageBarState = messageBarState) {
            AuthBody(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .padding(bottom = 20.dp),
                loading = loading,
                onClick = onClick
            )
        }
    }

    OneTapSignInWithGoogle(
        state = oneTapSignInState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { onTokenReceived(it) },
        onDialogDismissed = { onDialogDismissed(it) }
    )
}

@Composable
fun AuthBody(modifier: Modifier, loading: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {

        Spacer(modifier = Modifier.weight(0.3f))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .padding(top = 40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.google_logo),
                contentDescription = "Google logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(id = R.string.auth_title),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )

            Text(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                text = stringResource(id = R.string.auth_sub_title),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }

        GoogleButton(
            modifier = Modifier.fillMaxWidth(0.75f),
            loading = loading,
            onClick = onClick
        )
    }
}

@Composable
fun GoogleButton(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    primaryText: String = stringResource(id = R.string.sign_in_with_google),
    secondaryText: String = stringResource(id = R.string.please_wait),
    icon: Int = R.drawable.google_logo,
    shape: Shape = Shapes().extraSmall,
    borderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderStrokeWidth: Dp = 1.dp,
    progressIndicatorColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    val buttonText = remember(loading) {
        if (loading) secondaryText
        else primaryText
    }

    Button(
        modifier = modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearOutSlowInEasing
            )
        ),
        onClick = onClick,
        shape = shape,
        enabled = !loading,
        border = BorderStroke(width = borderStrokeWidth, color = borderColor),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Google Logo",
            tint = Color.Unspecified
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buttonText,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )

        if (loading) {
            Spacer(modifier = Modifier.width(16.dp))
            CircularProgressIndicator(
                modifier = Modifier
                    .size(16.dp),
                strokeWidth = 2.dp,
                color = progressIndicatorColor
            )
        }
    }
}