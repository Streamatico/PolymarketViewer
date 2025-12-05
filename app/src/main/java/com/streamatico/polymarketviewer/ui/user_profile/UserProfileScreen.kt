package com.streamatico.polymarketviewer.ui.user_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.data_api.UserActivityDto
import com.streamatico.polymarketviewer.data.model.data_api.UserClosedPositionDto
import com.streamatico.polymarketviewer.data.model.data_api.UserPositionDto
import com.streamatico.polymarketviewer.data.model.gamma_api.UserProfileDto
import com.streamatico.polymarketviewer.data.model.gamma_api.getDisplayName
import com.streamatico.polymarketviewer.ui.shared.PaginatedDataLoader
import com.streamatico.polymarketviewer.ui.shared.PaginatedList
import com.streamatico.polymarketviewer.ui.shared.UiFormatter
import com.streamatico.polymarketviewer.ui.shared.components.OpenInBrowserIconButton
import com.streamatico.polymarketviewer.ui.tooling.ProfilePreviewMocks
import com.streamatico.polymarketviewer.ui.user_profile.components.UserActivityItem
import com.streamatico.polymarketviewer.ui.user_profile.components.ClosedPositionItem
import com.streamatico.polymarketviewer.ui.user_profile.components.PaginatedListContent
import com.streamatico.polymarketviewer.ui.user_profile.components.PositionItem

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onEventClick: (eventSlug: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    val totalPositionsValue by viewModel.totalPositionsValue.collectAsState()
    val userTraded by viewModel.userTraded.collectAsState()

    UserProfileScaffold(
        profileState = profileState,
        totalPositionsValue = totalPositionsValue,
        userTraded = userTraded,
        onNavigateBack = onNavigateBack,
        onRetryLoadProfile = viewModel::retryLoadProfile,
        onEventClick = onEventClick,

        activePositions = viewModel.activePositions,
        closedPositions = viewModel.closedPositions,
        activities = viewModel.activities
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileScaffold(
    profileState: UserProfileState,
    totalPositionsValue: Double?,
    userTraded: Int?,
    onNavigateBack: () -> Unit,
    onRetryLoadProfile: () -> Unit,
    onEventClick: (eventSlug: String) -> Unit,

    activePositions: PaginatedList<UserPositionDto>,
    closedPositions: PaginatedList<UserClosedPositionDto>,
    activities: PaginatedList<UserActivityDto>,
) {
    val successState = profileState as? UserProfileState.Success
    val userProfile = successState?.userProfile

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.user_profile_title),
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (userProfile != null) {
                        OpenInBrowserIconButton(
                            "https://polymarket.com/profile/${userProfile.proxyWallet}"
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
            when (profileState) {
                is UserProfileState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UserProfileState.Success -> {
                    UserProfileContent(
                        userProfile = profileState.userProfile,
                        portfolioValue = totalPositionsValue,
                        userTraded = userTraded,
                        activePositions = activePositions,
                        closedPositions = closedPositions,
                        activities = activities,
                        onEventClick = onEventClick,
                    )
                }
                is UserProfileState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(profileState.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onRetryLoadProfile) { Text("Retry") }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileContent(
    userProfile: UserProfileDto,
    portfolioValue: Double?,
    userTraded: Int?,
    activePositions: PaginatedList<UserPositionDto>,
    closedPositions: PaginatedList<UserClosedPositionDto>,
    activities: PaginatedList<UserActivityDto>,
    onEventClick: (eventSlug: String) -> Unit,
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("Positions", "Activity")

    // Lazy loading effect based on selected tab
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> activePositions.loadIfNeeded() // Load active positions by default when entering tab
            1 -> activities.loadIfNeeded()        // Load activity when entering tab
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        UserProfileHeader(
            userProfile = userProfile,
            portfolioValue = portfolioValue,
            userTraded = userTraded,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            // Tabs
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content
            when (selectedTabIndex) {
                0 -> PositionsTab(
                    activePositions = activePositions,
                    closedPositions = closedPositions,
                    onEventClick = onEventClick
                )

                1 -> ActivityTab(
                    activityList = activities,
                    onEventClick = { eventSlug -> onEventClick(eventSlug) }
                )
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    userProfile: UserProfileDto,
    portfolioValue: Double?,
    userTraded: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar and Name Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = userProfile.profileImage,
                contentDescription = "${userProfile.getDisplayName()}'s avatar",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val displayName = userProfile.getDisplayName()
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!userProfile.pseudonym.isNullOrEmpty() && userProfile.pseudonym != displayName) {
                    Text(
                        text = "@${userProfile.pseudonym}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if(userProfile.createdAt != null) {
                    Text(
                        text = "Joined ${UiFormatter.formatDateOnly(userProfile.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "Positions Value",
                value = portfolioValue.let {
                    if(it != null) UiFormatter.formatCurrency(it)
                    else "—"
                }
            )
            StatItem(label = "Predictions", value = userTraded?.toString() ?: "—")
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PositionsTab(
    activePositions: PaginatedList<UserPositionDto>,
    closedPositions: PaginatedList<UserClosedPositionDto>,
    onEventClick: (eventSlug: String) -> Unit,
) {
    var showActive by rememberSaveable { mutableStateOf(true) }

    // Load closed positions when switched to closed tab
    LaunchedEffect(showActive) {
        if (!showActive) {
            closedPositions.loadIfNeeded()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            FilterChip(selected = showActive, onClick = { showActive = true }, label = "Active")
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(selected = !showActive, onClick = { showActive = false }, label = "Closed")
        }

        if (showActive) {
            PaginatedListContent(
                paginatedList = activePositions
            ) { position ->
                PositionItem(
                    position = position,
                    onClick = { onEventClick(position.eventSlug) }
                )
            }
        } else {
            PaginatedListContent(
                paginatedList = closedPositions
            ) { position ->
                ClosedPositionItem(
                    position = position,
                    onClick = { onEventClick(position.eventSlug) }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(selected: Boolean, onClick: () -> Unit, label: String) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon =
            if (selected) {
                {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                    )
                }
            } else {
                null
            }
    )
}

@Composable
private fun ActivityTab(
    activityList: PaginatedList<UserActivityDto>,
    onEventClick: (eventSlug: String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        PaginatedListContent(
            paginatedList = activityList
        ) { item ->
            UserActivityItem(
                userActivity = item,
                onClick = if(item.eventSlug.isNotBlank()) {
                    { onEventClick(item.eventSlug) }
                } else {
                    null
                }
            )
        }
    }
}


// === Previews ===

@Preview
@Composable
fun UserProfileScreenPreview() {
    val coroutineScope = rememberCoroutineScope()

    UserProfileScaffold(
        profileState = UserProfileState.Success(ProfilePreviewMocks.profileSample),
        totalPositionsValue = 10000.0,
        userTraded = 5,
        onNavigateBack = {},
        onRetryLoadProfile = {},
        onEventClick = {},
        activePositions = PaginatedList(
            PaginatedDataLoader(coroutineScope, {Result.success(ProfilePreviewMocks.positionsSample)})
        ),
        closedPositions = PaginatedList(
            PaginatedDataLoader(coroutineScope, {Result.success(emptyList())})
        ),
        activities = PaginatedList(
            PaginatedDataLoader(coroutineScope, {Result.success(emptyList())})
        ),
    )
}

