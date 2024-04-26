package com.example.alpha2.DBManager.System

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.alpha2.DBManager.Product.Converters
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [SystemSetting::class, CashSystem::class, CashState::class, PaymentMethod::class], version = 1)
@TypeConverters(Converters::class)


abstract class SystemDataBase: RoomDatabase(){
    abstract fun systemDao(): SystemDao
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