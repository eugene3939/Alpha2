package com.example.alpha2.DBManager.System

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SystemDao {
    //系統設定檔
    @Query("SELECT * FROM SystemSettings WHERE ecrNo = :eId")   //單一收集機號對應單一設定檔案
    suspend fun getSystemSettingNoById(eId: String): SystemSetting?

    @Insert
    suspend fun insert(system: SystemSetting)

    @Delete
    suspend fun delete(system: SystemSetting)

    //收銀機設定檔

    //尋找是否有對應的收銀機號
    @Query("SELECT * FROM CashSystems WHERE ecrNo = :eId")
    suspend fun getCashSystemNoById(eId: String): CashSystem?
    @Insert
    suspend fun insert(cashSystems: CashSystem)

    @Delete
    suspend fun delete(cashSystems: CashSystem)

    //收銀機狀態檔

    //尋找是否有對應的收銀機號
    @Query("SELECT * FROM CashStates WHERE ecrNo = :eId")
    suspend fun getCashStateNoById(eId: String): CashState?

    @Insert
    suspend fun insert(cashStates: CashState)

    @Delete
    suspend fun delete(cashStates: CashState)

    //付款方式檔

    //尋找是否有對應的付款方式
    @Query("SELECT * FROM PaymentMethods WHERE PAY_No = :payNo")
    suspend fun getPaymentMethodById(payNo: String): PaymentMethod?

    @Insert
    suspend fun insert(paymentMethod: PaymentMethod)

    @Delete
    suspend fun delete(paymentMethod: PaymentMethod)
}