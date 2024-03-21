package com.example.alpha2.DBManager.System

import androidx.room.Entity
import androidx.room.PrimaryKey

//收銀機狀態檔
@Entity(tableName = "CashStates")

data class CashState (
    @PrimaryKey
    val ecrNo: String,      //收銀機代碼
    val userNo: String,     //作業中的收銀員代碼
    val userLevel: String   //權限等級
)