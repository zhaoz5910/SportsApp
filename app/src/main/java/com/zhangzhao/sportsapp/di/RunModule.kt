package com.zhangzhao.sportsapp.di

import android.app.Application
import androidx.room.Room
import com.zhangzhao.sportsapp.model.Constants.RUN_DATABASE_NAME
import com.zhangzhao.sportsapp.room.RunDao
import com.zhangzhao.sportsapp.room.RunDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RunModule {
    @Singleton
    @Provides
    fun provideAppDb(app: Application): RunDatabase {
        return Room.databaseBuilder(app, RunDatabase::class.java, RUN_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: RunDatabase): RunDao {
        return db.getRunDao()
    }
}