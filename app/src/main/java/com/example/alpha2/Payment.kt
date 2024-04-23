package com.example.alpha2

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.System.SystemManager

class Payment : AppCompatActivity() {

    //從HomeFragment取得的購物清單內容
    private var filterList = mutableListOf<Product>()           //商品項次
    private var filterAmount = mutableMapOf<Product,Int>()      //商品項次 與 對應購買數
    private var totalPrice = 0                                  //購買總價
    private var nowLoginMember: Member? = null                  //會員

    private lateinit var systemDBManager: SystemManager         //系統主檔 (取得支援的付款方式)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //初始化Dao
        systemDBManager = SystemManager(this)

        // 使用 intent 獲取 商品清單
        try {
            filterList = intent.getSerializableExtra("filteredList_key") as MutableList<Product>
            filterAmount = intent.getSerializableExtra("quantities_key") as MutableMap<Product, Int>
            totalPrice = intent.getIntExtra("total_price",0)
            nowLoginMember = intent.getSerializableExtra("now_member") as? Member

            Log.d("調用商品清單測試", filterList.toString())
            Log.d("調用商品數量測試", filterAmount.toString())
            Log.d("小計總額", totalPrice.toString())
            Log.d("會員資訊",nowLoginMember.toString())

        }catch (e: Exception){
            Log.d("傳遞失敗","空的intent")
        }

        //所有允許的付款方式
        val paymentList = systemDBManager.getAllPaymentMethod()

        if (paymentList != null) {
            for (i in paymentList){
                Log.d("付款方式", i.toString())
            }
        }
    }
}