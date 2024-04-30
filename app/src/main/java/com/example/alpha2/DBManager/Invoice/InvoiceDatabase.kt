package com.example.alpha2.DBManager.Invoice

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InvoiceSetup::class], version = 1)

abstract class InvoiceDatabase : RoomDatabase(){
    abstract fun invoiceDao(): InvoiceDao
}