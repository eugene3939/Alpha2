package com.example.alpha2.DBManager.Payment

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/*即時銷售付款付款檔 (POS3010)*/
@Entity(tableName = "PaymentshowUps", primaryKeys = ["SYS_StoreNo", "TXN_Date", "ECR_No", "TXN_No", "TXN_Item"])


data class PaymentshowUp (
    val SYS_StoreNo:         String          ,       /*店號*/
    val TXN_Date:            LocalDateTime   ,       /*交易日期*/
    val ECR_No:              String          ,       /*收銀機代碼*/
    val TXN_No:              Int             ,       /*交易序號*/
    val TXN_Item:            Int             ,       /*項次*/

    val TXN_Time:            LocalDateTime   ,       /*交易時間*/
    val PAY_No:              String          ,       /*付款編號*/
    val TXN_PayDesc:         String          ,       /*付款註記*/
    val TXN_PayAmt:          Int             ,       /*付款金額*/
    val TXN_Mode:            String          ,       /*交易模式(同POS3008)*/
    val TXN_Status:          String          ,       /*交易狀態(同POS3008)*/
    val PAY_TaxType:         String          ,       /*付款稅別 0=未稅 1=已稅*/
    val TXN_OverAmt:         Int             ,       /*溢收金額*/
    val TXN_CouponNo:        String          ,       /*券號*/
    val PAY_Type:            String          ,       /*付款別型態:0=信用卡 1=現金 2=非信用卡 3=預收單 4=銷貨退回 5=銷退單 6=提貨單/禮券 9=儲值 A=應收帳款 E=悠遊卡 F=自由配點 G=回利卡 H=愛金卡 J=有錢卡*/
    val EDC_TmID:            String          ,       /*端末機號*/
    val EDC_BnkNo:           String          ,       /*收單銀行代碼*/
    val BNK_No:              String          ,       /*信用卡發卡銀行代碼*/
    val TERM_No:             Int                     /*期別 0=一般 其餘為分期的期數*/
)