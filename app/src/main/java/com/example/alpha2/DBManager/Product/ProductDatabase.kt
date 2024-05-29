package com.example.alpha2.DBManager.Product

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [Product::class, CouponMain::class, CouponDetail::class, PairedProduct::class, ScanProduct::class], version = 1)
@TypeConverters(Converters::class)

abstract class ProductDatabase : RoomDatabase(){
    abstract fun ProductDao(): ProductDao
}

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }
}