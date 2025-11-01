package com.streamatico.polymarketviewer.ui.about

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamatico.polymarketviewer.BuildConfig
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.preferences.UserPreferencesRepository
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import kotlinx.coroutines.launch

const val PRIVACY_POLICY_URL = "https://streamatico.net/polymarketapp/privacy_policy"
const val GITHUB_URL = "https://github.com/streamatico/PolymarketViewer"
const val EMAIL_ADDRESS = "streamatico+polymarket@gmail.com"
const val COMPANY_URL = "https://streamatico.net/"

@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    userPreferencesRepository: UserPreferencesRepository = hiltViewModel<AboutViewModel>().userPreferencesRepository
) {
    val userPreferences by userPreferencesRepository
        .userPreferencesFlow
        .collectAsState(initial = null)

    val scope = rememberCoroutineScope()

    AboutScreenContent(
        onNavigateBack = onNavigateBack,
        analyticsEnabled = userPreferences?.analyticsEnabled,
        onAnalyticsEnabledChange = { enabled ->
            scope.launch {
                userPreferencesRepository.setAnalyticsEnabled(enabled)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutScreenContent(
    onNavigateBack: () -> Unit,
    analyticsEnabled: Boolean?,
    onAnalyticsEnabledChange: (enabled: Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    MyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.about_app_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Header
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_logo_content_description),
                modifier = Modifier.size(96.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Clickable text with link to company website
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.developed_by)+" ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.company_name),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable { uriHandler.openUri(COMPANY_URL) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Links Card
            InfoCard {
                Text(
                    text = stringResource(id = R.string.links_section_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LinkItem(
                    icon = Icons.Outlined.Code,
                    title = stringResource(id = R.string.source_code),
                    subtitle = "GitHub",
                    contentDescription = stringResource(id = R.string.open_github),
                    onClick = { uriHandler.openUri(GITHUB_URL) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LinkItem(
                    icon = Icons.Outlined.Email,
                    title = stringResource(id = R.string.contact_email),
                    subtitle = EMAIL_ADDRESS,
                    contentDescription = stringResource(id = R.string.send_email),
                    onClick = { uriHandler.openUri("mailto:$EMAIL_ADDRESS") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Data Source Card
            InfoCard {
                Text(
                    text = stringResource(id = R.string.about_data_source_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.about_data_source_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Disclaimer Card
            InfoCard {
                Text(
                    text = stringResource(id = R.string.about_disclaimer_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(id = R.string.about_disclaimer_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Analytics Card
            InfoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.analytics_title),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.analytics_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if(analyticsEnabled == null) {
                        CircularProgressIndicator()
                    } else {
                        Switch(
                            checked = analyticsEnabled,
                            onCheckedChange = onAnalyticsEnabledChange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Footer with Privacy Policy
            Text(
                text = stringResource(id = R.string.privacy_policy),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.clickable { uriHandler.openUri(PRIVACY_POLICY_URL) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoCard(
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun LinkItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==== Previews ====

@Preview(showBackground = true, name = "About Screen Preview")
@Composable
private fun AboutScreenPreview() {
    PolymarketAppTheme {
        AboutScreenContent(
            onNavigateBack = {},
            analyticsEnabled = true,
            onAnalyticsEnabledChange = {}
        )
    }
}

@Preview(showBackground = true, name = "About Screen Preview (Dark)")
@Composable
private fun AboutScreenPreviewDark() {
    PolymarketAppTheme(darkTheme = true) {
        AboutScreenContent(
            onNavigateBack = {},
            analyticsEnabled = false,
            onAnalyticsEnabledChange = {}
        )
    }
}