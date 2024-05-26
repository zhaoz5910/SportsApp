package com.zhangzhao.sportsapp.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zhangzhao.sportsapp.model.RopeSkipping

import com.zhangzhao.sportsapp.model.Run
import com.zhangzhao.sportsapp.util.Converters

@Database(
    entities = [Run::class, RopeSkipping::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SportDatabase: RoomDatabase() {

    abstract fun getRunDao():RunDao
    abstract fun getRopeDao():RopeSkippingDao
}