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
    const val EVENT_ID_ARG = "eventId"
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
                // Use eventId for the new route
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
                onNavigateToEventDetail = { eventId ->
                    navController.navigate("${AppDestinations.EVENT_DETAIL}/$eventId")
                },
            )
        }

        // Market details screen
        composable(
            route = "${AppDestinations.MARKET_DETAIL}/{${AppDestinations.MARKET_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.MARKET_ID_ARG) { type = NavType.StringType })
        ) {
            // val marketId = it.arguments?.getString(AppDestinations.MARKET_ID_ARG)
            MarketDetailScreen(
                // marketId is no longer passed directly, ViewModel gets it from SavedStateHandle
                onNavigateBack = { navController.popBackStack() } // Add "Back" handler
            )
        }

        // Event details screen
        composable(
            route = "${AppDestinations.EVENT_DETAIL}/{${AppDestinations.EVENT_ID_ARG}}",
            arguments = listOf(navArgument(AppDestinations.EVENT_ID_ARG) { type = NavType.StringType })
        ) {
            EventDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                // Pass lambda for navigation to specific MARKET details from this screen
                onNavigateToMarketDetail = { marketId ->
                     navController.navigate("${AppDestinations.MARKET_DETAIL}/$marketId")
                },
                // Pass lambda for navigation to user profile
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
                 onEventClick = { eventId ->
                     navController.navigate("${AppDestinations.EVENT_DETAIL}/$eventId")
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