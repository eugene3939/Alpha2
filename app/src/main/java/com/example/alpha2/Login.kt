package com.example.alpha2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.alpha2.DBManager.Product.ClusterProduct
import com.example.alpha2.DBManager.Product.DiscountProduct
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.Product.ProductManager
import com.example.alpha2.DBManager.System.CashSystem
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
    private lateinit var systemDBManager: SystemManager  //設定檔Dao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 View Binding 初始化綁定
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化資料庫管理器
        userDBManager = UserManager(applicationContext)
        productDBManager = ProductManager(applicationContext)
        systemDBManager = SystemManager(applicationContext)

        //匯入整檔
        insertUserDB("1","Eugene", "1", "1")        //建立預設用戶
        insertUserDB("2","Oscar", "3", "3")

        //----DAO 方式建立預設商品資料庫 (建立商品資料)
        insertMerchandisesDB("1","Airwaves Super極酷薄荷無糖口香糖 - 極酷薄荷口味", "零食","4710716334875",50, 50, 50,100)
        insertMerchandisesDB("2","零負擔Android程式設計之旅", "書籍","9786263336148", 100, 100,50,80,mamMethod = "H", pluType = "2",mmpBegDate = LocalDateTime.of(2024, 1, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 7, 24, 18, 30))
        insertMerchandisesDB("3","Android開發架構實戰", "書籍","9786263332577", 200,180,100, 60,mamMethod = "H", pluType = "4",mmpBegDate = LocalDateTime.of(2024, 3, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 5, 24, 18, 30))
        insertMerchandisesDB("17","Android初學特訓班", "書籍","9789865023072", 500, 450,400,80,mamMethod = "1", pluType = "3",mmpBegDate = LocalDateTime.of(2024, 3, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 3, 24, 18, 30))
        insertMerchandisesDB("18","輕鬆學會Android kotlin程式開發", "書籍","9789864343751", 230,200,150, 60,mamMethod = "B", pluType = "2",mmpBegDate = LocalDateTime.of(2024, 8, 22, 10, 0),mmpEndDate = LocalDateTime.of(2024, 11, 24, 18, 30))
        insertMerchandisesDB("19","SQL Server 2022/2019資料庫設計與開發實務", "書籍","9786263245198", 80, 80,80,25,mamMethod = "D")
        insertMerchandisesDB("20","Android初學者套組", "組合商品","4902778915202", 300, 300,200,200,mamMethod = "E")

        insertDiscountProductDB("1","蘋果9折", 0.1 , 0)  //折扣商品清單
        insertDiscountProductDB("2","單品折30", 0.0, 30)
        insertDiscountProductDB("3","單品折10", 0.0, 10)
        insertDiscountProductDB("20","組合商品折60", 0.0, 60)

        insertPairProduct("20","1,2,3","1,2,3",60)    //綑綁商品清單

        //Dao匯入收銀機設定檔 (預設)
        insertCashSystemDB("1","Eugene")

        //Dao匯入系統設定檔 (預設)
        insertSystemSettingDB("store123","Nintendo","cashRegister123",30)

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

    //新增user dao Table
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
                                     number: Int,
                                     mamMethod:String = "0",
                                     pluType: String = "1",
                                     mmpBegDate: LocalDateTime? = null,
                                     mmpEndDate:LocalDateTime? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            //確認用戶是否已經存在
            val existingMerchandise = productDBManager.getProductByID(id)
            if (existingMerchandise == null) {
                val item = Product(pId = id, pName = name, pType = type, pluMagNo = pluMagNo, pNumber = number, fixPrc = fixPrc,salePrc = salePrc, unitPrc = unitPrc,mamMethod = mamMethod, pluType = pluType,mmpBegDate = mmpBegDate,mmpEndDate= mmpEndDate)
                productDBManager.insert(item)
                Log.d("新增商品", "Merchandise added: $item")
            } else {    //確認是否為已知id
                Log.d("既有商品", "Merchandise with pID $id already exists")
            }
        }
    }

    //折扣商品資料庫
    private fun insertDiscountProductDB(pId: String, description: String, percentage: Double, chargeback: Int) {
        lifecycleScope.launch(Dispatchers.IO){
            //確認折扣商品是否已經存在
            val existingDProduct = productDBManager.getDiscountByID(pId)
            if (existingDProduct == null) {
                val item = DiscountProduct(pId = pId, pDescription = description, pDiscount = percentage, pChargebacks = chargeback, selectedQuantity = 0)
                productDBManager.insertDiscount(item)
                Log.d("新增折扣商品", "DProduct added: $item")
            } else {    //確認是否為已知id
                Log.d("既有折扣商品", "DProduct with ID $pId already exists")
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