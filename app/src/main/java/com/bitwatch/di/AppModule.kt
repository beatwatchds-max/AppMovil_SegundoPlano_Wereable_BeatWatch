package com.bitwatch.di

import android.content.Context
import com.bitwatch.data.DataLayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @Singleton
    fun provideDataLayerManager(
        @dagger.hilt.android.qualifiers.ApplicationContext context: Context,
        scope: CoroutineScope
    ): DataLayerManager {
        return DataLayerManager(context, scope)
    }
}