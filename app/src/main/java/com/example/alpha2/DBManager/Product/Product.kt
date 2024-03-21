package com.example.alpha2.DBManager.Product

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Products")
data class Product (
  @PrimaryKey
  val pId: String,                 //商品編號
  val imageUrl: String = "0",      //商品圖片url (無圖片默認值為0)
  val pName: String,               //商品名稱
  val pType: String,               //商品類別
  val pBarcode: String,            //商品條碼
  val pNumber: Int,                //商品數量
  val pPrice: Int,                 //商品價格
  var selectedQuantity: Int = 0,   //購物車選擇數量
 ): Serializable