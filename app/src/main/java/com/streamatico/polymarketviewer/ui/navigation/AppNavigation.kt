package com.streamatico.polymarketviewer.ui.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
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
        transitionSpec = { horizontalSlideTransition(forward = true) },
        popTransitionSpec = { horizontalSlideTransition(forward = false) },
        predictivePopTransitionSpec = { horizontalSlideTransition(forward = false) },
        entryProvider = entryProvider {
            entry<NavKeys.EventList> {
                val vm = koinViewModel<EventListViewModel>()
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
                val vm = koinViewModel<SearchViewModel>()
                SearchScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToEventDetail = { eventSlug ->
                        backStack.add(NavKeys.EventDetail(eventSlug))
                    }
                )
            }
            entry<NavKeys.EventDetail> { key ->
                val vm = koinViewModel<EventDetailViewModel> { parametersOf(key) }
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
                val vm = koinViewModel<MarketDetailViewModel> { parametersOf(key) }
                MarketDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
            entry<NavKeys.UserProfile> { key ->
                val vm = koinViewModel<UserProfileViewModel> { parametersOf(key) }
                UserProfileScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onEventClick = { eventSlug ->
                        backStack.add(NavKeys.EventDetail(eventSlug))
                    }
                )
            }
            entry<NavKeys.About> {
                val vm = koinViewModel<AboutViewModel>()
                AboutScreen(
                    viewModel = vm,
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

private fun horizontalSlideTransition(forward: Boolean) =
    slideInHorizontally { if (forward) it else -it } togetherWith
        slideOutHorizontally { if (forward) -it else it } + fadeOut()
