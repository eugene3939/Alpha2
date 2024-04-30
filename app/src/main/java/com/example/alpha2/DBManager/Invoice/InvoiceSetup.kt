package com.example.alpha2.DBManager.Invoice

import androidx.room.Entity

/*發票號碼設定檔 (POS1018) */

@Entity(tableName = "InvoiceSetups", primaryKeys = ["STO_No", "GUI_YYMM", "POS_NOS", "SER_NOS"])

data class InvoiceSetup (
    val STO_No:              String = "000000",       /*分店編號*/
    val GUI_YYMM:            String,                  /*發票年月 YYYYMM (西元年)*/
    val POS_NOS:             String             ,     /*收銀機號*/
    val SER_NOS:             String             ,     /*發票號碼序號*/

    val STATUS:              String ?= null            ,     /*使用狀態 00=未使用 01=使用中 02=己用完 03=暫停使用 04=已鎖定*/
    val GUI_TRACK:           String ?= null            ,     /*發票字軌*/
    val GUI_SNOS:            String ?= null            ,     /*發票起始號*/
    val GUI_ENOS:            String ?= null            ,     /*發票終止號*/
    val NEXT_SNOS:           String ?= null            ,     /*下一個發票號碼*/
    val USED_NOS:            Int    ?= null            ,     /*已用張數*/
    val TOT_NOS:             Int    ?= null            ,     /*全部張數*/
    val FREE_NOS:            Int    ?= null            ,     /*未用張數*/
    val REMARK:              String ?= null            ,     /*備註(統一編號)*/
    val eINV_Flag:           String ?= null            ,     /*是否為電子發票 Y=電子發票 N=紙本發票(預設值)*/
)