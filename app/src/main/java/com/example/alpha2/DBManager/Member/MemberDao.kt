package com.example.alpha2.DBManager.Member

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
@Dao
interface MemberDao {
    //檢查是否有符合id的會員
    @Query("SELECT * FROM Members WHERE id = :mId")
    suspend fun getMemberById(mId: String): Member?

    //檢查是否有符合卡號的會員
    @Query("SELECT * FROM Members WHERE cardNo = :cardNo")
    suspend fun getMemberByCardNo(cardNo: String): Member?

    @Insert
    suspend fun insert(member: Member)

    @Delete
    suspend fun delete(member: Member)
}