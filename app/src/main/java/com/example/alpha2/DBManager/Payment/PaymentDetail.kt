package com.example.alpha2.DBManager.Payment

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/*即時銷售明細檔 (POS3009)*/

@Entity(tableName = "PaymentDetails")

data class PaymentDetail(
    @PrimaryKey
    val SYS_StoreNo:         String              ,      /*店號*/
    val TXN_Date:            LocalDateTime       ,      /*交易日期*/
    val ECR_No:              String              ,      /*收銀機代碼*/
    val TXN_No:              Int                 ,      /*交易序號*/
    val TXN_Item:            Int                 ,      /*項次*/
    val TXN_Time:            LocalDateTime       ,      /*交易時間*/
    val TXN_GUINo:           String              ,      /*發票號碼*/
    val PLU_No:              String              ,      /*商品條碼*/
    val DEP_No:              String              ,      /*部門編號*/
    val VEN_No:              String              ,      /*廠商編號*/
    val CAT_No:              String              ,      /*分類編號*/
    val TXN_Qty:             Int                 ,      /*數量*/
    val PLU_FixPrc:          Int                 ,      /*定價*/
    val PLU_SalePrc:         Int                 ,      /*售價*/
    val TXN_DiscS:           Int                 ,      /*人工折扣(負數)*/
    val TXN_DiscM:           Int                 ,      /*會員折扣(負數)*/
    val TXN_DiscT:           Int                 ,      /*總合折扣(負數)*/
    val TXN_SaleAmt:         Int                 ,      /*銷售金額=應稅銷售金額+免稅銷售金額*/
    val TXN_SaleTax:         Int                 ,      /*應稅銷售金額=未稅銷售金額+稅額*/
    val TXN_SaleNoTax:       Int                 ,      /*免稅銷售金額*/
    val TXN_Net:             Int                 ,      /*未稅銷售金額*/
    val TXN_Tax:             Int                 ,      /*稅額*/
    val PLU_TaxType:         String              ,      /*稅別 0=免稅 1=應稅*/
    val MMP_No:              String              ,      /*檔期編號*/
    val MAM_No:              String              ,      /*促銷編號*/
    val MAM_Condition:       Int                 ,      /*促銷數量*/
    val MAM_TotAmt:          Int                 ,      /*促銷總價*/
    val MAM_CombNo:          String              ,      /*組合編號*/
    val MAM_CombGrp:         String              ,      /*組合群組*/
    val MAM_Method:          String              ,      /*促銷方式*/
    val TXN_Ref:             String              ,      /*參考號碼*/
    val TXN_AuthNo:          String              ,      /*授權編號*/
    val TXN_AuthReason:      String              ,      /*授權原因*/
    val TXN_GUIPageNo:       Int                 ,      /*發票頁數*/
    val TXN_Mode:            String              ,      /*交易模式(同POS3008)*/
    val TXN_Status:          String              ,      /*交易狀態(同POS3008,R=退貨)*/
    val TXN_VIP:             String              ,      /*外交官交易Y=是,N=否*/
    val MAM_UnitPrc:         Int                 ,      /*促銷單價*/
    val MAN_ChangeNo:        String              ,      /*變價單號*/
    val PLU_Disc:            String              ,      /*是否可折扣(Y/N)*/
    val TXN_RebateS:         Int                 ,      /*單項折讓(負數)*/
    val TXN_RebateT:         Int                 ,      /*小計折讓(負數)*/
    val TXN_Ret_Cod:         String              ,      /*退貨原因 0=預設退貨 X=系統折價券 Y=被更正 Z=更正 S=銷退*/
    val TXN_Except_Sta:      String              ,      /*例外註記 N=正常 P=更改售價 F=強制輸入 G=自帶價格條碼 #=更正*/
    val PLU_MagNo:           String              ,      /*管理碼(6碼)*/
    val PLU_Type:            String              ,      /*商品類別 */
    val PLU_Set_Code:        String              ,      /*組合商品類別 0=無 1=母 2=子 */
    val TXN_ActualPrc:       Int                 ,      /*實際售價*/
    val TXN_DiscP:           Int                 ,      /*促銷折扣(負數)*/
    val TXN_MINUS_GM_Amt:    Int                 ,      /*折價券扣減銷項金額*/
    val TXN_Coup_Disc_Amt:   Int                 ,      /*折價券金額*/
    val PLU_Health:          String              ,      /*是否有健康捐(Y/N)*/
    val PLU_HealthPrc:       Int                 ,      /*健康捐單價*/
    val PLU_HealthBegDate:   LocalDateTime       ,      /*健康捐有效開始日期*/
    val PLU_HealthEndDate:   LocalDateTime       ,      /*健康捐有效結束日期*/
    val TXN_HealthAmt:       Int                 ,      /*健康捐金額=健康捐單價X數量*/
    val PLU_Gift:            String              ,
    val MAS_No:              String              ,      /*會員活動編號*/
    val TXN_DownMarginAmt:   Int                 ,      /*會員積分扣抵扣減銷項金額*/
    val TXN_PayQuartAmt:     Int                 ,      /*會員積分扣抵金額*/
    val TXN_PayQuart:        Int                 ,      /*會員積分扣抵*/
    val TXN_UseSalePrc:      String              ,      /*是否使用原價(Y/N)*/
    val PLU_Source:          String              ,      /*是否為自製品 0=其他 1=自製 2=OEM 3=外購 4=自行進口 5=代工 6=煙酒*/
    val TXN_ExtraAmt:        Int                 ,      /*代收代售銷售金額*/
    val PLU_TaxRate:         Int                 ,       /*稅率*/
    val PLU_Name:            String              ,       /*商品名稱*/

    val TLT_No:              String              ,       /*專櫃編號*/
    val Contract_No:         String              ,       /*專櫃合約編號*/
    val Contract_SubNo:      String                      /*專櫃合約附號*/
)
