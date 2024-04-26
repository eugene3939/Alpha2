package com.example.alpha2.DBManager.Payment

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PaymentDao {

    //---以下為付款主檔---

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