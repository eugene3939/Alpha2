package com.example.alpha2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
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
import com.example.alpha2.DBManager.Invoice.InvoiceManager
import com.example.alpha2.DBManager.Invoice.InvoiceSetup
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Payment.PaymentDetail
import com.example.alpha2.DBManager.Payment.PaymentMain
import com.example.alpha2.DBManager.Payment.PaymentManager
import com.example.alpha2.DBManager.System.PaymentMethod
import com.example.alpha2.DBManager.System.SystemManager
import com.example.alpha2.DBManager.System.SystemSetting
import com.example.alpha2.myAdapter.FilterProductAdapter
import com.example.alpha2.myObject.CartItem
import java.time.LocalDateTime
import kotlin.math.roundToInt


class Payment : AppCompatActivity() {
    //從HomeFragment取得的購物清單內容
    private var totalPrice = 0                                  //應付金額
    private var nowLoginMember: Member? = null                  //會員
    private var cartList = mutableListOf<CartItem>()            //購物車項目

    private lateinit var systemDBManager: SystemManager         //系統主檔 (取得支援的付款方式)
    private lateinit var paymentDBManager: PaymentManager       //付款主檔 (取得付款相關Dao資料)
    private lateinit var invoiceDBManager: InvoiceManager       //發票號碼設定檔 (取得發票號碼相關Dao資料)

    //目前的支付方式
    private var nowPaymentCode: String? = "無統編"               //付款類別: 無統編、統編、愛心碼... (預設支付方式為無統編)
    private var nowPaymentMethod: String? = "現金"               //支付方式: 現金、電子支付...      (預設支付方式為現金)
    private var nowInvoiceText: String? = null                  //載具/電子發票號碼

    private var nowPayment: Int = 0                             //實付金額

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
        val txtMember: TextView = findViewById(R.id.txtMember)          //會員
        val txtCashSum: TextView = findViewById(R.id.txtCashSum)        //小計總額
        val txtChange: TextView = findViewById(R.id.txtChange)          //找零金額
        val edtEnterCash: EditText = findViewById(R.id.edtCashAmount)   //輸入的商品金額( 如果是電子支付不允許此操作)
        val btnsCode: Button = findViewById(R.id.btnsCode)            //切換  載具按鈕 (載具、愛心碼...)
        val btnsPayment: Button = findViewById(R.id.btnsPayment)            //切換  支付類別 (現金、信用卡...)
        val btnConfirm: Button = findViewById(R.id.btnsPaymentConfirm)  //完成交易 按鈕 (按下後送出交易主檔)

        //初始化Dao
        systemDBManager = SystemManager(this)
        paymentDBManager = PaymentManager(this)
        invoiceDBManager = InvoiceManager(this)

        // 用 intent 獲取 商品清單
        try {
            cartList = intent.getSerializableExtra("cartList_key") as MutableList<CartItem>

            totalPrice = intent.getDoubleExtra("total_price",0.00).roundToInt()
            nowLoginMember = intent.getSerializableExtra("now_member") as? Member

            Log.d("小計總額", totalPrice.toString())
            Log.d("會員資訊",nowLoginMember.toString())

            //顯示商品件數
            txtCartAmount.text = cartList.size.toString()
            //顯示小計總額
            txtCashSum.text = totalPrice.toString()
            //顯示會員內容
            txtMember.text = nowLoginMember?.name ?: "非會員"  //顯示是否為會員身分

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
                    FilterProductAdapter(cartList,true)
                }else{
                    FilterProductAdapter(cartList,false)
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
                val totalAmount = edtEnterCash.text.toString().toDoubleOrNull() ?: 0.00
                val change = totalAmount - totalPrice
                if (change >= 0){
                    txtChange.text = change.roundToInt().toString()

                    //更新支付金額
                    nowPayment = totalAmount.roundToInt()
                }
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
        btnsCode.setOnClickListener {
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
        btnsPayment.setOnClickListener {
            showReceiptAlertDialog(this,paymentList,"選擇支付方式")
        }

        //按下確定按鈕後產生交易主檔 和 交易明細檔
        btnConfirm.setOnClickListener {
            //目前店號、機號、日期(localDateTime)
            val nowCashSystem = systemDBManager.getCashSystemNoById("031")
            val nowSystem = systemDBManager.getSystemSettingNoById("031")
            val nowDate = LocalDateTime.of(LocalDateTime.now().year, LocalDateTime.now().monthValue, 1, 0, 0, 0, 0)

            // 用sharedReference取得豋入頁面資料
            val sharedPreferences = getSharedPreferences("loginUser", Context.MODE_PRIVATE)
            // 目前登入的收銀員
            val clerkId = sharedPreferences.getString("userId", null)
            //目前的交易模式
            val transactionMethod = sharedPreferences.getString("transactionMethod","收銀")

            if (nowCashSystem!= null && nowSystem!= null && clerkId!=null) { //顯示目前機號
                Log.d("目前店號", nowSystem.storeNo)
                Log.d("目前收銀機號", nowSystem.ecrNo)
                Log.d("目前交易模式",transactionMethod.toString())

                //送出發票前先確認發票效期
                val invoiceYYYYMM = getYYYYMM() //取得效期
                //確認是否有對應的發票效期
                val existInvoiceSetup = invoiceDBManager.getInvoiceSetupsBy(nowSystem.storeNo,invoiceYYYYMM,nowSystem.ecrNo,"invoiceSerialNo")

                if (existInvoiceSetup != null){     //確認效期許可才會開立發票
                    //付款金額必須大於等於應付金額
                    if (nowPayment >= totalPrice){
                        val paymentMainItem = PaymentMain(
                            SYS_StoreNo = nowSystem.storeNo,
                            TXN_Date = nowDate,                 /*交易日期*/
                            ECR_No = nowSystem.ecrNo,                       /*收銀機代碼*/
                            TXN_No = paymentDBManager.searchPaymentMainByMaxYYMM(nowDate)?.plus(1)
                                ?: 1,/*交易序號 (今日開出數+1 ，確認今天開出幾張，由1開始)*/

                            TXN_Time =  LocalDateTime.now(),               /*交易時間*/
                            USR_No = clerkId,                              /*收銀員號碼*/
                            TXN_Uniform = nowInvoiceText.toString(),       /*統一編號*/
                            TXN_MemCard = nowLoginMember?.id.toString(),   /*會員卡號碼*/
                            TXN_GUIPaper = "E",     /*2=二聯式 3=三聯式 N=免開發票 E=電子發票 R=銷退單*/
                            TXN_GUIBegNo = existInvoiceSetup.GUI_TRACK.toString() + existInvoiceSetup.GUI_SNOS.toString(),  /*發票起始發票號*/
                            TXN_GUICnt = 1,                 /*發票張數*/
                            TXN_TotQty = cartList.size,   /*總數量*/
                            TXN_TotDiscS = cartList.sumOf { it.discountS },               /*總人工折扣(負數) 最優先*/
                            TXN_TotDiscT = cartList.filter { it.productItem.pluMagNo != "0000000" }.sumOf { it.discountT },        /*總合小計折扣 目前僅依照會員價與商  品單價之差價 第二優先*/
                            TXN_TotDiscM = nowLoginMember?.let { cartList.sumOf{ (it.productItem.unitPrc - it.productItem.memPrc) * (it.quantity) }}?: 0.00,               /*總會員折扣(負數) 最後算*/

                            TXN_TotSaleAmt = nowPayment.toDouble(),    /*總銷售金額=總應稅銷售金額+總免稅銷售金額  銷售明細加總*/
                            TXN_TotGUI = totalPrice,        /*總發票金額 有多少錢是要開發票的(可能有禮券，禮券已經開過了)*/
                            TXN_Mode = when(transactionMethod){
                                "收銀" -> "R"
                                "補輸入"-> "E"
                                "訓練"->"T"
                                "預收取貨"->"W"
                                "銷退"->"Y"
                                "折讓"->"X"
                                "財務手開"->"F"
                                else -> "F"},                 /*交易模式*/
                            TXN_Status = "N",
                            TXN_TotPayAmt = nowPayment.toDouble())     /*總付款金額 付款明細加總*/

                        paymentDBManager.addPaymentMain(paymentMainItem)

                        //更新下一個發票序號
                        updateNextInvoiceNumber(existInvoiceSetup,invoiceYYYYMM)

                        //建立即時銷售明細檔
                        buildPaymentDetail(nowSystem,nowDate,existInvoiceSetup,transactionMethod)

                        //成功送出後回到主畫面
                        val intent = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this,"未達到應付金額",Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this,"不在許可發票效期",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //建立商品明細檔
    private fun buildPaymentDetail(
        nowSystem: SystemSetting,
        nowDate: LocalDateTime,
        existInvoiceSetup: InvoiceSetup,
        transactionMethod: String?
    ) {
        //目前最大的TXN_No(交易序號) ，向上疊加
        val maxTXN = paymentDBManager.searchPaymentDetailByMaxYYMM(nowDate)?.plus(1) ?: 1
        var temp = 1    //項次
        for (item in cartList){
            if (item.productItem.pluMagNo == "0000000"){    //跳過小計折扣商品
                continue
            }else{
                val paymentDetailItem = PaymentDetail(
                    SYS_StoreNo = nowSystem.storeNo,
                    TXN_Date = nowDate,
                    ECR_No = nowSystem.ecrNo,
                    TXN_No = maxTXN,       /*交易序號 (今日開出數+1 ，確認今天開出幾張，由1開始)*/

                    TXN_Item = temp,
                    TXN_Time = LocalDateTime.now(),
                    TXN_GUINo = existInvoiceSetup.GUI_TRACK.toString() + existInvoiceSetup.GUI_SNOS.toString(),  /*發票起始發票號*/
                    PLU_No = item.productItem.pluMagNo,
                    DEP_No = item.productItem.DEP_No,
                    VEN_No = item.productItem.VEN_No,
                    CAT_No = item.productItem.CAT_No,
                    TXN_Qty = item.quantity,
                    PLU_FixPrc = item.productItem.fixPrc,
                    PLU_SalePrc = item.productItem.salePrc,

                    TXN_DiscS =item.discountS.roundToInt().toDouble(),                   /*人工折扣(負數)*/
                    TXN_DiscT = item.discountT.roundToInt().toDouble(),                  /*總合折扣(負數)*/
                    TXN_DiscM = nowLoginMember?.let { (item.productItem.unitPrc - item.productItem.memPrc) * item.quantity }?: 0.00,   /*會員折扣 目前僅計算會員差價*/

                    TXN_SaleAmt = item.productItem.unitPrc * item.quantity - item.discountS.roundToInt().toDouble() - item.discountT.roundToInt().toDouble(),  /*銷售金額=應稅銷售金額+免稅銷售金額*/
                    TXN_SaleTax = item.productItem.unitPrc,  /*應稅銷售金額=未稅銷售金額+稅額*/
                    TXN_SaleNoTax = item.productItem.unitPrc,/*免稅銷售金額*/
                    TXN_Net = item.productItem.unitPrc,      /*未稅銷售金額*/
                    TXN_Tax = 0.00,                    /*稅額*/
                    PLU_TaxType = "0",                 /*稅別 0=免稅 1=應稅*/

                    TXN_Mode = when(transactionMethod){
                        "收銀" -> "R"
                        "補輸入"-> "E"
                        "訓練"->"T"
                        "預收取貨"->"W"
                        "銷退"->"Y"
                        "折讓"->"X"
                        "財務手開"->"F"
                        else -> "F"},               /*交易模式(同POS3008)*/
                    TXN_Status = "N",               /*交易狀態(同POS3008,R=退貨)*/

                    PLU_Name = item.productItem.pName
                )

                //寫入明細檔
                paymentDBManager.addPaymentDetail(paymentDetailItem)

                temp+=1
            }
        }
    }

    //更新下一個發票序號
    private fun updateNextInvoiceNumber(existInvoiceSetup: InvoiceSetup, invoiceYYYYMM: String) {
        val currentGUI = existInvoiceSetup.GUI_SNOS?.toInt() ?: 0
        val nextGUI = String.format("%06d", currentGUI + 1)

        val currentNEXT_SNOS = existInvoiceSetup.NEXT_SNOS?.toInt() ?: 0
        val nextNEXT_SNOS = String.format("%06d", currentNEXT_SNOS + 1)

        //如果發票設定檔還是未使用狀態就進行更新
        if (existInvoiceSetup.STATUS == "0"){
            invoiceDBManager.updateStatus("01","03",invoiceYYYYMM)     //變更狀態為使用中
        }

        //成功開立發票後更新發票號 (該效期的發票號 起始號 、下一號 進行變更)
        invoiceDBManager.updateGUI_NEXT(nextGUI,nextNEXT_SNOS,"03",invoiceYYYYMM)
    }

    //將目前日期(LocalDateTime)轉換為YYYYMM
    private fun getYYYYMM(): String {

        // 取得目前時間
        val now = LocalDateTime.now()

        // 從時間中擷取年份和月份
        val year = now.year
        val month = now.monthValue

        // 生成表示法 "YYYYMM"
        // 將單數月份轉換成雙數形式
        val formattedMonth = if (month % 2 == 0) {
            (month - 1).toString().padStart(2, '0')
        } else {
            month.toString().padStart(2, '0')
        }

        // 生成表示法 "YYYYMM"
        val yearMonthString = "%04d%s".format(year, formattedMonth)

        // 回傳結果
        return  yearMonthString
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
        //變更顯示標題
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

                nowPaymentMethod = paymentName

                nowPaymentMethod?.let { Log.d("點擊位置", it) }

                //如果選現金以外欄位，自動填入金額
                if (position != 0 ){
                    val edtEnterCash: EditText = findViewById(R.id.edtCashAmount)

                    nowPayment = totalPrice

                    Log.d("目前支付金額",nowPayment.toString())

                    //更新支付方式
                    val btnConfirm: Button = findViewById(R.id.btnsPayment)
                    btnConfirm.text = paymentName

                    edtEnterCash.setText(nowPayment.toString())    //變更文本內容
                }

                // 顯示 Toast
                Toast.makeText(context, "點擊位置: $position, 支付編號: $paymentNo, 支付名稱: $paymentName", Toast.LENGTH_SHORT).show()
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
        builder.setPositiveButton("確定") { _, _ ->
            // 在確定按鈕點擊時執行的操作
            dialog.dismiss() // 關閉對話框
        }

        // 添加取消按鈕
        builder.setNegativeButton("取消") { _, _ ->
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
            nowInvoiceText = myEditText.text.toString() //統編號碼
            nowPaymentCode = selectedPaymentItem        //統一編號類別

            println("我儲存的發票形式 $nowInvoiceText")

            //變更按鈕的顯示內容(載具、愛心碼...)
            val btnsCode: Button = findViewById(R.id.btnsCode)
            btnsCode.text = selectedPaymentItem

            //變更文字的顯示內容(載具、愛心碼...)
            val btnsCodeTitle: TextView = findViewById(R.id.txtScodeTitle)
            btnsCodeTitle.text = selectedPaymentItem

            //載具顯示內容
            val btnsCodeVale: TextView = findViewById(R.id.txtVATNumber)
            btnsCodeVale.text = nowInvoiceText

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