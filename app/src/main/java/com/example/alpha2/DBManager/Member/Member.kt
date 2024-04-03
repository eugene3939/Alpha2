package com.example.alpha2.DBManager.Member

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "Members")

data class Member(
    @PrimaryKey
    val id: String,                          //用戶id
    val type: String = "1",                  //卡別 1:個人 2:公司
    val name: String,                        //用戶名稱
    val birthday: String? = null,            //用戶生日
    val phone: String? = null,               //用戶電話
    val email: String? = null,               //用戶郵件
    val issueDate: LocalDateTime,            //發卡日期
    val expireDate: LocalDateTime,           //到期日期
    val status: String = "0",                //會員狀態 0=正常 1=無效 9=申請中
    val discRate: Double,                    //會員折扣率
    val stockFee: Int = 0,                   //累積點數
    val cardNo: String)                      //會員卡卡號
