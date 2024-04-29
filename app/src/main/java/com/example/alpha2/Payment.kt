package com.example.alpha2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Payment.PaymentMain
import com.example.alpha2.DBManager.Payment.PaymentManager
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.System.PaymentMethod
import com.example.alpha2.DBManager.System.SystemManager
import com.example.alpha2.myAdapter.FilterProductAdapter
import java.time.LocalDateTime


class Payment : AppCompatActivity() {
    //從HomeFragment取得的購物清單內容
    private var filterList = mutableListOf<Product>()           //商品項次
    private var filterAmount = mutableMapOf<Product,Int>()      //商品項次 與 對應購買數
    private var totalPrice = 0                                  //購買總價
    private var nowLoginMember: Member? = null                  //會員

    private lateinit var systemDBManager: SystemManager         //系統主檔 (取得支援的付款方式)
    private lateinit var paymentDBManager: PaymentManager       //付款主檔 (取得付款相關Dao資料)

    //目前的支付方式
    private var nowPaymentMethod: String? = "01"                //預設支付方式為現金
    private var nowInvoiceText: String? = null                  //載具/電子發票號碼

    @SuppressLint("MissingInflatedId")
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
        val txtCartAmount: TextView = findViewById(R.id.txtCartAmount)  //商品數
        val txtCashSum: TextView = findViewById(R.id.txtCashSum)        //小計總額
        val txtChange: TextView = findViewById(R.id.txtChange)          //找零金額
        val edtEnterCash: EditText = findViewById(R.id.edtCashAmount)   //輸入的商品金額( 如果是電子支付不允許此操作)
        val btnPayment: Button = findViewById(R.id.btnsCode)            //切換  載具按鈕 (載具、愛心碼...)
        val btnCash: Button = findViewById(R.id.btnsPayment)            //切換  支付類別 (現金、信用卡...)
        val btnConfirm: Button = findViewById(R.id.btnsPaymentConfirm)  //完成交易 按鈕 (按下後送出交易主檔)

        //初始化Dao
        systemDBManager = SystemManager(this)
        paymentDBManager = PaymentManager(this)

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

            //顯示商品件數
            txtCartAmount.text = filterList.size.toString()
            //顯示小計總額
            txtCashSum.text = totalPrice.toString()

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

        //依照輸入金額自動顯示找零
        edtEnterCash.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 文字變化後顯示找零金額
                val totalAmount = edtEnterCash.text.toString().toIntOrNull() ?: 0
                val change = totalAmount - totalPrice
                if (change >= 0)
                    txtChange.text = change.toString()
                else
                    txtChange.text = "金額不足"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 不需要實現
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 不需要實現
            }
        })

        //點擊按鈕時跳出畫面 協助用戶切換 載具形式
        btnPayment.setOnClickListener {
            //所有允許的發票內容 (無統編、統編、愛心碼、電子載具...)

            val allowedInvoiceContents = listOf(
                "無統編",
                "統編",
                "愛心碼",
                "電子載具"
            )

            showInvoiceAlertDialog(this,allowedInvoiceContents,"選擇發票設定")
        }

        //點擊按鈕時跳出畫面 協助用戶切換 支付方式
        btnCash.setOnClickListener {
            showReceiptAlertDialog(this,paymentList,"選擇支付方式")
        }

        //按下確定按鈕後產生交易主檔
        btnConfirm.setOnClickListener {

            //顯示目前店號、機號
            val nowCashSystem = systemDBManager.getCashSystemNoById("1")
            val nowSystem = systemDBManager.getSystemSettingNoById("cashRegister123")

            if (nowCashSystem!= null && nowSystem!= null){
                Log.d("目前店號",nowSystem.storeNo)
                Log.d("目前收銀機號",nowSystem.ecrNo)

                var seqNo = 1
                for (i in filterList){  //將每一個商品項次都儲存到 即時銷售主檔
                    val paymentMainItem = PaymentMain(SYS_StoreNo = nowSystem.storeNo,
                                                        TXN_Date = LocalDateTime.now(),
                                                        ECR_No = nowSystem.ecrNo,
                                                        TXN_No = seqNo,

                                                        TXN_Time =  LocalDateTime.now(),
                                                        USR_No = "Eugene",
                                                        TXN_Uniform = "統一編號",
                                                        TXN_MemCard = "會員卡號碼",
                                                        TXN_GUIPaper = "3",     /*2=二聯式 3=三聯式 N=免開發票 E=電子發票 R=銷退單*/
                                                        TXN_GUIBegNo = "起始發票號碼",
                                                        TXN_GUICnt = 1,
                                                        TXN_TotQty = filterList.size,
                                                        TXN_TotDiscS = 0,
                                                        TXN_TotDiscM = 0,
                                                        TXN_TotDiscT = 0,
                                                        TXN_TotSaleAmt = totalPrice,
                                                        TXN_TotGUI = totalPrice,
                                                        TXN_Mode = "N",
                                                        TXN_TotPayAmt = totalPrice)

                    paymentDBManager.addPaymentMain(paymentMainItem)

                    seqNo +=1
                }

                //成功送出後回到主畫面
                val intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    //選擇支付方式
    @SuppressLint("WrongViewCast")
    fun showReceiptAlertDialog(
        context: Context,
        paymentList: MutableList<PaymentMethod>?,
        title: String
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.chooseitem, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(customView)

        //顯示標題
        val customTitle = customView.findViewById<TextView>(R.id.txtSelectionTitle)

        //顯示支付清單內容
        val showUpListView = customView.findViewById<ListView>(R.id.lvSelectionOption)

        customTitle.text = title

        //內容清單
        // 檢查支付方式列表是否為空
        if (paymentList != null) {
            // 轉換 PaymentMethod 對象為 Map
            val dataList = paymentList.map { paymentMethod ->
                mapOf("a" to paymentMethod.PAY_No, "b" to paymentMethod.PAY_Name)
            }.sortedBy { it["a"] }

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

            showUpListView.adapter = adapter

            // 監聽 ListView 的點擊事件
            showUpListView.setOnItemClickListener { _, _, position, _ ->
                // 獲取點擊的位置
                val clickedItem = dataList[position]
                val paymentNo = clickedItem["a"]
                val paymentName = clickedItem["b"]

                nowPaymentMethod = clickedItem["a"]

                nowPaymentMethod?.let { Log.d("點點位置", it) }

                // 顯示 Toast
                Toast.makeText(context, "點擊位置: $position, 支付編號: $paymentNo, 支付名稱: $paymentName", Toast.LENGTH_SHORT).show()
            }
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

    //選擇載具
    @SuppressLint("WrongViewCast")
    fun showInvoiceAlertDialog(context: Context, allowedInvoiceContents: List<String>, title: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.chooseitem, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(customView)

        //顯示支付清單內容
        val showUpListView = customView.findViewById<ListView>(R.id.lvSelectionOption)
        //顯示標題
        val customTitle = customView.findViewById<TextView>(R.id.txtSelectionTitle)

        // 創建並顯示對話框
        val dialog = builder.create()
        dialog.show()

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, allowedInvoiceContents)
        showUpListView.adapter = adapter

        //設置標題
        customTitle.text = title

        // 設置 ListView 的點擊事件監聽器
        showUpListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            // 獲取用戶點擊的發票內容
            val selectedItem = allowedInvoiceContents[position]

            // 打印選擇的內容
            println("用戶選擇的發票內容：$selectedItem")

            if (position != 0){     //無統編以外的支付方式就開啟支付框
                //關閉當前的alertDialog降低資源使用
                dialog.dismiss()

                //開啟對應的輸入頁，讓用戶輸入載具
                shoeEditTextDialog(context,selectedItem)
            }
        }

        // 添加確定按鈕
        builder.setPositiveButton("確定") { dialog, _ ->
            // 在確定按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }

        // 添加取消按鈕
        builder.setNegativeButton("取消") { dialog, _ ->
            // 在取消按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }
    }

    //開啟對應的輸入頁，讓用戶輸入載具 (可以用 selectedPaymentItem 指定支付方式對應的規則，目前先不使用)
    @SuppressLint("SetTextI18n")
    private fun shoeEditTextDialog(context: Context, selectedPaymentItem: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customView = inflater.inflate(R.layout.entertext, null)

        val builder = AlertDialog.Builder(context)
        builder.setView(customView)

        //顯示支付清單內容
        val myEditText = customView.findViewById<EditText>(R.id.edtEnterTxtContent)
        //顯示標題
        val customTitle = customView.findViewById<TextView>(R.id.txtEnterTxtTitle)

        //設置標題
        customTitle.text = "輸入$selectedPaymentItem"

        // 添加確定按鈕
        builder.setPositiveButton("確定") { dialog, _ ->
            //儲存到全域變數
            nowInvoiceText = myEditText.text.toString()

            println("我儲存的支付方式 $nowInvoiceText")

            // 在確定按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }

        // 添加取消按鈕
        builder.setNegativeButton("取消") { dialog, _ ->
            // 在取消按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }

        // 創建並顯示對話框
        val dialog = builder.create()
        dialog.show()
    }
}