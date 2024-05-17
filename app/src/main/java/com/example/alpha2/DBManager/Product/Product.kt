package com.example.alpha2.DBManager.Product

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.time.LocalDateTime

//商品主檔(原POS2004)
@Entity(tableName = "Products")
data class Product (
  @PrimaryKey
  val pId: String,                 //商品編號
  val imageUrl: String = "0",      //商品圖片url (無圖片默認值為0)
  val pName: String,               //商品名稱
  val pType: String,               //商品分類 (特定類別，eg:生鮮會顯示簡易搜尋bar，或是沒有條碼的商品)
  val pluUnit: String,             //商品單位
  val pluType: String,             //商品類別

  /*  0=原料 1=一般商品 2=生鮮商品 3=代收 4=代付 */
  /*  5=代售 6=組合商品 7=充值 8=專櫃/購買子會員 9=寄賣/記次消費 */
  /*  A=贈品 B=禮券/會籍/課程(點數)消費*/
  /*  C=股金 D=年費 E=加成 F=折讓(該商品數量為負) G=紅利點數 */
  /*  01=正常商品 04=S/O商品 CO=寄售商品 CC=專櫃商品*/
  /*  71=Deposit 72=安裝商品 73=運費商品 74=工資商品*/
  /*  75=折價券商品 77=溢收商品*/
  /*商品類別小於70為非商品銷售 */

//  商品類別
  val DEP_No :String ?= null,       /*部門編號*/
  val CAT_No :String ?= null,       /*分類編號*/
  val VEN_No :String ?= null,       /*廠商編號*/

  val mamMethod: String,           //促銷方式 (0表示沒有促銷)

  /*1 永久變價*/
  /*H 降價促銷*/
  /*I 印花(卷)*/
  /*B 同單價不同組合促銷*/
  /*C 不同單價不同組合促銷*/
  /*D By Group買X元享Y%off折扣*/
  /*E By group 買滿X元送贈品*/
  /*F 買A Product Group送*/
  /*G By Group 每買N個中的M個可享折扣*/
  /*P 同單價的不同商品任選 X1 個總價 Y1元、任選 X2 個總價 Y2元...任選 Xn 個總價 Yn 元*/

  val pluMagNo: String,            //商品條碼 (管理碼)
  val pluDisc: String ?= "Y",      //是否可折扣 (Y/N)
  val pNumber: Int,                //商品數量
  val fixPrc: Double,                 //商品定價
  val salePrc: Double,                //商品售價
  val unitPrc: Double,                //促銷單價
  val memPrc: Double,                 //會員價

  val mmpBegDate: LocalDateTime? = null,            //促銷開始日期
  val mmpEndDate: LocalDateTime? = null,            //促銷結束日期 (不用考慮超過期限，價格更新於回價時處理)
 ): Serializable