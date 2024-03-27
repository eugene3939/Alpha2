package com.example.alpha2.DBManager.Product

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDateTime

//在
@Entity(tableName = "ScanProducts")

data class ScanProduct(
    @PrimaryKey
    val pId: String,                 //商品編號
    val pluMagNo: String,            //商品條碼 (管理碼)
    var selectedQuantity: Int = 0,   //購物車選擇數量
): Serializable