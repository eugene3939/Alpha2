package com.example.alpha2.DBManager.System

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SystemSetting::class, CashSystem::class, CashState::class], version = 1)

abstract class SystemDataBase: RoomDatabase(){
    abstract fun systemDao(): SystemDao
}