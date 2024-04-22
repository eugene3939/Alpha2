package com.example.alpha2.DBManager.System

import androidx.room.Entity
import androidx.room.PrimaryKey

/*付款方式檔(原POS1006)*/

@Entity(tableName = "PaymentMethods")
data class PaymentMethod(
            @PrimaryKey
            val PAY_No:               String              ,   /*付款別編號*/
            /*  01=現金 03=銷退單 04=禮券 05=信用卡 */
            /*  06=威士卡 07=萬事達卡 08=美國運通卡 09=大來卡 */
            /*  10=吉世美卡 11=聯合信用卡 12=內卡 13=儲值卡 */
            /*  76=暫結 77=內部採購 78=預收單 79=銷貨退回*/
            val PAY_Name:              String ?=null      ,   /*付款別名稱-列印*/
            val PAY_TaxType:           String ?="0"       ,   /*稅別 0=未稅 1=已稅*/
            val PAY_Type:              String             ,   /*付款別型態 0=信用卡 1=現金 2=非信用卡 5=銷退單 6=禮券 7=分期付款 8=金融卡 A=應收帳款 B=抵用券 C=紅利點數 E=悠遊卡 F=餘額卡 I=一卡通 G=中獎發票代兌 M=第三方支付*/
            val PAY_Ref:               String             ,   /*是否輸入參考號Y/N (手動輸入的開關)*/
            val PAY_OpenBox:           String             ,   /*是否開抽屜Y/N*/
            val PAY_OverPay:           String ?="N"       ,   /*是否可溢收Y/N X=不可溢收也不可找零 A=付款金額必須等於銷售總額*/
            val PAY_CDDisplay:         String ?=null      ,   /*客戶顯示器顯示文字*/
            val PAY_LimitValue:        Int    ?=null      ,   /*禮券最小面額*/
            val PAY_MaxLimit:          Int    ?=null      ,   /*禮券最大面額*/
            val JDA_PAY_No:            String ?=null      ,   /*JDA付款別編號*/
            val PAY_PrnInvoice:        String ?="Y"       ,   /*是否開發票 Y=開立發票 N=後開發票 Z=已開發票*/
            val PAY_LimitType:         String ?="0"       ,   /*付款限定種類,參考POS1024*/
            /*0:不限定*/
            /*1:符合限定的商品才可使用*/
            /*2:符合限定的商品不可使用*/
            val PAY_OnlyOrgPrice:      String ?=null      ,   /*原價銷售*/
            val PAY_ChangeMax :        Int    ?=null      ,   /*找零最大金額*/
            val CUR_Code:              String ?=null      ,   /*幣別代碼*/
)
