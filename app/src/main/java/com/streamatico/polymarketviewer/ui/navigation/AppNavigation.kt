package com.streamatico.polymarketviewer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.streamatico.polymarketviewer.ui.event_detail.EventDetailScreen
import com.streamatico.polymarketviewer.ui.event_list.EventListScreen
import com.streamatico.polymarketviewer.ui.market_detail.MarketDetailScreen
import com.streamatico.polymarketviewer.ui.user_profile.UserProfileScreen
import com.streamatico.polymarketviewer.ui.about.AboutScreen
import com.streamatico.polymarketviewer.ui.search_screen.SearchScreen

// Define routes
object AppDestinations {
    const val EVENT_LIST = "eventList"
    const val MARKET_DETAIL = "marketDetail"
    const val EVENT_DETAIL = "eventDetail"
    const val USER_PROFILE = "userProfile"
    const val ABOUT_APP = "aboutApp"
    const val SEARCH = "search"
    const val MARKET_ID_ARG = "marketId"
    const val EVENT_SLUG_ARG = "eventSlug"
    const val USER_ADDRESS_ARG = "userAddress"
}

@Composable
fun AppNavigation(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = AppDestinations.EVENT_LIST) {
        // Event list screen
        composable(AppDestinations.EVENT_LIST) {
            EventListScreen(
                onNavigateToEventDetail = { eventId ->
                    navController.navigate("${AppDestinations.EVENT_DETAIL}/$eventId")
                },
                onNavigateToAbout = { navController.navigate(AppDestinations.ABOUT_APP) },
                onNavigateToSearch = { navController.navigate(AppDestinations.SEARCH) }
            )
        }

        // Search screen
        composable(AppDestinations.SEARCH) {
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEventDetail = { eventSlug ->
                    navController.navigate("${AppDestinations.EVENT_DETAIL}/$eventSlug")
                },
            )
        }

        // Market details screen
        composable(
            route = "${AppDestinations.MARKET_DETAIL}/{${AppDestinations.MARKET_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.MARKET_ID_ARG) { type = NavType.StringType })
        ) {
            MarketDetailScreen(
                onNavigateBack = { navController.popBackStack() } // Add "Back" handler
            )
        }

        // Event details screen
        composable(
            route = "${AppDestinations.EVENT_DETAIL}/{${AppDestinations.EVENT_SLUG_ARG}}",
            arguments = listOf(navArgument(AppDestinations.EVENT_SLUG_ARG) { type = NavType.StringType })
        ) {
            EventDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMarketDetail = { marketId ->
                     navController.navigate("${AppDestinations.MARKET_DETAIL}/$marketId")
                },
                onNavigateToUserProfile = { userAddress ->
                    navController.navigate("${AppDestinations.USER_PROFILE}/$userAddress")
                }
            )
        }

        // User profile screen
        composable(
            route = "${AppDestinations.USER_PROFILE}/{${AppDestinations.USER_ADDRESS_ARG}}",
            arguments = listOf(navArgument(AppDestinations.USER_ADDRESS_ARG) { type = NavType.StringType })
        ) {
             UserProfileScreen(
                 onNavigateBack = { navController.popBackStack() },
                 onEventClick = { eventSlug ->
                     navController.navigate("${AppDestinations.EVENT_DETAIL}/$eventSlug")
                 }
             )
        }

        // Add composable for AboutScreen
        composable(AppDestinations.ABOUT_APP) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}