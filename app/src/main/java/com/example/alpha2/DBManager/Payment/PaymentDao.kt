package com.example.alpha2.DBManager.Payment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import java.time.LocalDateTime

@Dao
interface PaymentDao {

    //---以下為付款主檔---

    //尋找對應的發票號碼
    @Query("SELECT * FROM PaymentMains WHERE TXN_GUIBegNo = :TXN_GUIBegNo")
    suspend fun searchPaymentMainByTXN_GUINo(TXN_GUIBegNo: String): PaymentMain?

    //尋找對應日期的交易序號
    @Query("SELECT MAX(TXN_No) FROM PaymentMains WHERE TXN_Date = :YYMM")
    suspend fun searchPaymentMainByMaxYYMM(YYMM: LocalDateTime): Int?

    //檢查是否有符合id的付款主檔
    @Query("SELECT * FROM PaymentMains WHERE SYS_StoreNo = :storeNo")
    suspend fun getPaymentMainByStoreNo(storeNo: String): PaymentMain?

    @Insert
    suspend fun insert(paymentMain: PaymentMain)

    @Delete
    suspend fun delete(paymentMain: PaymentMain)

    //---以下為付款明細檔---

    //檢查是否有符合id的付款明細檔
    @Query("SELECT * FROM PaymentDetails WHERE SYS_StoreNo = :pId")
    suspend fun getPaymentDetailById(pId: String): PaymentDetail?

    //檢查是否有符合店號的明細檔
    @Query("SELECT * FROM PaymentDetails WHERE SYS_StoreNo = :pId")
    suspend fun getPaymentDetailByStoreNo(pId: String): PaymentDetail?

    @Insert
    suspend fun insert(paymentDetail: PaymentDetail)

    @Delete
    suspend fun delete(paymentDetail: PaymentDetail)

    //---以下為即時銷售付款付款檔---

    //檢查是否有符合id的付款檔
    @Query("SELECT * FROM PaymentshowUps WHERE SYS_StoreNo = :pId")
    suspend fun getPaymentShowById(pId: String): PaymentshowUp?

    //檢查是否有符合店號的付款檔
    @Query("SELECT * FROM PaymentshowUps WHERE SYS_StoreNo = :pId")
    suspend fun getPaymentShowByStoreNo(pId: String): PaymentshowUp?

    @Insert
    suspend fun insert(paymentShowUp: PaymentshowUp)

    @Delete
    suspend fun delete(paymentShowUp: PaymentshowUp)
}