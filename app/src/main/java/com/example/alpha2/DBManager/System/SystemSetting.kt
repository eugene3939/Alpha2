package com.example.alpha2.DBManager.System

import androidx.room.Entity
import androidx.room.PrimaryKey

//系統設定檔 (原POS1001)
@Entity(tableName = "SystemSettings")

data class SystemSetting (
    val storeNo: String,                    //公司編號/店號
    val storeNm: String,                    //公司店名
    @PrimaryKey
    val ecrNo: String,                      //收銀機代碼
    val srvDDir: String = "deviceDownLoad",      //伺服器下載目錄
    val srvUDir: String = "deviceUPLoad",   //伺服器上傳目錄
    val sernoType: String = "1",            //交易序號使用方式 1=循環使用 2=每日重計 3=每月重計
    val outOfPaper: Int = 1,                //提示(設定剩餘發票張數)
    val useDefGUI: String = "abc123",       //使用發票預設號
    val mBRDiscRate: Int,                   //會員折扣率
)