package com.example.alpha2.DBManager.Member

import android.content.Context
import androidx.room.Room
import androidx.room.TypeConverter
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MemberManager(context: Context) {
    private val db: MemberDatabase = Room.databaseBuilder(
        context,
        MemberDatabase::class.java, "member-database"
    ).build()

    private val memberDao: MemberDao = db.memberDao()

    //新增會員
    fun addMember(member: Member) {
        runBlocking {
            memberDao.insert(member)
        }
    }

    //檢查是否有符合id的會員
    fun getMemberById(mId: String): Member? {
        return runBlocking {
            memberDao.getMemberById(mId)
        }
    }

    //檢查是否有符合卡號的會員
    fun getMemberByCardNo(cardNo: String): Member?{
        return runBlocking {
            memberDao.getMemberByCardNo(cardNo)
        }
    }
}

object Converters {
    @TypeConverter
    @JvmStatic
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? {
        return dateTime?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @TypeConverter
    @JvmStatic
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
    }
}