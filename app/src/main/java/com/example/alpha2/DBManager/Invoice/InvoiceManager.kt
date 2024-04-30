package com.example.alpha2.DBManager.Invoice

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking

class InvoiceManager(context: Context) {
    private val db: InvoiceDatabase = Room.databaseBuilder(
        context,
        InvoiceDatabase::class.java, "invoice-database"
    ).build()

    private val invoiceDao: InvoiceDao = db.invoiceDao()

    //新增會員
    fun addInvoiceSetup(invoiceSetup: InvoiceSetup) {
        runBlocking {
            invoiceDao.insert(invoiceSetup)
        }
    }

    //檢查是否有符合id的會員
    fun deleteInvoiceSetup(invoiceSetup: InvoiceSetup) {
        return runBlocking {
            invoiceDao.delete(invoiceSetup)
        }
    }

    //檢查是否有符合卡號的會員
    fun getInvoiceSetupsBy(STO_No: String,GUI_YYMM: String,POS_NOS: String,SER_NOS: String): InvoiceSetup?{
        return runBlocking {
            invoiceDao.getInvoiceSetupsBy(STO_No, GUI_YYMM, POS_NOS, SER_NOS)
        }
    }
}