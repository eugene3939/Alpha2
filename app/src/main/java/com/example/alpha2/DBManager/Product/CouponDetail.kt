package com.example.alpha2.DBManager.Product

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

//折價券明細檔(原POS2088)
@Entity(tableName = "CouponDetails")
data class CouponDetail(
    @PrimaryKey
    val DISC_PLU_MagNo: String,                         /*折價券貨號*/
    val FROM_DATE: LocalDateTime ?= null,               /*開始日期*/
    val TO_DATE: LocalDateTime ?= null,                 /*結束日期*/
    val SEQ_NO: Int,                                    /*序號-------------------------*/
    val SDEP_No: String ?= null,                        /*適用次部門*/
    val CAT_No: String ?= null,                         /*適用主分類*/
    val SCAT_No: String ?= null,                        /*適用次分類*/
    val VEN_No: String ?= null,                         /*適用廠商編號*/
    val PLU_MagNo: String ?= null,                      /*適用貨號---------------------*/
    val CRT_Date: LocalDateTime?= null,                 /*建立日期*/
    val UPD_Date: LocalDateTime?= null,                 /*變更日期*/
    val USR_No: String ?= null,                         /*輸入人員*/
    val DEP_No: String ?= null,                         /*部門編號----------------*/
    val PLU_Source: String ?= null,                     /*是否為自製品*/
    val DISC_RATE: Int ?= null,                         /*折扣比率*/
)
