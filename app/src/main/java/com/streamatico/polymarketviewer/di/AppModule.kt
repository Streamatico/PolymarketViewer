package com.streamatico.polymarketviewer.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.streamatico.polymarketviewer.data.repository.PolymarketRepositoryImpl
import com.streamatico.polymarketviewer.domain.repository.PolymarketRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val USER_PREFERENCES = "user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Provides DataStore<Preferences> --- //
    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface AppModuleBinder {
    @Binds
    @Singleton
    fun bindPolymarketRepository(impl: PolymarketRepositoryImpl): PolymarketRepository
}