package com.zhangzhao.sportsapp.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.zhangzhao.sportsapp.model.Run
import com.zhangzhao.sportsapp.util.Converters

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RunDatabase: RoomDatabase() {

    abstract fun getRunDao():RunDao
}