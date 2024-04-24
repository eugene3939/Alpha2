package com.example.alpha2

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.GridView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.System.SystemManager
import com.example.alpha2.databinding.ActivityPaymentBinding
import com.example.alpha2.myAdapter.FilterProductAdapter


class Payment : AppCompatActivity() {

    //從HomeFragment取得的購物清單內容
    private var filterList = mutableListOf<Product>()           //商品項次
    private var filterAmount = mutableMapOf<Product,Int>()      //商品項次 與 對應購買數
    private var totalPrice = 0                                  //購買總價
    private var nowLoginMember: Member? = null                  //會員

    private lateinit var systemDBManager: SystemManager         //系統主檔 (取得支援的付款方式)

    private var _binding: ActivityPaymentBinding? = null
    private val binding get() = _binding!!

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

        // 用 intent 獲取 商品清單
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

            try {
                // 創建一個字母列表
                val letters = paymentList.map { it.PAY_Name }

                // 使用 ArrayAdapter 將字母列表與 GridView 連接
                val adapter = if (nowLoginMember != null){
                     FilterProductAdapter(filterList,filterAmount,true)
                }else{
                     FilterProductAdapter(filterList,filterAmount,false)
                }

                // 獲取 GridView 並設置適配器
                val gridView: GridView = findViewById(R.id.gr_paymentMethod)
                gridView.adapter = adapter
                gridView.numColumns = 1

            } catch (e: Exception) {
                Log.e("警告", e.toString())
                Log.d("名稱清單", paymentList.map { it.PAY_Name }.toString())
            }
        } else {
            Log.e("警告", "paymentList 為空")
        }
    }
}