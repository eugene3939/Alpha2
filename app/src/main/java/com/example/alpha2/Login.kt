package com.example.alpha2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.alpha2.DBManager.Product.ClusterProduct
import com.example.alpha2.DBManager.Product.DiscountProduct
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.Product.ProductManager
import com.example.alpha2.DBManager.User.User
import com.example.alpha2.DBManager.User.UserManager
import com.example.alpha2.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)
    private lateinit var productDBManager: ProductManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用 View Binding 初始化綁定
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化資料庫管理器
        userDBManager = UserManager(applicationContext)
        productDBManager = ProductManager(applicationContext)

        //匯入整檔
        insertUserDB("1","Eugene", "1", "1", "clerk")        //建立預設用戶
        insertUserDB("2","Oscar", "3", "3","clerk2")

        //----DAO 方式建立商品資料庫(考量後續變更資料複雜度，暫不使用)

        insertMerchandisesDB("1","Apple", "水果","SBC", 50, 100, "0")        //建立商品資料
        insertMerchandisesDB("2","Pineapple", "水果","123", 100, 80, "0")
        insertMerchandisesDB("3","Snapple", "其他","A12", 200, 60, "0")
        insertMerchandisesDB("17","可可亞", "食物","ABC", 500, 80, "0")
        insertMerchandisesDB("18","西瓜", "飲料","RCT", 230, 60, "0")
        insertMerchandisesDB("19","綠茶", "飲料","CCC", 80, 25, "0")
        insertMerchandisesDB("20","Apple set", "組合商品","RTX", 300, 200, "0")

        insertDiscountProductDB("1","蘋果9折", 0.1 , 0)  //折扣商品清單
        insertDiscountProductDB("2","單品折30", 0.0, 30)
        insertDiscountProductDB("3","單品折10", 0.0, 10)
        insertDiscountProductDB("20","組合商品折60", 0.0, 60)

        insertPairProduct("20","1,2,3","1,2,3",60)    //綑綁商品清單

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

                    //跳轉到登入頁面
                    val intent = Intent(this@Login,MainActivity::class.java)
                    startActivity(intent)
                }else{
                    Log.d("登入失敗: ", "無此用戶")
                }
            }
        }
    }

    //新增user dao Table
    private fun insertUserDB(id: String,name: String,account: String,password: String,authentication: String) {
        lifecycleScope.launch(Dispatchers.IO){
            //確認用戶是否已經存在
            val existingUser = userDBManager.getUserById(id)
            if (existingUser == null) {
                val user = User(id, name, account,password,authentication)
                userDBManager.addUser(user)
                Log.d("新增用戶", "User added: $user")
            } else {    //確認是否為已知id
                Log.d("既有用戶", "User with ID $id already exists")
            }
        }
    }

    //一般商品資料庫
    private fun insertMerchandisesDB(id: String, name: String, type: String, barcode: String, price: Int, number: Int, image: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            //確認用戶是否已經存在
            val existingMerchandise = productDBManager.getMerchandiseByID(id)
            if (existingMerchandise == null) {
                val item = Product(pId = id, imageUrl = image, pName = name, pType = type, pBarcode = barcode, pNumber = number, pPrice = price, selectedQuantity = 0)
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