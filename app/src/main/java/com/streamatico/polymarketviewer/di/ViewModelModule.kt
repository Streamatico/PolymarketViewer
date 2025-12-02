package com.streamatico.polymarketviewer.di

import com.streamatico.polymarketviewer.ui.about.AboutViewModel
import com.streamatico.polymarketviewer.ui.event_detail.EventDetailViewModel
import com.streamatico.polymarketviewer.ui.event_list.EventListViewModel
import com.streamatico.polymarketviewer.ui.market_detail.MarketDetailViewModel
import com.streamatico.polymarketviewer.ui.search_screen.SearchViewModel
import com.streamatico.polymarketviewer.ui.user_profile.UserProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for presentation layer ViewModels.
 * All ViewModels are registered here for dependency injection.
 */
val viewModelModule = module {
    viewModelOf(::EventListViewModel)
    viewModelOf(::EventDetailViewModel)
    viewModelOf(::MarketDetailViewModel)
    viewModelOf(::UserProfileViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::AboutViewModel)
}
