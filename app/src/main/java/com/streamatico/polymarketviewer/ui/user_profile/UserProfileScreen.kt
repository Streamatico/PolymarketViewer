package com.streamatico.polymarketviewer.ui.user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.UserProfileDto
import com.streamatico.polymarketviewer.data.model.getDisplayName
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.OpenInBrowserIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val successUserProfile = (uiState as? UserProfileUiState.Success)?.userProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val displayName = successUserProfile?.getDisplayName()
                    Text(
                        text = displayName ?: stringResource(R.string.user_profile_title),
                        overflow = TextOverflow.Ellipsis)
                },
                actions = {
                    if(successUserProfile != null) {
                        OpenInBrowserIconButton(
                            "https://polymarket.com/profile/${successUserProfile.proxyWallet}"
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is UserProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UserProfileUiState.Success -> {
                    UserProfileContent(userProfile = state.userProfile)
                }
                is UserProfileUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.retryLoad() }) { Text("Retry") }
                    }
                }
            }
        }
    }
}

@Composable
fun UserProfileContent(userProfile: UserProfileDto, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = userProfile.profileImage,
            contentDescription = "${userProfile.getDisplayName()}'s avatar",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))

        if(!userProfile.name.isNullOrEmpty()) {
            InfoRow("Name",userProfile.name)
        }

        if(!userProfile.pseudonym.isNullOrEmpty()) {
            InfoRow("Pseudonym",userProfile.pseudonym)
        }

        InfoRow("Address",userProfile.proxyWallet)

        // Add more fields as needed
        if(userProfile.createdAt != null) {
            InfoRow(
                "Joined",
                UiFormatter.formatDateTimeLong(userProfile.createdAt)
            )
        }
    }
}

// Reusable InfoRow (similar to the one in EventDetailScreen)
@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(100.dp) // Align labels
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            //fontWeight = FontWeight.SemiBold,
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}