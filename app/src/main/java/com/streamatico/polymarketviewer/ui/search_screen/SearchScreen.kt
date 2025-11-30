package com.streamatico.polymarketviewer.ui.search_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.streamatico.polymarketviewer.R
import com.streamatico.polymarketviewer.data.model.gamma_api.OptimizedEventDto
import com.streamatico.polymarketviewer.ui.event_list.components.EventListItem
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import com.streamatico.polymarketviewer.ui.shared.components.LoadingBox
import com.streamatico.polymarketviewer.ui.shared.components.MyScaffold
import com.streamatico.polymarketviewer.ui.theme.PolymarketAppTheme
import com.streamatico.polymarketviewer.ui.tooling.PreviewMocks

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventSlug: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    SearchScreenContent(
        uiState = uiState,
        searchQuery = searchQuery,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearch = viewModel::clearSearch,
        onNavigateBack = onNavigateBack,
        onNavigateToEventDetail = onNavigateToEventDetail,
        onRetry = viewModel::retrySearch
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToEventDetail: (eventSlug: String) -> Unit,
    onRetry: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    MyScaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                title = {
                    SearchTextField(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                },
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearSearch) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.search_clear)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is SearchUiState.Empty -> {
                    EmptySearchState()
                }
                is SearchUiState.Loading -> {
                    LoadingBox(Modifier.align(Alignment.Center))
                }
                is SearchUiState.Success -> {
                    if (uiState.events.isEmpty()) {
                        NoResultsState()
                    } else {
                        SearchResultsList(
                            events = uiState.events,
                            onEventClick = onNavigateToEventDetail
                        )
                    }
                }
                is SearchUiState.Error -> {
                    ErrorBox(
                        message = uiState.message,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        singleLine = true,
        decorationBox = { innerTextField ->
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            innerTextField()
        }
    )
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.search_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun NoResultsState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.search_no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@Composable
private fun SearchResultsList(
    events: List<OptimizedEventDto>,
    onEventClick: (eventSlug: String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
    ) {
        items(events, key = { it.id }) { event ->
            EventListItem(
                event = event,
                onClick = { onEventClick(event.slug) }
            )
        }
    }
}

// === Previews ===

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Success")
@Composable
private fun SearchScreenPreview_Success() {
    PolymarketAppTheme {
        SearchScreenContent(
            uiState = SearchUiState.Success(
                PreviewMocks.sampleOptimizedEvents
            ),
            searchQuery = "xyz123",
            onSearchQueryChange = {},
            onClearSearch = {},
            onNavigateBack = {},
            onNavigateToEventDetail = {},
            onRetry = {}
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Empty State")
@Composable
private fun SearchScreenPreview_Empty() {
    PolymarketAppTheme {
        SearchScreenContent(
            uiState = SearchUiState.Empty,
            searchQuery = "",
            onSearchQueryChange = {},
            onClearSearch = {},
            onNavigateBack = {},
            onNavigateToEventDetail = {},
            onRetry = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Loading State")
@Composable
private fun SearchScreenPreview_Loading() {
    PolymarketAppTheme {
        SearchScreenContent(
            uiState = SearchUiState.Loading,
            searchQuery = "trump",
            onSearchQueryChange = {},
            onClearSearch = {},
            onNavigateBack = {},
            onNavigateToEventDetail = {},
            onRetry = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "No Results")
@Composable
private fun SearchScreenPreview_NoResults() {
    PolymarketAppTheme {
        SearchScreenContent(
            uiState = SearchUiState.Success(emptyList()),
            searchQuery = "xyz123",
            onSearchQueryChange = {},
            onClearSearch = {},
            onNavigateBack = {},
            onNavigateToEventDetail = {},
            onRetry = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Error State")
@Composable
private fun SearchScreenPreview_Error() {
    PolymarketAppTheme {
        SearchScreenContent(
            uiState = SearchUiState.Error("Network error occurred"),
            searchQuery = "trump",
            onSearchQueryChange = {},
            onClearSearch = {},
            onNavigateBack = {},
            onNavigateToEventDetail = {},
            onRetry = {}
        )
    }
}