package com.example.alpha2.DBManager.User

import androidx.room.Entity
import androidx.room.PrimaryKey

//user table (用戶資料表) 2001
@Entity(tableName = "Users")
data class User (
    @PrimaryKey
    val id: String,                    //用戶id
    val name: String,                  //用戶名稱
    val account: String,               //用戶帳號
    val password: String,              //用戶密碼
    val level: String = "0",           //鍵盤等級(0-9) 個別鍵盤樣式  ， 預設為0
    val authentication: Int = 1)       //授權等級(0-99) 店員...店長 ， 預設為1