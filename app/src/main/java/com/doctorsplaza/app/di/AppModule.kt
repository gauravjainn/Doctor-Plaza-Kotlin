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

/*
    @Singleton
    @Provides
    fun provideRepository(dao:AppDatabaseDao) = Repository(dao)
*/

/*

    @Singleton
    @Provides
    fun provideAgileLoader(@ApplicationContext context: Context) = AgileLoader(context)
*/

    @Singleton
    @Provides
    fun provideSessionManager(@ApplicationContext context: Context) =
        SessionManager(context)

    /*@Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context):AppDataBase =
        Room.databaseBuilder(context,AppDataBase::class.java,"gym_app_db").fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()

    @Singleton
    @Provides
    fun provideAppDao(db: AppDataBase) =
        db.getAppDao()*/


}