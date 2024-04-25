package com.example.alpha2

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.GridView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.System.PaymentMethod
import com.example.alpha2.DBManager.System.SystemManager
import com.example.alpha2.myAdapter.FilterProductAdapter


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

        //初始化
        val gridView: GridView = findViewById(R.id.gr_paymentMethod)    //商品清單GridView
        val btnPayment: Button = findViewById(R.id.btnsCode)            //切換  載具按鈕 (載具、愛心碼...)
        val btnCash: Button = findViewById(R.id.btnsPayment)            //切換  支付類別 (現金、信用卡...)

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
                //用GridView 顯示 購買清單
                val adapter = if (nowLoginMember != null){
                     FilterProductAdapter(filterList,filterAmount,true)
                }else{
                     FilterProductAdapter(filterList,filterAmount,false)
                }

                // 獲取 GridView 並設置適配器
                gridView.adapter = adapter
                gridView.numColumns = 1

            } catch (e: Exception) {
                Log.e("警告", e.toString())
                Log.d("名稱清單", paymentList.map { it.PAY_Name }.toString())
            }
        } else {
            Log.e("警告", "paymentList 為空")
        }

        //點擊按鈕時跳出畫面 協助用戶切換 載具形式
        btnPayment.setOnClickListener {
            showReceiptAlertDialog(this,paymentList)
            Toast.makeText(this,"切換載具形式",Toast.LENGTH_SHORT).show()
        }

        //點擊按鈕時跳出畫面 協助用戶切換 支付方式
        btnCash.setOnClickListener {
            Toast.makeText(this,"切換支付方式",Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("WrongViewCast")
    fun showReceiptAlertDialog(context: Context, paymentList: MutableList<PaymentMethod>?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.chooseitem, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(customView)

        val showUpGridView = customView.findViewById<ListView>(R.id.lvSelectionOption)
        val btnCancel = customView.findViewById<Button>(R.id.btnSelectionCancel)

        //內容清單
        // 檢查支付方式列表是否為空
        if (paymentList != null) {
            // 轉換 PaymentMethod 對象為 Map
            val dataList = paymentList.map { paymentMethod ->
                mapOf("a" to paymentMethod.PAY_No, "b" to paymentMethod.PAY_Name)
            }

            // 設置 SimpleAdapter
            val from = arrayOf("a", "b")
            val to = intArrayOf(android.R.id.text1, android.R.id.text2)
            val adapter = SimpleAdapter(
                context,
                dataList,
                android.R.layout.simple_list_item_2,
                from,
                to
            )

            showUpGridView.adapter = adapter
        }

        //取消變更按鈕
        btnCancel.setOnClickListener {
            Toast.makeText(this,"哈囉你好嗎~衷心感謝",Toast.LENGTH_SHORT).show()
        }

        // 添加確定按鈕
        builder.setPositiveButton("確定") { dialog, which ->
            // 在確定按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }

        // 添加取消按鈕
        builder.setNegativeButton("取消") { dialog, which ->
            // 在取消按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }

        // 創建並顯示對話框
        val dialog = builder.create()
        dialog.show()
    }
}