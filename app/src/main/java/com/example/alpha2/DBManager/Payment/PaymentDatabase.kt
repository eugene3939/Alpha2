package com.example.alpha2.DBManager.Payment

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.alpha2.DBManager.Product.Converters
import com.example.alpha2.DBManager.Product.ProductDao
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [PaymentMain::class, PaymentDetail::class, PaymentshowUp::class], version = 1)
@TypeConverters(Converters::class)

abstract class PaymentDatabase: RoomDatabase() {
    abstract fun PaymentDao(): PaymentDao
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