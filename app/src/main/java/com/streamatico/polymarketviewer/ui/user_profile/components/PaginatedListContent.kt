package com.streamatico.polymarketviewer.ui.user_profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.streamatico.polymarketviewer.ui.shared.PaginatedList
import com.streamatico.polymarketviewer.ui.shared.components.ErrorBox
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
internal fun <T> PaginatedListContent(
    paginatedList: PaginatedList<T>,
    itemContent: @Composable (T) -> Unit
) {
    val state by paginatedList.state.collectAsState()
    val listState = rememberLazyListState()

    // Handle empty list states (Error, Loading, or No Items)
    if (state.items.isEmpty()) {
        if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorBox(
                    message = state.error!!,
                    onRetry = paginatedList::refresh
                )
            }
            return
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        // If we are here: Empty, Not Loading, No Error.
        // Show "No items" placeholder.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No items", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    LazyColumn(
        state = listState,
        //contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.items) { item ->
            itemContent(item)
        }

        if (state.isLoading) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        if (state.error != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error loading more: ${state.error}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = paginatedList::loadMore) { Text("Retry") }
                }
            }
        }
    }

    InfiniteListHandler(
        listState = listState,
        onLoadMore = paginatedList::loadMore,
        canLoadMore = state.canLoadMore && state.error == null,
        isLoading = state.isLoading
    )
}

@Composable
private fun InfiniteListHandler(
    listState: LazyListState,
    onLoadMore: () -> Unit,
    canLoadMore: Boolean,
    isLoading: Boolean,
    buffer: Int = 2
) {
    LaunchedEffect(listState, canLoadMore, isLoading) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .filter { it != null }
            .map { it!! }
            .distinctUntilChanged()
            .collect { lastVisibleItemIndex ->
                val totalItems = listState.layoutInfo.totalItemsCount
                if (canLoadMore && !isLoading && totalItems > 0 && lastVisibleItemIndex >= totalItems - buffer) {
                    onLoadMore()
                }
            }
    }
}