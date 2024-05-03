package com.example.alpha2.DBManager.System

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import java.lang.System

class SystemManager(context: Context) {
    private val db: SystemDataBase = Room.databaseBuilder(
        context,
        SystemDataBase::class.java, "system-database"
    ).build()

    private val systemDao: SystemDao = db.systemDao()

    //---以下為系統設定檔---

    //檢查是否有收銀機號id的資料
    fun getSystemSettingNoById(ecrNo: String): SystemSetting? {
        return runBlocking {
            systemDao.getSystemSettingNoById(ecrNo)
        }
    }

    fun addSystem(s: SystemSetting) {
        runBlocking {
            systemDao.insert(s)
        }
    }

    //---以下為收銀機設定檔---

    //檢查是否有收銀機號id的資料
    fun getCashSystemNoById(ecrNo: String): CashSystem? {
        return runBlocking {
            systemDao.getCashSystemNoById(ecrNo)
        }
    }

    //新增設定檔
    fun addCashState(cashState: CashState) {
        runBlocking {
            systemDao.insert(cashState)
        }
    }

    //---以下為收銀機狀態檔---

    //檢查是否有收銀機號id的資料
    fun getCashStateNoById(eId: String): CashState? {
        return runBlocking {
            systemDao.getCashStateNoById(eId)
        }
    }

    //新增狀態檔
    fun addCashSystem(cashSystem: CashSystem) {
        runBlocking {
            systemDao.insert(cashSystem)
        }
    }

    //---以下為付款方式檔---

    //取得所有的付款方式
    fun getAllPaymentMethod(): MutableList<PaymentMethod>? {
        return runBlocking {
            systemDao.getAllPaymentMethod()
        }
    }

    //檢查是否有收銀機號id的資料
    fun getPaymentMethodById(payNo: String): PaymentMethod? {
        return runBlocking {
            systemDao.getPaymentMethodById(payNo)
        }
    }

    //新增付款方式
    fun addPaymentMethod(paymentMethod: PaymentMethod) {
        runBlocking {
            systemDao.insert(paymentMethod)
        }
    }

    //刪除付款方式
    fun deletePaymentMethod(paymentMethod: PaymentMethod){
        runBlocking {
            systemDao.delete(paymentMethod)
        }
    }
}