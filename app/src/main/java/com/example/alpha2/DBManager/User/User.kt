package com.example.alpha2.DBManager.User

import androidx.room.Entity
import androidx.room.PrimaryKey

//user table
@Entity(tableName = "Users")
data class User (
    @PrimaryKey
        val id: String,                    //用戶id
        val name: String,                  //用戶名稱
        val account: String,               //用戶帳號
        val password: String,              //用戶密碼
        val authentication: String)        //用戶權限