package com.example.alpha2.DBManager.Member

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.alpha2.DBManager.Product.Converters

@Database(entities = [Member::class], version = 1)
@TypeConverters(Converters::class)

abstract class MemberDatabase : RoomDatabase(){
    abstract fun memberDao(): MemberDao
}