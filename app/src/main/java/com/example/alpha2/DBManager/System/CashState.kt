package com.example.alpha2.DBManager.System

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

//收銀機狀態檔 (POS1003)
@Entity(tableName = "CashStates")

data class CashState (
    @PrimaryKey
    val ECR_No:              String,       /*收銀機代碼*/

    val USR_No:              String,       /*作業中的收銀員代碼*/
    val TXN_NextNo:          Int,          /*下一個可使用交易序號*/
    val GUI_BegNo:           String,       /*起始發票號碼*/
    val GUI_NextNo:          String,       /*下一個可使用發票號碼*/
    val GUI_EndNo:           String,       /*最後一個發票號碼*/
    val ERS_OnlineMode:      String,       /*連線狀態*/
    val ERS_Mode:            String,       /*收銀模式*/
    val ERS_Status:          String,       /*POS 狀態*/
    val ERS_Normal:          String,       /*是否正常結束*/
    val ERS_BussDate: LocalDateTime,       /*營業日期*/
    val TXN_NextOther:       Int   ,       /*下一個可使用非交易序號*/
    val GUI_SerNo:           String,       /*發票號碼序號*/
    val USR_Class:           String,       /*收銀班別*/
    val MEB_No:              String,       /*會員編號*/
    val EMP_No:              String,       /*員工編號*/
    val COM_No:              String,       /*總公司分店編號*/
    val ERS_NextEInvNo:      String,       /*下一個可使用電子發票號碼*/
    val ERS_EndEInvNo:       String,       /*最後一個電子發票號碼*/
    val USR_Level:           String,       /*權限等級*/
    val ERS_FtpUsrId:        String,       /*FTP登入使用者代碼*/
    val ERS_FtpUsrPwd:       String,       /*FTP登入使用者密碼*/
    val ECR_KBType:          String,       /*鍵盤型態*/
    val ERS_ExtData:         String,       /*備註*/
    val ORG_SYS_StoreNo:     String,       /*原交易店號*/
    val ORG_TXN_Date: LocalDateTime,       /*原交易日期*/
    val ORG_ECR_No:          String,       /*原交易機號*/
    val ORG_TXN_No:          Int,          /*原交易序號*/
    val ORG_TXN_Item:        Int,          /*原交易項次*/
    val TLT_No:              String,       /*專櫃編號*/
    val MMT_No:              String        /*商場代號*/
)