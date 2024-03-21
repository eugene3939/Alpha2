package com.example.alpha2.DBManager.System

import androidx.room.Entity
import androidx.room.PrimaryKey

//收銀機狀態檔 (原POS1003)
@Entity(tableName = "CashStates")

data class CashState (
    @PrimaryKey
    val ecrNo: String,      //收銀機代碼
    val userNo: String,     //作業中的收銀員代碼
    val userLevel: String,  //權限等級
    val txnNextNo: Int = 0,    //下一個可使用交易序號
    val guiBegNo: String = "A",  //起始發票號碼
    val guiNextNo: String = "B", //下一個可使用發票號碼
    val guiEndNo: String = "C"   //最後一個發票號碼
)