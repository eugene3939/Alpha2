package com.example.alpha2.DBManager.Product

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

//折價券主檔(原POS2087)
@Entity(tableName = "CouponMains")
data class CouponMain (
    @PrimaryKey
    val DISC_PLU_MagNo: String,                    //折價券貨號
    val DISC_TYPE: String,                       //*種類 0=折扣券(飲料類) 1=折價券(金額) 2=打折券(滿足條件[單價最高其中一個])
    val MIN_AMT: Int = 0,                        //購物條件金額 0表示不限制
    val MAX_PAGE: Int = 0,                       //限用張數 0表示不限制
    val FROM_DATE: LocalDateTime?= null,         //開始日期
    val TO_DATE: LocalDateTime?= null,           //結束日期
    val VIP_CD: String = "Y",                    //適用會員別 Y=一般會員 N=所有會員 A=限制會員卡號 POS2204 B=排除會員卡別 POS2095
    val DISC_CD: String = "N",                    //不可折商品適用否 (Y/N)
    val BASE_TYPE: String = "0"                  //條件金額限制指定類別否
                                                //0:不限制指定類別 不用看
                                                //1:限制指定類別,符合類別的商品才計入條件金額
                                                //2:限制指定類別,符合類別的商品不計入條件金額,除此之外都算
)