package com.streamatico.polymarketviewer.ui.search_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventId: String) -> Unit,
) {
    // TODO: Implement search screen
    Scaffold() { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            Button(onNavigateBack) {
                Text("Back")
            }
        }
    }
}

// === Preview ===
// TODO: Implement previews