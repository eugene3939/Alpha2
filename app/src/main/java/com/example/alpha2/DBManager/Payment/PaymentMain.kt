package com.example.alpha2.DBManager.Payment

import androidx.room.Entity
import java.time.LocalDateTime

/*即時銷售主檔 (POS3008)*/

@Entity(tableName = "PaymentMains", primaryKeys = ["SYS_StoreNo", "TXN_Date", "ECR_No", "TXN_No"])

data class PaymentMain(
    //PrimaryKey
    val  SYS_StoreNo:        String,      /*店號*/
    val  TXN_Date:    LocalDateTime,      /*交易日期*/
    val  ECR_No:             String,      /*收銀機代碼*/
    val  TXN_No:                Int,      /*交易序號*/ //A商品 -> 1, B商品 -> 2

    val  TXN_Time  :  LocalDateTime,      /*交易時間*/
    val  USR_No:             String,      /*收銀員號碼*/
    val  TXN_Uniform:        String,      /*統一編號*/
    val  TXN_MemCard:        String,      /*會員卡號碼*/
    val  TXN_GUIPaper:       String,      /*發票聯數 2=二聯式 3=三聯式 N=免開發票 E=電子發票 R=銷退單*/
    val  TXN_GUIBegNo:       String,      /*起始發票號碼*/
    val  TXN_GUICnt:            Int,      /*發票號碼張數*/
    val  TXN_TotQty:            Int,      /*總數量*/


    val  TXN_TotDiscS:          Int,      /*總人工折扣(負數)*/
    val  TXN_TotDiscM:          Int,      /*總組合折扣(負數)*/
    val  TXN_TotDiscT:          Int,      /*總總合折扣(負數)*/
    val  TXN_TotSaleAmt:        Int,      /*總銷售金額=總應稅銷售金額+總免稅銷售金額*/
    val  TXN_TotSaleTax:        Int ?= null,      /*總應稅銷售金額=總未稅銷售金額+總稅額*/
    val  TXN_TotSaleNoTax:      Int ?= null,      /*總免稅銷售金額*/
    val  TXN_TotNet:            Int ?= null,      /*總未稅銷售金額*/
    val  TXN_TotTax:            Int ?= null,      /*總稅額*/
    val  TXN_TotGUI:            Int,              /*總發票金額*/
    val  TXN_TotHaveTax:        Int ?= null,      /*總已稅金額*/
    val  TXN_TotOver:           Int ?= null,      /*總溢收金額*/
    val  TXN_DtlCnt:            Int ?= null,      /*明細項數*/
    val  TXN_PayCnt:            Int ?= null,      /*付款項數*/


    val  TXN_CustCnt:           Int ?= null,      /*來客數*/
    val  TXN_VoidUsrNo:      String ?= null,      /*發票作廢人員號碼*/
    val  TXN_VoidDT:  LocalDateTime ?= null,      /*發票作廢日期*/
    val  TXN_EntryDT: LocalDateTime ?= null,      /*補輸入日期*/
    val  TXN_Mode:           String,      /*交易模式 */     /*********************/
//        /  R=收銀     E=補輸入   T=訓練 /
//        /  W=預收取貨 B=預收取貨 Y=銷退 /
//        /  X=折讓     F=財務手開        /
//        TXN_Status          VARCHAR(1)         NOT NULL,      /*交易狀態 */
//        /  N=正常交易       C=交易取消 /
//        /  D=發票作廢       E=隔日發票作廢 /
//        /  R=退貨           P=強制輸入 /
//        /  S=預收銷售       T=完成取貨 /
//        /  U=當日預收取消   V=隔日預收取消 /
//        /  M=非銷貨交易     A=非銷貨當日作廢 /
//        /  B=非銷貨隔日作廢 /
val  TXN_VIP:            String = "N"   ,      /*外交官交易 Y=是 N=否 */
val  TXN_ShiftNo:        String? = null ,      /*班別*/
val  TXN_TotPayAmt:      Int            ,      /*總付款金額*/  /*********************/
val  TXN_MemID:          String? = null ,      /*會員身分證字號*/
val  TXN_PreOrderNo:     String? = null ,      /*預收單號 S/O單號*/
val  ORG_SYS_StoreNo:    String? = null ,      /*原始店號*/
val  ORG_TXN_Date:       LocalDateTime? = null  ,      /*原始交易日期*/
val  ORG_ECR_No:         String? = null ,      /*原始收銀機號*/
val  ORG_TXN_No:         Int? = null    ,      /*原始交易序號*/
val  TXN_VoidStoreNo:    String? = null ,      /*發票作廢店號*/
val  TXN_VoidEcrNo:      String? = null ,      /*發票作廢收銀機號*/
val  TXN_VoidTime:       LocalDateTime? = null  ,      /*發票作廢時間*/
val  TXN_VoidShiftNo:    String? = null ,      /*發票作廢班別*/
val  TXN_TotRebateS:     Int? = null    ,      /*總單項折讓(負數)*/
val  TXN_TotRebateT:     Int? = null    ,      /*總小計折讓(負數)*/
val  TXN_Ret_No:         String? = null ,      /*退貨單號 折讓單號*/
val  TXN_TaxType:        String?= "0"   ,      /*稅別 0=免稅 1=應稅 2=零稅*/
val  TXN_VIP_Disc:       Int? = null    ,      /*特別折扣率%OFF 八折為20*/
val  TXN_TotDiscP:       Int? = null    ,      /*總促銷折扣(負數)*/
val  TXN_Ret_PrnNo:      String? = null ,      /*折讓單原始發票號碼*/
val  TXN_GUIEndNo:       String? = null ,      /*結束發票號碼*/
val  TXN_PrintTimes:     Int? = null    ,      /*列印次數*/
val  TXN_GUI_Flag:       String? = null ,      /*發票註記*/
val  TXN_Desc:           String? = null ,      /*交易註記 折讓單號*/
val  TXN_CustType:       String? = null ,      /*交易客層*/
val  TXN_OrgTime:        LocalDateTime? = null  ,      /*原始交易時間*/
val  TXN_SaleNoteNo:     String? = null ,      /*銷貨單號*/
val  TXN_Sales  :        String ? = null,      /*銷貨員*/
val  TXN_TotHealthAmt:   Int? = null    ,      /*總健康捐金額*/
val  MEB_Type:           String? = null ,      /*會員等級 1:固定為一般卡 */
val  TXN_Score:          Int? = null    ,      /*交易積分(一元積一分不足一元則四捨五入)*/
val  TXN_PayScore:       Int? = null    ,      /*交易積分扣抵(負項)*/
val  TXN_TotExtraAmt:    Int? = null    ,      /*代收代售銷售總額*/
val  TXN_PayScoreAmt:    Int? = null    ,      /*會員點數扣抵金額(負項)*/
val  TXN_GUIRemark:      String ? = null,      /*電子發票隨機碼 D4=開立折讓單(TXN_Ret_PrnNo=原始發票號碼) C5=退貨交易、不開立折讓單(TXN_Ret_PrnNo=原始發票號碼)*/
val  STO_Uniform :       String ? = null,      /*本店統一編號*/
val  TXN_GUILoveCode:    String? = null ,      /*電子發票愛心碼*/
val  TXN_GUI_CarrType:   String? = null ,      /*電子發票載具類別 悠遊卡=1K0001 共通性載具手機條碼=3J0002 自然人憑證=CQ0001 一卡通=1H0001 信用卡=EK0002 EK0004=愛金卡 EG0011=OPEN POINT 會員載具 #=取消載具*/
val  TXN_GUI_CarrId :    String? = null ,      /*電子發票載具ID CarrierId2載具隱碼id 信用卡=信用卡加密卡號50碼*/
val  TXN_GUI_CarrIdEx:   String? = null ,      /*電子發票載具ID 顯碼 CarrierId1載具顯碼id 信用卡=刷卡日期(民國年月日7碼)刷卡金額(右靠左補0共10碼) */

val  TLT_No :            String? = null ,     /*專櫃編號*/
val  Contract_No :       String? = null ,     /*專櫃合約編號*/
val  Contract_SubNo :    String? = null ,     /*專櫃合約附號*/

val  MMT_No :            String? = null ,     /*商場代號*/
val  HOS_No:             String? = null       /*倉庫編號*/
)
