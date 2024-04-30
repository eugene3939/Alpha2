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

    //新增發票號碼設定檔
    fun addInvoiceSetup(invoiceSetup: InvoiceSetup) {
        runBlocking {
            invoiceDao.insert(invoiceSetup)
        }
    }

    //刪除發票號碼設定檔
    fun deleteInvoiceSetup(invoiceSetup: InvoiceSetup) {
        return runBlocking {
            invoiceDao.delete(invoiceSetup)
        }
    }

    //更新發票號
    fun updateGUI_NEXT(GUI_SNOS: String,NEXT_SNOS: String,STO_No: String,GUI_YYMM: String){
        return runBlocking {
            invoiceDao.updateGUI_NEXT(GUI_SNOS,NEXT_SNOS,STO_No,GUI_YYMM)
        }
    }

    //檢查是否有符合卡的發票號碼設定檔
    fun getInvoiceSetupsBy(STO_No: String,GUI_YYMM: String,POS_NOS: String,SER_NOS: String): InvoiceSetup?{
        return runBlocking {
            invoiceDao.getInvoiceSetupsBy(STO_No, GUI_YYMM, POS_NOS, SER_NOS)
        }
    }
}