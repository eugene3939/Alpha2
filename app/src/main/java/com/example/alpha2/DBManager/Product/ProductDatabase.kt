package com.example.alpha2.DBManager.Product

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Product::class, ClusterProduct::class, DiscountProduct::class], version = 1)

abstract class ProductDatabase : RoomDatabase(){
    abstract fun ProductDao(): ProductDao
}