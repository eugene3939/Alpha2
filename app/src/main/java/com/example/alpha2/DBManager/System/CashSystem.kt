package com.example.alpha2.DBManager.System

import androidx.room.Entity
import androidx.room.PrimaryKey

//收銀機系統設定檔 (POS1002)
@Entity(tableName = "CashSystems")

data class CashSystem (
    @PrimaryKey
    val ecrNo: String,     //收銀機代碼
    val ecrName: String    //收銀機名稱
)