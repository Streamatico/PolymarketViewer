package com.streamatico.polymarketviewer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.preferences.DnsOverHttpsProvider
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme

private const val DOH_LEARN_MORE_URL = "https://en.wikipedia.org/wiki/DNS_over_HTTPS"

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel
) {
    val userPreferences by viewModel.userPreferencesFlow.collectAsState(initial = null)

    SettingsScreenContent(
        onNavigateBack = onNavigateBack,
        analyticsEnabled = userPreferences?.analyticsEnabled,
        dohEnabled = userPreferences?.dohEnabled,
        dohProvider = userPreferences?.dohProvider,
        onAnalyticsEnabledChange = viewModel::setAnalyticsEnabled,
        onDohEnabledChange = viewModel::setDohEnabled,
        onDohProviderSelected = viewModel::setDohProvider
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    onNavigateBack: () -> Unit,
    analyticsEnabled: Boolean?,
    dohEnabled: Boolean?,
    dohProvider: DnsOverHttpsProvider?,
    onAnalyticsEnabledChange: (Boolean) -> Unit,
    onDohEnabledChange: (Boolean) -> Unit,
    onDohProviderSelected: (DnsOverHttpsProvider) -> Unit
) {
    val selectedProvider = dohProvider ?: DnsOverHttpsProvider.DEFAULT
    val uriHandler = LocalUriHandler.current

    MyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.doh_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.doh_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (dohEnabled == null) {
                        CircularProgressIndicator()
                    } else {
                        Switch(
                            checked = dohEnabled,
                            onCheckedChange = onDohEnabledChange
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.doh_provider_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.doh_provider_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DnsOverHttpsProvider.entries.forEachIndexed { index, provider ->
                        val labelResId = when (provider) {
                            DnsOverHttpsProvider.GOOGLE -> R.string.doh_provider_google
                            DnsOverHttpsProvider.CLOUDFLARE -> R.string.doh_provider_cloudflare
                        }

                        SegmentedButton(
                            modifier = Modifier.weight(1f),
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = DnsOverHttpsProvider.entries.size
                            ),
                            selected = selectedProvider == provider,
                            onClick = { onDohProviderSelected(provider) },
                            enabled = dohEnabled == true
                        ) {
                            Text(text = stringResource(id = labelResId))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.doh_helper_text),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(id = R.string.doh_learn_more),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable {
                        uriHandler.openUri(DOH_LEARN_MORE_URL)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.analytics_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(id = R.string.analytics_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (analyticsEnabled == null) {
                        CircularProgressIndicator()
                    } else {
                        Switch(
                            checked = analyticsEnabled,
                            onCheckedChange = onAnalyticsEnabledChange
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    PolymarketAppTheme {
        SettingsScreenContent(
            onNavigateBack = {},
            analyticsEnabled = true,
            dohEnabled = true,
            dohProvider = DnsOverHttpsProvider.CLOUDFLARE,
            onAnalyticsEnabledChange = {},
            onDohEnabledChange = {},
            onDohProviderSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Disabled")
@Composable
private fun SettingsScreenDisabledPreview() {
    PolymarketAppTheme {
        SettingsScreenContent(
            onNavigateBack = {},
            analyticsEnabled = false,
            dohEnabled = false,
            dohProvider = DnsOverHttpsProvider.GOOGLE,
            onAnalyticsEnabledChange = {},
            onDohEnabledChange = {},
            onDohProviderSelected = {}
        )
    }
}

