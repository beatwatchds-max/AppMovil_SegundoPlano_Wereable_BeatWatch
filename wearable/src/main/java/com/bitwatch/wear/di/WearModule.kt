package com.bitwatch.wear.di

import android.content.Context
import com.bitwatch.wear.data.DataLayerManager
import com.bitwatch.wear.data.HeartRateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WearModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideHeartRateRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): HeartRateRepository {
        return HeartRateRepository(context)
    }

    @Provides
    @Singleton
    fun provideDataLayerManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
        scope: CoroutineScope
    ): DataLayerManager {
        return DataLayerManager(context, scope)
    }
}