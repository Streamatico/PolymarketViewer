package com.streamatico.polymarketviewer.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.streamatico.polymarketviewer.ui.about.AboutScreen
import com.streamatico.polymarketviewer.ui.about.AboutViewModel
import com.streamatico.polymarketviewer.ui.event_detail.EventDetailScreen
import com.streamatico.polymarketviewer.ui.event_detail.EventDetailViewModel
import com.streamatico.polymarketviewer.ui.event_list.EventListScreen
import com.streamatico.polymarketviewer.ui.event_list.EventListViewModel
import com.streamatico.polymarketviewer.ui.market_detail.MarketDetailScreen
import com.streamatico.polymarketviewer.ui.market_detail.MarketDetailViewModel
import com.streamatico.polymarketviewer.ui.search_screen.SearchScreen
import com.streamatico.polymarketviewer.ui.search_screen.SearchViewModel
import com.streamatico.polymarketviewer.ui.user_profile.UserProfileScreen
import com.streamatico.polymarketviewer.ui.user_profile.UserProfileViewModel

@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(NavKeys.EventList)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        },
        popTransitionSpec = {
            slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        },
        entryProvider = entryProvider {
            entry<NavKeys.EventList> {
                val vm = hiltViewModel<EventListViewModel, EventListViewModel.Factory>(
                    creationCallback = { factory -> factory.create() }
                )
                EventListScreen(
                    viewModel = vm,
                    onNavigateToEventDetail = { eventSlug ->
                        backStack.add(NavKeys.EventDetail(eventSlug))
                    },
                    onNavigateToAbout = { backStack.add(NavKeys.About) },
                    onNavigateToSearch = { backStack.add(NavKeys.Search) }
                )
            }
            entry<NavKeys.Search> {
                val vm = hiltViewModel<SearchViewModel, SearchViewModel.Factory>(
                    creationCallback = { factory -> factory.create() }
                )
                SearchScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEventDetail = { eventSlug ->
                        backStack.add(NavKeys.EventDetail(eventSlug))
                    }
                )
            }
            entry<NavKeys.EventDetail> { key ->
                val vm = hiltViewModel<EventDetailViewModel, EventDetailViewModel.Factory>(
                    creationCallback = { factory -> factory.create(key) }
                )
                EventDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToMarketDetail = { marketId ->
                        backStack.add(NavKeys.MarketDetail(marketId))
                    },
                    onNavigateToUserProfile = { userAddress ->
                        backStack.add(NavKeys.UserProfile(userAddress))
                    }
                )
            }
            entry<NavKeys.MarketDetail> { key ->
                val vm = hiltViewModel<MarketDetailViewModel, MarketDetailViewModel.Factory>(
                    creationCallback = { factory -> factory.create(key) }
                )
                MarketDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<NavKeys.UserProfile> { key ->
                val vm = hiltViewModel<UserProfileViewModel, UserProfileViewModel.Factory>(
                    creationCallback = { factory -> factory.create(key) }
                )
                UserProfileScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onEventClick = { eventSlug ->
                        backStack.add(NavKeys.EventDetail(eventSlug))
                    }
                )
            }
            entry<NavKeys.About> {
                val vm = hiltViewModel<AboutViewModel, AboutViewModel.Factory>(
                    creationCallback = { factory -> factory.create() }
                )
                AboutScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}