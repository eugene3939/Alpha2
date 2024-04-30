package com.example.alpha2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.alpha2.DBManager.Invoice.InvoiceDao
import com.example.alpha2.DBManager.Invoice.InvoiceManager
import com.example.alpha2.DBManager.Invoice.InvoiceSetup
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Member.MemberManager
import com.example.alpha2.DBManager.Product.ClusterProduct
import com.example.alpha2.DBManager.Product.CouponDetail
import com.example.alpha2.DBManager.Product.CouponMain
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.Product.ProductManager
import com.example.alpha2.DBManager.System.CashSystem
import com.example.alpha2.DBManager.System.PaymentMethod
import com.example.alpha2.DBManager.System.SystemManager
import com.example.alpha2.DBManager.System.SystemSetting
import com.example.alpha2.DBManager.User.User
import com.example.alpha2.DBManager.User.UserManager
import com.example.alpha2.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)
    private lateinit var productDBManager: ProductManager //商品Dao
    private lateinit var systemDBManager: SystemManager   //設定檔Dao
    private lateinit var memberDBManager: MemberManager   //會員Dao
    private lateinit var InvoiceDBManager: InvoiceManager //發票Dao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 View Binding 初始化綁定
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化資料庫管理器
        userDBManager = UserManager(applicationContext)
        productDBManager = ProductManager(applicationContext)
        systemDBManager = SystemManager(applicationContext)
        memberDBManager = MemberManager(applicationContext)
        InvoiceDBManager = InvoiceManager(applicationContext)

        //匯入整檔
        insertUserDB("1","Eugene", "1", "1")        //建立預設用戶
        insertUserDB("2","Oscar", "3", "3")

        //----DAO 方式建立預設商品資料庫 (建立商品資料)
        insertMerchandisesDB("1","Airwaves Super極酷薄荷無糖口香糖 - 極酷薄荷口味", "零食","4710716334875",50, 50, 50,50,100, pluUnit = "包", DEP_No = "1", CAT_No = "1", VEN_No = "1")
        insertMerchandisesDB("2","零負擔Android程式設計之旅", "書籍","9786263336148", 690, 650,600,500,80,mamMethod = "H", pluType = "2", CAT_No = "1", pluUnit = "本",mmpBegDate = LocalDateTime.of(2024, 1, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 7, 24, 18, 30))
        insertMerchandisesDB("3","Android開發架構實戰", "書籍","9786263332577", 600,550,550, 500,60,mamMethod = "H", pluType = "4", CAT_No = "1", pluUnit = "本",mmpBegDate = LocalDateTime.of(2024, 3, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 5, 24, 18, 30))
        insertMerchandisesDB("17","Android初學特訓班", "書籍","9789865023072", 500, 450,400,400,80,mamMethod = "1", pluType = "3", CAT_No = "1", pluUnit = "本",mmpBegDate = LocalDateTime.of(2024, 3, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 3, 24, 18, 30))
        insertMerchandisesDB("18","輕鬆學會Android kotlin程式開發", "書籍","9789864343751", 500,500,500, 500,60,mamMethod = "B", pluType = "2", DEP_No = "1", CAT_No = "1", pluUnit = "本",mmpBegDate = LocalDateTime.of(2024, 8, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 11, 24, 18, 30))
        insertMerchandisesDB("19","SQL Server 2022/2019資料庫設計與開發實務", "書籍","9786263245198", 660, 650,650,650,25,mamMethod = "D", pluUnit = "本")
        insertMerchandisesDB("20","Android初學者套組", "組合商品","4902778915202", 2000, 2000,2000,1800,200,mamMethod = "E", pluUnit = "分")

        insertMerchandisesDB("BookCoupon03","貨號券(實戰)","折扣券","SS555555",50,50,50,50,200, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("BookCoupon11","貨號券(排除 實戰、輕鬆)","折扣券","SS444444",50,50,50,50,200, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("BookCoupon04","類別券CAT(單一)","折扣券","SS666666",100,100,100,100,300, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("BookCoupon05","類別券C D V(多重)","折扣券","SS600000",30,30,300,30,300, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("BookCoupon06","類別券DEP(排除)","折扣券","SS777777",100,100,100,100,300, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("BookCoupon08","過期類別券(排除)","折扣券","SS888888",100,100,100,100,300, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("Coupon100","30元折價券","折價券","SS123456",30,30,30,30,100, pluUnit = "張", pluType = "75")
        insertMerchandisesDB("Coupon500","50元折價券","折價券","SS111111",50,50,50,50,200, pluUnit = "張", pluType = "75")

        insertCouponDetailDB(DISC_PLU_MagNo = "SS444444", FROM_DATE = LocalDateTime.of(2024, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2024, 10, 10, 10, 10), SEQ_NO = 177, PLU_MagNo = "9789864343751")
        insertCouponDetailDB(DISC_PLU_MagNo = "SS444444", FROM_DATE = LocalDateTime.of(2024, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2024, 10, 10, 10, 10), SEQ_NO = 188, PLU_MagNo = "9786263332577")
        insertCouponDetailDB(DISC_PLU_MagNo = "SS555555", FROM_DATE = LocalDateTime.of(2024, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2024, 10, 10, 10, 10), SEQ_NO = 111, PLU_MagNo = "9786263332577")
        insertCouponDetailDB(DISC_PLU_MagNo = "SS666666", FROM_DATE = LocalDateTime.of(2024, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2024, 10, 10, 10, 10), SEQ_NO = 121, CAT_No = "1")
        insertCouponDetailDB(DISC_PLU_MagNo = "SS600000", FROM_DATE = LocalDateTime.of(2024, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2024, 10, 10, 10, 10), SEQ_NO = 121, DEP_No = "1", CAT_No = "1", VEN_No = "1")
        insertCouponDetailDB(DISC_PLU_MagNo = "SS777777", FROM_DATE = LocalDateTime.of(2024, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2024, 10, 10, 10, 10), SEQ_NO = 121, DEP_No = "1")
        insertCouponDetailDB(DISC_PLU_MagNo = "SS888888", FROM_DATE = LocalDateTime.of(2022, 1, 20, 20, 20), TO_DATE = LocalDateTime.of(2023, 10, 10, 10, 10), SEQ_NO = 121, DEP_No = "1")

        insertCouponMainDB(pluMagNo = "SS444444", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"0","2")      //打折券(分類)
        insertCouponMainDB(pluMagNo = "SS555555", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"0","1")
        insertCouponMainDB(pluMagNo = "SS666666", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"0","1")
        insertCouponMainDB(pluMagNo = "SS600000", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"0","1")
        insertCouponMainDB(pluMagNo = "SS777777", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"0","2")
        insertCouponMainDB(pluMagNo = "SS888888", fromDate = LocalDateTime.of(2022, 1, 20, 20, 20), toDate = LocalDateTime.of(2023, 10, 10, 10, 10),"0","2")
        insertCouponMainDB(pluMagNo = "SS123456", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"1")                   //折價券(單價) baseType = 0 就不用檢查明細檔，直接適用
        insertCouponMainDB(pluMagNo = "SS111111", fromDate = LocalDateTime.of(2024, 1, 20, 20, 20), toDate = LocalDateTime.of(2024, 10, 10, 10, 10),"1")

        insertPairProduct("20","1,2,3","1,2,3",60)    //綑綁商品清單

        //Dao匯入收銀機設定檔 (預設)
        insertCashSystemDB("1","Eugene")

        //Dao匯入系統設定檔 (預設)
        insertSystemSettingDB("store123","Nintendo","cashRegister123",30)

        //Dao匯入付款發票號碼設定檔 (預設)
        insertInvoiceSetupDB("store123","202405","register123","invoiceSerialNo",
            "00","AB","000000","999999","000001",
            0,50,50,"統一編號","Y")

        //Dao匯入付款方式檔 (預設)
        insertPaymentMethodDB("01","現金","0","1","N","Y")
        insertPaymentMethodDB("05","信用卡","0","0","Y","N")
        insertPaymentMethodDB("06","非信用卡","0","2","Y","N")
        insertPaymentMethodDB("13","悠遊卡","0","E","N","N")

        //Dao匯入預設會員檔案
        insertMember("ABC12345","Joyce",LocalDateTime.of(2024, 8, 22, 10, 0),LocalDateTime.of(2024, 8, 22, 10, 0),0.9,"card123")

        // 登入按鈕
        binding.btnLogin.setOnClickListener {
            // 取得帳號、密碼
            val acc = binding.edtAcc.text.toString()
            val pas = binding.edtPas.text.toString()

            //用Dao查看是否為許可用戶
            lifecycleScope.launch(Dispatchers.IO) {
                val accessUser = userDBManager.loginByAccPas(acc,pas)

                //有許可用戶
                if (accessUser != null){
                    //顯示登入成功訊息
                    Log.d("登入成功", "用戶名稱: ${accessUser.name}")

                    //保存用戶訊息到sharedReference
                    val sharedPreferences = getSharedPreferences("loginUser", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("userId", accessUser.id)
                    editor.apply()

                    //跳轉到登入頁面
                    val intent = Intent(this@Login,MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.d("登入失敗: ", "無此用戶")
                }
            }
        }
    }

    //Dao匯入付款發票號碼設定檔 (預設)
    private fun insertInvoiceSetupDB( STO_No: String, GUI_YYMM: String, POS_NOS: String, SER_NOS: String,

                                      STATUS: String, GUI_TRACK: String, GUI_SNOS: String, GUI_ENOS: String, NEXT_SNOS: String,
                                      USED_NOS: Int, TOT_NOS: Int, FREE_NOS: Int, REMARK: String, eINV_Flag: String) {

        lifecycleScope.launch(Dispatchers.IO){
            val invoiceManager = InvoiceManager(applicationContext)

            //確認是否有重複的項目
            val existingInvoiceSetting = invoiceManager.getInvoiceSetupsBy(STO_No, GUI_YYMM,POS_NOS, SER_NOS)

            if(existingInvoiceSetting == null){
                //新建發票號碼設定檔
                val newInvoiceSetup = InvoiceSetup(STO_No, GUI_YYMM, POS_NOS, SER_NOS,
                    STATUS,GUI_TRACK,GUI_SNOS,GUI_ENOS,NEXT_SNOS,
                            USED_NOS,TOT_NOS,FREE_NOS,REMARK,eINV_Flag)

                InvoiceDBManager.addInvoiceSetup(newInvoiceSetup)

                Log.d("新增發票號碼設定檔", "Cash System added: $STO_No $GUI_YYMM")
            }else{
                Log.d("既有發票號碼設定檔", "Cash System ecrNo $STO_No $GUI_YYMM already exists")
            }
        }

    }

    //新增會員預設資料
    private fun insertMember(mId: String, mName: String, startDate: LocalDateTime?, endDate: LocalDateTime?, discountRate: Double, cardId: String) {
        lifecycleScope.launch(Dispatchers.IO){
            val memberDBManager = MemberManager(applicationContext)

            //確認收銀機是否已經存在
            val existingCash = memberDBManager.getMemberById(mId)

            if(existingCash == null){

                if (startDate != null && endDate!=null) {
                    val newMember =Member(id = mId, name = mName, issueDate = startDate, expireDate = endDate, discRate = discountRate, cardNo = cardId)
                    memberDBManager.addMember(newMember)

                    Log.d("新增會員預設檔", "Member mId added: $mId")
                }

            }else{
                Log.d("既有收銀機狀態檔", "Member mId: $mId already exists")
            }
        }
    }

    //預設收銀機系統檔案
    private fun insertCashSystemDB(ecrNo: String, ecrName: String) {
        lifecycleScope.launch(Dispatchers.IO){
            val systemDBManager = SystemManager(applicationContext)

            //確認收銀機是否已經存在
            val existingCash = systemDBManager.getCashSystemNoById(ecrNo)

            if(existingCash == null){

                val cashRegister = CashSystem(ecrNo =  ecrNo,ecrName= ecrName)
                systemDBManager.addCashSystem(cashRegister)

                Log.d("新增收銀機狀態檔", "Cash System added: $ecrNo")
            }else{
                Log.d("既有收銀機狀態檔", "Cash System ecrNo $ecrNo already exists")
            }
        }
    }

    //預設系統設定檔案
    private fun insertSystemSettingDB(storeNo: String,storeNm: String,ecrNo: String, mBRDiscRate: Int){
        //確認一般設定檔是否已經存在
        val existingSysSetting= systemDBManager.getSystemSettingNoById(ecrNo)
        if (existingSysSetting == null) {
            val sys = SystemSetting(storeNo = storeNo, storeNm = storeNm, ecrNo = ecrNo,mBRDiscRate = mBRDiscRate)
            systemDBManager.addSystem(sys)
            Log.d("新增一般設定檔", "User added: $ecrNo")
        } else {    //確認是否為已知id
            Log.d("既有一般設定檔", "User with ID $ecrNo already exists")
        }
    }

    //新增付款方式檔
    private fun insertPaymentMethodDB(payNo: String, payName: String, payTaxType: String, payType: String, payRef: String, payOpenBox: String) {
        lifecycleScope.launch(Dispatchers.IO){
            //確認用戶是否已經存在
            val existingPaymentMethod = systemDBManager.getPaymentMethodById(payNo)
            if (existingPaymentMethod == null) {
                val payment = PaymentMethod(PAY_No = payNo, PAY_Name = payName,PAY_TaxType = payTaxType, PAY_Type = payType, PAY_Ref = payRef, PAY_OpenBox = payOpenBox)
                systemDBManager.addPaymentMethod(payment)
                Log.d("新增付款方式", "Payment method added: $payment")
            } else {    //確認是否為已知id
                Log.d("既有付款方式", "Payment with ID $payName already exists")
            }
        }
    }

    //新增會員檔
    private fun insertUserDB(id: String,name: String,account: String,password: String) {
        lifecycleScope.launch(Dispatchers.IO){
            //確認用戶是否已經存在
            val existingUser = userDBManager.getUserById(id)
            if (existingUser == null) {
                val user = User(id, name, account,password)
                userDBManager.addUser(user)
                Log.d("新增用戶", "User added: $user")
            } else {    //確認是否為已知id
                Log.d("既有用戶", "User with ID $id already exists")
            }
        }
    }

    //一般商品資料庫
    private fun insertMerchandisesDB(id: String,
                                     name: String,
                                     type: String,
                                     pluMagNo: String,
                                     fixPrc: Int,
                                     salePrc: Int,
                                     unitPrc: Int,
                                     memPrc: Int,
                                     number: Int,

        //商品分類
                                     DEP_No: String ?= null,    /*部門編號*/
                                     CAT_No: String ?= null,    /*分類編號*/
                                     VEN_No: String ?= null,    /*廠商編號*/

                                     mamMethod:String = "0",
                                     pluUnit: String,                       //單位
                                     pluType: String = "1",
                                     mmpBegDate: LocalDateTime? = null,
                                     mmpEndDate:LocalDateTime? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            //確認用戶是否已經存在
            val existingMerchandise = productDBManager.getProductByID(id)
            if (existingMerchandise == null) {
                val item = Product( pId = id, pName = name, pType = type, pluMagNo = pluMagNo, pluType = pluType, pluUnit = pluUnit, pNumber = number,
                    fixPrc = fixPrc,salePrc = salePrc, unitPrc = unitPrc, memPrc = memPrc,
                    DEP_No = DEP_No,CAT_No = CAT_No, VEN_No = VEN_No,
                    mamMethod = mamMethod,mmpBegDate = mmpBegDate,mmpEndDate= mmpEndDate)

                productDBManager.insert(item)
                Log.d("新增商品", "Merchandise added: $item")
            } else {    //確認是否為已知id
                Log.d("既有商品", "Merchandise with pID $id already exists")
            }
        }
    }

    //折價券 商品資料庫
    private fun insertCouponMainDB(pluMagNo: String, fromDate: LocalDateTime, toDate: LocalDateTime, discTYPE: String,baseTYPE: String = "0") {
        lifecycleScope.launch(Dispatchers.IO){
            //確認折扣商品是否已經存在
            val existCouponMain = productDBManager.getCouponMainByPluMagNo(pluMagNo)
            if (existCouponMain == null) {
                val item = CouponMain(DISC_PLU_MagNo = pluMagNo, FROM_DATE = fromDate, TO_DATE = toDate, DISC_TYPE = discTYPE, BASE_TYPE = baseTYPE)
                productDBManager.insertCouponMain(item)
                Log.d("新增折價券主檔", "DProduct added: $item")
            } else {    //確認是否為已知id
                Log.d("既有折價券主檔", "DProduct with ID $pluMagNo already exists")
            }
        }
    }


    //折價券 商品明細檔
    private fun insertCouponDetailDB(DISC_PLU_MagNo: String,
                                     FROM_DATE: LocalDateTime,
                                     TO_DATE: LocalDateTime,
                                     SEQ_NO: Int,
        //指定貨號
                                     PLU_MagNo:String?= null,
        //商品分類
                                     DEP_No: String ?= null,    /*部門編號*/
                                     CAT_No: String ?= null,    /*分類編號*/
                                     VEN_No: String ?= null,    /*廠商編號*/) {

        lifecycleScope.launch(Dispatchers.IO){
            //確認折扣商品是否已經存在
            val existCouponDetail = productDBManager.getCouponDetailByFullKeys(DISC_PLU_MagNo,FROM_DATE,TO_DATE,SEQ_NO)
            if (existCouponDetail != null) {
                if (existCouponDetail.isEmpty()) {
                    val item = CouponDetail(DISC_PLU_MagNo = DISC_PLU_MagNo, FROM_DATE = FROM_DATE, TO_DATE = TO_DATE, SEQ_NO = SEQ_NO, PLU_MagNo = PLU_MagNo,DEP_No = DEP_No, CAT_No = CAT_No, VEN_No = VEN_No)
                    productDBManager.insertCouponDetail(item)
                    Log.d("新增折價券明細檔", "DProduct added: $item")
                } else {    //確認是否為已知id )
                    Log.d("既有折價券明細檔", "DProduct with ID $DISC_PLU_MagNo already exists")
                }
            }
        }
    }

    //組合商品清單
    private fun insertPairProduct(pId: String,itemSet : String, number: String, total: Int) {
        lifecycleScope.launch(Dispatchers.IO){
            //確認組合商品是否已經存在
            val existingCProduct = productDBManager.getClusterByID(pId)
            if (existingCProduct == null) {
                val item = ClusterProduct(pId = pId, itemSet = itemSet, number = number, total = total)
                productDBManager.insertCluster(item)
                Log.d("新增組合商品", "CProduct added: $item")
            } else {    //確認是否為已知id
                Log.d("既有組合商品", "CProduct with ID $pId already exists")
            }
        }
    }
}