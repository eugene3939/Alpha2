package com.example.alpha2.DBManager.Member

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.alpha2.DBManager.User.User

@Dao
interface MemberDao {
    //檢查是否有符合id的會員
    @Query("SELECT * FROM Members WHERE id = :mId")
    suspend fun getMemberById(mId: String): Member?

    @Insert
    suspend fun insert(member: Member)

    @Delete
    suspend fun delete(member: Member)
}