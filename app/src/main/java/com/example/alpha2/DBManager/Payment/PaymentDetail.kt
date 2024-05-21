package com.example.alpha2.DBManager.Payment

import androidx.room.Entity
import java.time.LocalDateTime

/*即時銷售明細檔 (POS3009)*/

@Entity(tableName = "PaymentDetails",primaryKeys = ["SYS_StoreNo", "TXN_Date", "ECR_No", "TXN_No", "TXN_Item"])

data class PaymentDetail(
    // Primary Keys
    val SYS_StoreNo:         String              ,      /*店號*/
    val TXN_Date:            LocalDateTime       ,      /*交易日期*/
    val ECR_No:              String              ,      /*收銀機代碼*/
    val TXN_No:              Int                 ,      /*交易序號 今日的第幾筆交易*/
    val TXN_Item:            Int                 ,      /*項次*/ //商品A -> 1, 商品B -> 2

    val TXN_Time:            LocalDateTime       ,      /*交易時間*/
    val TXN_GUINo:           String              ,      /*發票號碼*/
    val PLU_No:              String              ,      /*商品條碼*/
    val DEP_No:              String? = null      ,      /*部門編號*/
    val VEN_No:              String? = null      ,      /*廠商編號*/
    val CAT_No:              String? = null      ,      /*分類編號*/
    val TXN_Qty:             Int                 ,      /*數量*/
    val PLU_FixPrc:          Double                 ,      /*定價*/
    val PLU_SalePrc:         Double                 ,      /*售價*/
    val TXN_DiscS:           Double                 ,      /*人工折扣(負數)*/
    val TXN_DiscM:           Double                 ,      /*會員折扣(負數)*/
    val TXN_DiscT:           Double                 ,      /*總合折扣(負數)*/
    val TXN_SaleAmt:         Double                 ,      /*銷售金額=應稅銷售金額+免稅銷售金額*/
    val TXN_SaleTax:         Double                 ,      /*應稅銷售金額=未稅銷售金額+稅額*/
    val TXN_SaleNoTax:       Double                 ,      /*免稅銷售金額*/
    val TXN_Net:             Double                 ,      /*未稅銷售金額*/
    val TXN_Tax:             Double                 ,      /*稅額*/
    val PLU_TaxType:         String? = "0"       ,      /*稅別 0=免稅 1=應稅*/
    val MMP_No:              String? = null      ,      /*檔期編號*/
    val MAM_No:              String? = null      ,      /*促銷編號*/
    val MAM_Condition:       Int? = null         ,      /*促銷數量*/
    val MAM_TotAmt:          Int? = null         ,      /*促銷總價*/
    val MAM_CombNo:          String? = null      ,      /*組合編號*/
    val MAM_CombGrp:         String? = null      ,      /*組合群組*/
    val MAM_Method:          String? = null      ,      /*促銷方式*/
    val TXN_Ref:             String? = null      ,      /*參考號碼*/
    val TXN_AuthNo:          String? = null      ,      /*授權編號*/
    val TXN_AuthReason:      String? = null      ,      /*授權原因*/
    val TXN_GUIPageNo:       Int? = null         ,      /*發票頁數*/
    val TXN_Mode:            String              ,      /*交易模式(同POS3008)*/              /*-------必填寫-----*/
    val TXN_Status:          String              ,      /*交易狀態(同POS3008,R=退貨)*/        /*-------必填寫-----*/
    val TXN_VIP:             String? = "N"      ,       /*外交官交易Y=是,N=否*/
    val MAM_UnitPrc:         Int? = null         ,      /*促銷單價*/
    val MAN_ChangeNo:        String? = null      ,      /*變價單號*/
    val PLU_Disc:            String? = null      ,      /*是否可折扣(Y/N)*/
    val TXN_RebateS:         Int? = null         ,      /*單項折讓(負數)*/
    val TXN_RebateT:         Int? = null         ,      /*小計折讓(負數)*/
    val TXN_Ret_Cod:         String? = null      ,      /*退貨原因 0=預設退貨 X=系統折價券 Y=被更正 Z=更正 S=銷退*/
    val TXN_Except_Sta:      String? = null      ,      /*例外註記 N=正常 P=更改售價 F=強制輸入 G=自帶價格條碼 #=更正*/
    val PLU_MagNo:           String? = null      ,      /*管理碼(6碼)*/
    val PLU_Type:            String? = null      ,      /*商品類別 */
    val PLU_Set_Code:        String? = null      ,      /*組合商品類別 0=無 1=母 2=子 */
    val TXN_ActualPrc:       Int? = null         ,      /*實際售價*/
    val TXN_DiscP:           Int? = null         ,      /*促銷折扣(負數)*/
    val TXN_MINUS_GM_Amt:    Int? = null         ,      /*折價券扣減銷項金額*/
    val TXN_Coup_Disc_Amt:   Int? = null         ,      /*折價券金額*/
    val PLU_Health:          String? = null      ,      /*是否有健康捐(Y/N)*/
    val PLU_HealthPrc:       Int? = null         ,      /*健康捐單價*/
    val PLU_HealthBegDate:   LocalDateTime? = null,      /*健康捐有效開始日期*/
    val PLU_HealthEndDate:   LocalDateTime? = null,      /*健康捐有效結束日期*/
    val TXN_HealthAmt:       Int? = null         ,      /*健康捐金額=健康捐單價X數量*/
    val PLU_Gift:            String? = null      ,
    val MAS_No:              String? = null      ,      /*會員活動編號*/
    val TXN_DownMarginAmt:   Int? = null         ,      /*會員積分扣抵扣減銷項金額*/
    val TXN_PayQuartAmt:     Int? = null         ,      /*會員積分扣抵金額*/
    val TXN_PayQuart:        Int? = null         ,      /*會員積分扣抵*/
    val TXN_UseSalePrc:      String? = null      ,      /*是否使用原價(Y/N)*/
    val PLU_Source:          String? = null      ,      /*是否為自製品 0=其他 1=自製 2=OEM 3=外購 4=自行進口 5=代工 6=煙酒*/
    val TXN_ExtraAmt:        Int? = null         ,      /*代收代售銷售金額*/
    val PLU_TaxRate:         Int? = null         ,       /*稅率*/
    val PLU_Name:            String? = null      ,       /*商品名稱*/

    val TLT_No:              String? = null      ,       /*專櫃編號*/
    val Contract_No:         String? = null      ,       /*專櫃合約編號*/
    val Contract_SubNo:      String? = null              /*專櫃合約附號*/
)
