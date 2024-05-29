package com.example.alpha2.DBManager.Product

import androidx.room.Entity

//原POS2011 (組合商品子項: 主檔(2004)的項目這邊也會有)
@Entity(tableName = "PairedProducts", primaryKeys = ["CMB_No","PLU_No","CMB_UnitPrice"])

data class PairedProduct(
    //Primary key
    val CMB_No:              String        ,   /*組合編號*/
    val PLU_No:              String        ,   /*商品編號*/
    val CMB_UnitPrice:       Double        ,   /*組合單價,同POS2004商品主檔售價*/

    val CMB_QTY:             Int           ,   /*組合數量*/
    val CMB_Disc:            Double        ,   /*單價折扣(負值),表示一個商品之折扣*/
    val CMB_Change:          String        ,   /*商品是否可退換*/
    val CMB_Type:            String = "0",     /*價格種類 0=全部一致 1=售價 2=會員價 3=特價 4=VIP價*/
)
