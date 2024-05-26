package com.zhangzhao.sportsapp.di

import android.app.Application
import androidx.room.Room
import com.zhangzhao.sportsapp.model.Constants.SPORT_DATABASE_NAME
import com.zhangzhao.sportsapp.room.RopeSkippingDao
import com.zhangzhao.sportsapp.room.RunDao
import com.zhangzhao.sportsapp.room.SportDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SportModule {
    @Singleton
    @Provides
    fun provideRunDb(app: Application): SportDatabase {
        return Room.databaseBuilder(app, SportDatabase::class.java, SPORT_DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: SportDatabase): RunDao {
        return db.getRunDao()
    }

    @Singleton
    @Provides
    fun provideRopeDao(db: SportDatabase): RopeSkippingDao {
        return db.getRopeDao()
    }
}