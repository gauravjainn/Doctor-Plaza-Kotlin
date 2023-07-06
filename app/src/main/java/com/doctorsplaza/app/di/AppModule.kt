package com.doctorsplaza.app.di

import android.content.Context
import com.doctorsplaza.app.utils.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) =
        SessionManager(context)
}