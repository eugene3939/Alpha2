package com.example.alpha2.DBManager.Payment

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking

class PaymentManager(context: Context) {
    private val db: PaymentDatabase = Room.databaseBuilder(
        context,
        PaymentDatabase::class.java, "payment-database"
    ).build()

    private val paymentDao: PaymentDao = db.PaymentDao()

    //--即時銷售主檔--

    //新增銷售主檔
    fun addPaymentMain(paymentMain: PaymentMain) {
        runBlocking {
            paymentDao.insert(paymentMain)
        }
    }

    //檢查是否有符合店號的銷售主檔
    fun getPaymentMainByStoreNo(storeNo: String): PaymentMain? {
        return runBlocking {
            paymentDao.getPaymentMainByStoreNo(storeNo)
        }
    }

    //--即時銷售明細檔--

    //新增銷售主檔
    fun addPaymentDetail(paymentDetail: PaymentDetail) {
        runBlocking {
            paymentDao.insert(paymentDetail)
        }
    }

    //檢查是否有符合店號的銷售主檔
    fun getPaymentDetailByStoreNo(storeNo: String): PaymentDetail? {
        return runBlocking {
            paymentDao.getPaymentDetailByStoreNo(storeNo)
        }
    }

    //--即時銷售明細檔--

    //新增銷售明細檔
    fun addPaymentDetail(paymentShowUp: PaymentshowUp) {
        runBlocking {
            paymentDao.insert(paymentShowUp)
        }
    }

    //檢查是否有符合店號的銷售明細檔
    fun getPaymentShowByStoreNo(storeNo: String): PaymentshowUp? {
        return runBlocking {
            paymentDao.getPaymentShowByStoreNo(storeNo)
        }
    }
}