package com.example.alpha2.DBManager.Invoice

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface InvoiceDao {
    //檢查是否有符合的發票號碼設定檔
    @Query("SELECT * FROM InvoiceSetups WHERE STO_No=:STO_No AND GUI_YYMM=:GUI_YYMM AND POS_NOS=:POS_NOS AND SER_NOS=:SER_NOS")
    suspend fun getInvoiceSetupsBy(STO_No: String,GUI_YYMM: String,POS_NOS: String,SER_NOS: String): InvoiceSetup?

    @Insert
    suspend fun insert(invoiceSetup: InvoiceSetup)

    @Delete
    suspend fun delete(invoiceSetup: InvoiceSetup)

    //變更發票的下一個序號
    @Query("UPDATE InvoiceSetups SET GUI_SNOS = :GUI_SNOS, NEXT_SNOS = :NEXT_SNOS WHERE STO_No = :STO_No AND GUI_YYMM = :GUI_YYMM")
    suspend fun updateGUI_NEXT(GUI_SNOS: String,NEXT_SNOS: String,STO_No: String,GUI_YYMM: String)

    //變更發票的使用狀態
    @Query("UPDATE InvoiceSetups SET STATUS = :status WHERE STO_No = :STO_No AND GUI_YYMM = :GUI_YYMM")
    suspend fun updateStatus(status: String,STO_No: String,GUI_YYMM: String)
}