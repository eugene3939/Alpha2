package com.example.alpha2.DBManager.User

import androidx.room.Room
import kotlinx.coroutines.runBlocking
import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class UserManager(context: Context){
    private val db: UserDatabase = Room.databaseBuilder(
        context,
        UserDatabase::class.java, "user-database"
    ).build()

    private val userDao: UserDao = db.userDao()

    //新增用戶
    fun addUser(user: User) {
        runBlocking {
            userDao.insert(user)
        }
    }

    //檢查是否有符合id的用戶
    fun getUserById(uId: String): User? {
        return runBlocking {
            userDao.getUserById(uId)
        }
    }

    //檢查是否有符合帳號密碼的用戶
    fun loginByAccPas(acc: String, pas: String): User? {
        return runBlocking {
            userDao.loginByAccPas(acc, pas)
        }
    }
}