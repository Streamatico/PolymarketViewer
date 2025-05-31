package com.streamatico.polymarketviewer.ui.event_detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.streamatico.polymarketviewer.data.model.EventDto

// --- Utility Functions (moved from TranslateUtils) ---

/**
 * Attempts to start the standard Android ACTION_PROCESS_TEXT intent.
 *
 * @param context The application context.
 * @param textToTranslate The text to be translated.
 * @return `true` if the translate intent was successfully launched, `false` otherwise
 *         (no suitable translate app was found).
 * @throws Exception Rethrows any exception other than ActivityNotFoundException.
 */
private fun tryStartTranslateIntent(context: Context, textToTranslate: String): Boolean {
    val processTextIntent = Intent(Intent.ACTION_PROCESS_TEXT).apply {
        putExtra(Intent.EXTRA_TEXT, textToTranslate)
        putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
        type = "text/plain"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.google.android.apps.translate")
    }
    try {
        context.startActivity(processTextIntent)
        return true // Intent launched successfully
    } catch (e: ActivityNotFoundException) {
        Log.e("Translate", "Google Translate not found or cannot handle ACTION_PROCESS_TEXT", e)
        return false // No app found
    } catch (e: Exception) {
        Log.e("Translate", "Failed to start ACTION_PROCESS_TEXT activity", e)
        throw e // Rethrow unexpected exceptions
    }
}

/**
 * Opens the Google Translate app page in the Google Play Store.
 *
 * @param context The application context.
 */
private fun openGoogleTranslateInPlayStore(context: Context) {
    try {
        val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=com.google.android.apps.translate".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(playStoreIntent)
    } catch (e: Exception) {
        Log.e("Translate", "Failed to open Play Store for Google Translate", e)
        Toast.makeText(context, "Could not open Play Store.", Toast.LENGTH_SHORT).show()
    }
}

// --- Composable Components ---

/**
 * Displays the AlertDialog prompting the user to install Google Translate.
 */
@Composable
private fun InstallTranslateDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = {
            Icon(Icons.Default.Translate, contentDescription = "Translate Icon")
        },
        title = { Text("Google Translate Not Found") },
        text = { Text("To translate content, the Google Translate app needs to be installed.") },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm() // Call the provided confirm lambda
                    // Actual opening logic is now called from the caller
                    // openGoogleTranslateInPlayStore(context)
                }
            ) {
                Text("Install")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Displays the Translate action icon in the TopAppBar.
 *
 * @param isVisible Whether the icon should be visible (e.g., based on locale and UI state).
 * @param event The current EventDto (nullable).
 */
@Composable
fun TranslateAction(
    isVisible: Boolean,
    event: EventDto?,
    // onTranslateFailed: () -> Unit // Remove parameter
) {
    // Internal state to control the dialog visibility
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current // Get context once

    if (isVisible && event != null) {
        val titleToTranslate = event.title
        val descriptionToTranslate = event.description ?: ""
        val textToTranslate = "$titleToTranslate\n\n$descriptionToTranslate".trim()

        if (textToTranslate.isNotBlank()) {
            IconButton(onClick = {
                if (!tryStartTranslateIntent(context, textToTranslate)) {
                    // onTranslateFailed() // Remove callback
                    showDialog = true // Show dialog directly
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Translate Title and Description"
                )
            }
        }
    }

    // Show the dialog if needed
    if (showDialog) {
        InstallTranslateDialog(
            onDismissRequest = { showDialog = false },
            onConfirm = {
                showDialog = false
                openGoogleTranslateInPlayStore(context) // Open Play Store on confirm
            }
        )
    }
} 