package com.streamatico.polymarketviewer.ui.shared.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.ui.shared.UiError
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme

data class ErrorBoxSecondaryAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
fun ErrorBox(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryAction: ErrorBoxSecondaryAction? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error.title,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        error.details?.let { details ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = details,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (secondaryAction != null) {
            Row {
                Button(onClick = onRetry) {
                    Text(stringResource(id = R.string.action_retry))
                }
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(onClick = secondaryAction.onClick) {
                    Text(secondaryAction.label)
                }
            }
        } else {
            Button(onClick = onRetry) {
                Text(stringResource(id = R.string.action_retry))
            }
        }
    }
}

// ====== Preview ======

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ErrorBoxPreview() {
    PolymarketAppTheme {
        ErrorBox(
            error = UiError(
                title = "Failed to load data",
                details = "Unable to connect to example host. Please check your network settings and try again."
            ),
            onRetry = {},
            secondaryAction = ErrorBoxSecondaryAction(
                label = "Open DNS settings",
                onClick = {}
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorBoxSimplePreview() {
    PolymarketAppTheme {
        ErrorBox(
            error = UiError(
                title = "Failed to load data",
                details = "Unable to connect to example host. Please check your network settings and try again."
            ),
            onRetry = {}
        )
    }
}