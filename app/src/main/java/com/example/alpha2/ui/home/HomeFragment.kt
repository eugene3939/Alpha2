package com.example.alpha2.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.Product.ProductManager
import com.example.alpha2.DBManager.User.UserManager
import com.example.alpha2.databinding.FragmentHomeBinding
import com.google.zxing.integration.android.IntentIntegrator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.alpha2.DBManager.Member.Member
import com.example.alpha2.DBManager.Member.MemberManager
import com.example.alpha2.DBManager.Payment.PaymentDetail
import com.example.alpha2.DBManager.Product.CouponDetail
import com.example.alpha2.DBManager.Product.CouponMain
import com.example.alpha2.Payment
import com.example.alpha2.R
import com.example.alpha2.myAdapter.CouponAdapter
import com.example.alpha2.myAdapter.FilterProductAdapter
import com.example.alpha2.myObject.CartItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.lang.Exception
import java.time.LocalDateTime
import kotlin.math.roundToInt

//不允許螢幕旋轉，螢幕旋轉容易導致資料流失

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)
    private lateinit var productDBManager: ProductManager //商品Dao
    private lateinit var memberDBManager: MemberManager   //會員Dao

    //用購物車物件管理條目(包含商品、折扣、購買數、稅別...可以新增不同需求)---------------------------

    //購物車項次(單次交易)
    private var Cno:Int = 0
    //購物車項次物件 (序號、貨號、購買數量、折扣)
    private val cartList = mutableListOf<CartItem>()    //購物車項次清單

    private val cartDetail = mutableListOf<PaymentDetail>()             //交易明細清單 (按下小計後會送出此清單)
    private val deleteCartDetail = mutableListOf<PaymentDetail>()       //交易明細清單紀錄 (回復紀錄，可能會進行儲存)

    //--------------------------------------------------------------------------------------

    private val REQUEST_CODE_SCAN = 1002    //掃描請求碼

    private var existItemCheck = false      //檢查掃描商品是否存在於Dao

    //activity首次創建(初次開啟或是螢幕旋轉)
    private var isFirstCreation = true

    //用物件保留刪除的購物車資訊
    //刪除的購物車索引
    private var Dno:Int = 0
    data class DeletedCartItem(val deleteOrder: Int, val cartItem: CartItem)
    //刪除掉的商品購物車內容
    private val deleteCartList = mutableListOf<DeletedCartItem>()

    //目前會員
    private var nowLoginMember: Member? = null

    //總小計金額
    private var totalSumUnitPrice: Double = 0.00
    //全折後金額:
    private var totalSumPro: Double = 0.00

    //鏡頭開啟時處理條碼邏輯 (加入會員)
    @SuppressLint("SetTextI18n")
    private val barcodeMemberScannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, data)
            if (scanResult != null) {

                var stateHintCode = 1   //代碼 (0: 掃描成功,1: 不存在會員)
                if (scanResult.contents == null) {
                    Toast.makeText(requireContext(), "掃描取消", Toast.LENGTH_LONG).show()
                } else {
                    //確認Dao會員是否包含掃描項目
                    lifecycleScope.launch {
                        val scanMember = withContext(Dispatchers.IO) {
                            memberDBManager.getMemberByCardNo(scanResult.contents)
                        }
                        if (scanMember != null) {
                            Log.d("存在對應會員", scanMember.name)
                            nowLoginMember = scanMember //紀錄目前登入的用戶
                            binding.btnUserFavor.text = "會員"

                            Log.d("取得用戶資料",nowLoginMember.toString())

                            stateHintCode = 0
                        } else {
                            nowLoginMember = null       //重置會員名稱
                            binding.btnUserFavor.text = "非會員"
                            Log.d("不存在此會員", scanResult.contents)
                        }

                        loadFilterProduct()     //重新載入清單

                        withContext(Dispatchers.Main) {

                            //確認掃描結果，並顯示於主畫面
                            when (stateHintCode) {
                                0 -> Toast.makeText(requireContext(), "會員: ${scanMember?.name}", Toast.LENGTH_SHORT).show()
                                1 -> Toast.makeText(requireContext(), "不存在此會員: ${scanResult.contents}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    //鏡頭開啟時處理條碼邏輯 (加入商品)
    private val barcodeScannerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, data)
            if (scanResult != null) {
                if (scanResult.contents == null) {
                    Toast.makeText(requireContext(), "掃描取消", Toast.LENGTH_LONG).show()
                } else {
                    productScanCheck(scanResult.contents)    //確認掃描結果能否加入購物車
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //初始化Dao
        userDBManager = UserManager(requireContext())
        productDBManager = ProductManager(requireContext())
        memberDBManager = MemberManager(requireContext())

        //shareReference讀取登入用戶ID
        val sharedPreferences = requireContext().getSharedPreferences("loginUser", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userId", null)

        //確認用戶身分
        if (userID!=null){
            val accessUser = userDBManager.getUserById(userID)
            if (accessUser != null) {
                binding.textDashboard.text = "收銀員: ${accessUser.name}"
            }
        }else{
            binding.textDashboard.text = "Null user access"
        }

        // 如果是首次創建 Fragment，則從 ViewModel 中讀取資料
        if (isFirstCreation) {
            loadFilterProduct()
            isFirstCreation = false
        }

        // BottomSheetDialog修改掃描商品數量
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.selectnum) // 將你的 變更數量的xml 設置為BottomView內容

        // 找到 修改數量畫面的 +1按鈕
        val btnPlus1 = bottomSheetDialog.findViewById<Button>(R.id.btnSelectPlus1)
        // 找到 修改數量畫面的 -1按鈕
        val btnMinus1 = bottomSheetDialog.findViewById<Button>(R.id.btnSelectMinus1)
        // 找到 修改數量畫面的 確定按鈕
        val btnConfirm = bottomSheetDialog.findViewById<Button>(R.id.btnScanNumConfirm)
        // 找到 目前數量的 輸入框
        val edtNumber = bottomSheetDialog.findViewById<EditText>(R.id.edtScanNumber)

        //變更購物車商品數量(只能變更最後一項，並選0會跳出是否更正之提醒，+1 -1 取消)
        binding.grTableProduct.setOnItemClickListener { parent, _, position, _ ->
            //目前點按項目
            val clickItem = parent.getItemAtPosition(position) as CartItem //進行強制轉型確認購物車類別
            // 顯示 BottomView
            bottomSheetDialog.show()

            //這邊先確定點及項目是否為最後一項(最後一項才允許變更數量)
            if (clickItem == cartList.last()){
                if (btnPlus1 != null && btnMinus1 != null && btnConfirm!=null) {
                    //點擊商品的目前數量
                    val selectScanNumber = cartList.last().quantity

                    //點擊數量(如果是折價券就變成負金額)
                    var changeAmount= selectScanNumber
                    if (clickItem.productItem.pluType == "75"){
                        changeAmount *= (-1)
                    }

                    //顯示點數量
                    if (edtNumber != null) {
                        //如果是折價券商品就變成負金額
                        edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                    }

                    // 更新editText內容
                    edtNumber?.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            // 不需要實現
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            // 當 EditText 的文字變更時，更新 changeAmount 的值
                            if (!s.isNullOrEmpty()) {
                                changeAmount = s.toString().toIntOrNull() ?: 0
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {
                            // 不需要實現
                        }
                    })

                    btnPlus1.setOnClickListener {
                        changeAmount += 1       //點擊數量+1
                        Log.d("數量","$changeAmount")

                        //顯示點擊數量
                        if (edtNumber != null) {
                            edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                        }
                    }
                    btnMinus1.setOnClickListener {
                        if (changeAmount >=1)   //數量最少要是1
                            changeAmount -= 1           //點擊數量-1

                        Log.d("數量","$changeAmount")

                        //顯示點擊數量
                        if (edtNumber != null) {
                            edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                        }
                    }
                    btnConfirm.setOnClickListener {
                        //如果已經有單向折扣了就不允許變更數量，要求先進行更正
                        if (clickItem.discountS !=0.0 ){
                            Toast.makeText(requireContext(),"折扣後不允許數量變更",Toast.LENGTH_SHORT).show()
                        }else if (clickItem.productItem.pName == "小計折扣"){    //小計折扣項目不允許更正
                            Toast.makeText(requireContext(),"小計折扣不允許數量變更",Toast.LENGTH_SHORT).show()
                        } else{
                            // 如果變更後的數量變成 0 就刪除選擇商品
                            if (changeAmount == 0) {
                                //進行更正作業
                                merchantClearProcess()
                            }else{
                                //更新成新數量
                                //如果商品類別是否為折價券
                                if (clickItem.productItem.pluType == "75"){ //如果屬於折價券類別，就將數量變成負值
                                    cartList[cartList.size-1].quantity = changeAmount*-1
                                }else{
                                    cartList[cartList.size-1].quantity = changeAmount*1
                                }

                                //更新購物車物件的數量
                                if (cartList.isNotEmpty()) {
                                    val lastItem = cartList.last()

                                    //如果是全折類別不允許變數量
                                    lastItem.quantity = changeAmount
                                    println("變更數量 ${lastItem.quantity}")
                                } else {
                                    println("購物車為空，無法變更數量")
                                }

                                Log.d("商品數量","${cartList.last().quantity}")
                            }
                        }

                        //重新載入清單
                        loadFilterProduct()

                        bottomSheetDialog.dismiss()     //結束bottomView
                    }
                }
            }else{
                bottomSheetDialog.dismiss() //關閉商品數量視窗
                Toast.makeText(requireContext(),"請先進行更正作業，再變更商品數量",Toast.LENGTH_SHORT).show()
            }
        }

        //更正按鈕(將最後一項商品刪除，並且留下刪除紀錄)
        binding.btnMerchantClear.setOnClickListener {
            merchantClearProcess()
        }

        //顯示用戶自定義側滑式清單
        binding.myNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_blue -> binding.textDashboard.setTextColor(Color.BLUE)
                R.id.nav_green -> binding.textDashboard.setTextColor(Color.GREEN)
                R.id.nav_yellow -> binding.textDashboard.setTextColor(Color.YELLOW)
            }
            true
        }

        //顯示會員輸入框的下拉式選單
        binding.btnUserFavor.setOnClickListener {
            val memberBottomDialog = BottomSheetDialog(requireContext())
            memberBottomDialog.setContentView(R.layout.member)

            // 找到 輸入會員卡號 editText
            val edtMemberCardNumber = memberBottomDialog.findViewById<EditText>(R.id.memEdt)
            // 找到 清除輸入的 確定按鈕
            val btnClearMemberID = memberBottomDialog.findViewById<Button>(R.id.memBtnClearTxt)
            // 找到 確定輸入的 確定按鈕
            val btnConfirmMemberID = memberBottomDialog.findViewById<Button>(R.id.memBtnConfirm)
            // 找到 掃描會員編號的 確定按鈕
            val btnScanMemberID = memberBottomDialog.findViewById<Button>(R.id.memBtnScan)

            memberBottomDialog.show()

            //清除輸入文字 (換成空白)
            btnClearMemberID?.setOnClickListener {
                edtMemberCardNumber?.text = Editable.Factory.getInstance().newEditable("")
            }

            //輸入會員
            btnConfirmMemberID?.setOnClickListener {
                if (edtMemberCardNumber != null){
                    //輸入的會員卡號
                    val memberCardNumber = edtMemberCardNumber.text.toString()

                    //副執行續進行member Dao查詢
                    lifecycleScope.launch(Dispatchers.IO) {
                        val accessMember = memberDBManager.getMemberByCardNo(memberCardNumber)

                        //確認是否為Dao的會員資訊
                        if (accessMember!=null){
                            Log.d("正確會員", memberCardNumber)
                            //顯示到主畫面
                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "會員: $memberCardNumber", Toast.LENGTH_SHORT).show()
                                nowLoginMember = accessMember //紀錄目前登入的用戶
                                //更新顯示會員名稱
                                binding.btnUserFavor.text = "會員"

                                Log.d("取得用戶資料",nowLoginMember.toString())
                                memberBottomDialog.dismiss()
                            }
                        }else{
                            Log.d("沒有此會員",memberCardNumber)
                            nowLoginMember = null //重置會員名稱

                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "沒有此會員: $memberCardNumber", Toast.LENGTH_SHORT).show()
                                //重設會員提示文字
                                binding.btnUserFavor.text = "非會員"
                            }
                        }

                        loadFilterProduct()     //重新載入清單
                    }
                }else{
                    Toast.makeText(requireContext(),"您沒有輸入卡號喔",Toast.LENGTH_SHORT).show()
                }
            }

            //開啟掃描器確認會員身分
            btnScanMemberID?.setOnClickListener {
                val integrator = IntentIntegrator.forSupportFragment(this)
                integrator.setPrompt("請對準條碼進行掃描")
                integrator.setBeepEnabled(true)
                integrator.setOrientationLocked(false)
                integrator.setRequestCode(REQUEST_CODE_SCAN)

                //啟動掃描頁
                barcodeMemberScannerLauncher.launch(integrator.createScanIntent())
            }
        }

        //點擊開啟掃描器
        binding.btBarcodeScanner.setOnClickListener {
            startBarcodeScanner()
        }

        //輸入貨號後新增商品(加入\移出購物車)
        binding.btnSearch.setOnClickListener {
            //搜尋文字
            val searchMgaNo: String = binding.edtSearchRow.text.toString()

            //掃描清單名稱列表
            val filterNameList: List<String> = cartList.map { it.productItem.pluMagNo }

            lifecycleScope.launch(Dispatchers.IO) {
                //結果代碼
                var resultCode = 0

                Log.d("filterNameLists內容",filterNameList.toString())
                Log.d("輸入內容",searchMgaNo)

                //輸入貨號查詢的商品
                val selectItem = productDBManager.getProductByMagNo(searchMgaNo)

                //輸入貨號不包含在清單，且存在於商品目錄，可新增
                if (!filterNameList.contains(searchMgaNo) && selectItem!=null){
                    //和規的貨號，加入清單
                    try {
                        //檢查是否為折價券商品
                        if (selectItem.pluType == "75"){
                            //確認折價券能否加入
                            val isCouponValid = couponAddCheck(selectItem)

                            if (isCouponValid) {
                                Cno+=1  //更新購物車項次
                                cartList.add(CartItem(Cno,selectItem,1,0.00))  //商品加入購物車

                                //更新購物車清單顯示內容
                                loadFilterProduct()

                                Log.d("加入商品", selectItem.pName)
                                resultCode = 99
                            } else {
                                Log.d("不符合折價券規格", "未達折價券指定最低金額")
                                resultCode = 3
                            }
                        }else{ //輸入貨號為一般商品
                            productScanCheck(selectItem.pluMagNo)   //檢查貨號
                            resultCode = 99
                        }

                    }catch (e: Exception){
                        println(e)
                    }

                }else{
                    //已存在或是不合規定的訂單編號
                    if (productDBManager.getProductByMagNo(searchMgaNo)==null){
                        Log.d("貨號搜尋錯誤","不存在的貨號")
                        resultCode = 1
                    } else if (filterNameList.contains(searchMgaNo)){
                        Log.d("貨號搜尋錯誤","已經加入這筆商品了")
                        resultCode = 2
                    }else{
                        Log.d("貨號搜尋錯誤","其他錯誤")
                    }
                }

                //顯示結果代碼
                withContext(Dispatchers.Main) {
                    when (resultCode){
                        0 -> Toast.makeText(requireContext(),"其他錯誤",Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(requireContext(),"不存在的貨號",Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(requireContext(),"已經加入這筆商品了",Toast.LENGTH_SHORT).show()
                        3 -> Toast.makeText(requireContext(),"未達折價券指定最低金額",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 自訂折價券按鈕(快速鍵)
        binding.btnDiscountList.setOnClickListener{
            //顯示的折價券清單
            val simpleBottomDialog = BottomSheetDialog(requireContext())
            simpleBottomDialog.setContentView(R.layout.couponlist)

            // 找到 顯示折扣清單 gridView
            val grCouponList = simpleBottomDialog.findViewById<GridView>(R.id.grCouponList)

            //用lifecycleScope找尋對應的Dao資料
            lifecycleScope.launch(Dispatchers.IO) {
                //折價券商品(全部欄位)
                val filterPluList = productDBManager.getProductByPluType("75")  //類別是折價券

                //折價券商品(顯示欄位)
                val showUpList = filterPluList?.map { "${it.pId}, ${it.pName}, ${it.unitPrc}" }

                // 顯示篩選出的列印資訊
                showUpList?.forEach { Log.d("Product Info", it) }

                withContext(Dispatchers.Main){
                    val couponAdapter = CouponAdapter(requireContext(), showUpList ?: emptyList())
                    grCouponList?.adapter = couponAdapter

                    couponAdapter.setOnItemClickListener(object : CouponAdapter.OnItemClickListener {

                        //取得按鈕點按位置
                        override fun onButtonClicked(position: Int) {

                            //顯示折價券貨號
                            val sentCouponItem = filterPluList!![position].pluMagNo

                            Log.d("折扣券號",sentCouponItem)

                            // 送出折價券商品
                            // 將外部操作也放在協程內部
                            lifecycleScope.launch(Dispatchers.IO) {
                                // 在主線程中執行資料庫操作
                                val product = productDBManager.getProductByMagNo(sentCouponItem)

                                // 將商品加入購物車 (必須不包含在已知清單內)
                                if (product != null && !cartList.map { it.productItem }.contains(product)) {
                                    withContext(Dispatchers.Main) {
                                        //確認優惠券能否放入清單
                                        if(couponAddCheck(product)){
                                            cartList.add(CartItem(Cno,product,-1,0.00))
                                            Toast.makeText(requireContext(),"新增折價券 ${product.pName}",Toast.LENGTH_SHORT).show()
                                        }else{
                                            //錯誤回報在couponAddCheck就會跳出提示，這邊用Log說明新增出現異常即可
                                            Log.d("折價券新增狀況","未滿足折價券使用條件")
                                        }
                                        //主程序外更新顯示內容
                                        loadFilterProduct()
                                    }
                                }
                            }

                            Log.d("優惠券檢查", "已經更新")
                        }
                    })

                    simpleBottomDialog.show()
                }
            }
        }

        //折扣按鈕(修改購物車 折扣選項 : 單向折扣)
        binding.btnSingleCharge.setOnClickListener {
            //單向折扣只會變更最後一項的價格

            //開啟alertDialog讓收銀員輸入人工折扣
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.entertext, null)
            val alertDialogBuilder = AlertDialog.Builder(requireContext())

            alertDialogBuilder.setView(dialogView)

            alertDialogBuilder.apply {
                //顯示折數
                val myEditText = dialogView.findViewById<EditText>(R.id.edtEnterTxtContent)
                //顯示標題
                val customTitle = dialogView.findViewById<TextView>(R.id.txtEnterTxtTitle)

                customTitle.text="請輸入折數"

                setPositiveButton("確認"){_, _ ->
                    val discountText = myEditText.text.toString()

                    if (cartList.size>=1) {
                        if (myEditText.text.isEmpty()){
                            Toast.makeText(requireContext(),"折數不能為空",Toast.LENGTH_SHORT).show()
                        }else{
                            if (discountText.toIntOrNull() == null) //確認是否為整數型
                                Toast.makeText(requireContext(),"輸入內容必須為整數",Toast.LENGTH_SHORT).show()
                            else{ //確認是否為0-100之間
                                if (discountText.toIntOrNull()!! > 100 || discountText.toIntOrNull()!! < 0 ){
                                    Toast.makeText(requireContext(),"輸入內容必須在0-100之間",Toast.LENGTH_SHORT).show()
                                }else{
                                    val discountValue = discountText.toDouble() / 100 //折數轉換為小數型
                                    val lastProduct = cartList.last()

                                    //購物車項目不允許重複折扣
                                    if (lastProduct.discountS.toInt()!= 0 || lastProduct.discountT.toInt()!=0){
                                        Toast.makeText(requireContext(),"不可重複折扣",Toast.LENGTH_SHORT).show()
                                    }else{
                                        //確認是否為會員
                                        if (nowLoginMember!=null){
                                            //變更購物車最後一項的折扣金額
                                            val lastPrc = (lastProduct.productItem.memPrc * lastProduct.quantity * discountValue).roundToInt()
                                            lastProduct.discountS = lastPrc - lastProduct.productItem.memPrc * lastProduct.quantity
                                        }
                                        else{
                                            //變更購物車最後一項的折扣金額
                                            val lastPrc = (lastProduct.productItem.unitPrc * lastProduct.quantity * discountValue).roundToInt()
                                            lastProduct.discountS = lastPrc - lastProduct.productItem.unitPrc * lastProduct.quantity
                                        }

                                        loadFilterProduct()
                                    }
                                }
                            }
                        }
                    }else{
                        Toast.makeText(requireContext(),"購物車尚未加入商品",Toast.LENGTH_SHORT).show()
                    }

                }

                setNegativeButton("取消"){dialog, _ ->
                    dialog.dismiss()
                }
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        //現折按鈕(用戶輸入金額進行折價: 現金折讓)
        binding.btnCharge.setOnClickListener {
            //單向折扣只會變更最後一項的價格

            //開啟alertDialog讓收銀員輸入人工折扣
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.entertext, null)
            val alertDialogBuilder = AlertDialog.Builder(requireContext())

            alertDialogBuilder.setView(dialogView)

            alertDialogBuilder.apply {
                //顯示折數
                val myEditText = dialogView.findViewById<EditText>(R.id.edtEnterTxtContent)
                //顯示標題
                val customTitle = dialogView.findViewById<TextView>(R.id.txtEnterTxtTitle)

                customTitle.text="請輸入折讓金額"

                setPositiveButton("確認"){_, _ ->
                    val discountText = myEditText.text.toString()

                    if (cartList.size>=1) {
                        if (myEditText.text.isEmpty()){
                            Toast.makeText(requireContext(),"折數不能為空",Toast.LENGTH_SHORT).show()
                        }else{
                            val discountValue = discountText.toIntOrNull()    //輸入金額轉換為整數型
                            val lastProduct = cartList.last()

                            if (discountValue == null) //確認是否為整數型
                                Toast.makeText(requireContext(),"輸入內容必須為整數",Toast.LENGTH_SHORT).show()
                            else{
                                //購物車單向總價
                                val singlePrcSum = if (nowLoginMember!=null){
                                    lastProduct.productItem.memPrc * lastProduct.quantity - lastProduct.discountS
                                }else{
                                    lastProduct.productItem.unitPrc * lastProduct.quantity - lastProduct.discountS
                                }
                                //購物車項目不允許重複折扣
                                if (lastProduct.discountS.toInt()!= 0 || lastProduct.discountT.toInt()!=0){
                                    Toast.makeText(requireContext(),"不可重複折讓",Toast.LENGTH_SHORT).show()
                                }else{
                                    //輸入折讓必須小於商品總價
                                    if (discountValue >= singlePrcSum){
                                        Toast.makeText(requireContext(),"折讓金額過大",Toast.LENGTH_SHORT).show()
                                    }else{
                                        //確認是否為會員
                                        if (nowLoginMember!=null){
                                            //變更購物車最後一項的折扣金額
                                            lastProduct.discountS = -discountValue.toDouble()
                                        }
                                        else{
                                            //變更購物車最後一項的折扣金額
                                            lastProduct.discountS = -discountValue.toDouble()
                                        }

                                        loadFilterProduct()
                                    }
                                }
                            }
                        }
                    }else{
                        Toast.makeText(requireContext(),"購物車尚未加入商品",Toast.LENGTH_SHORT).show()
                    }

                }

                setNegativeButton("取消"){dialog, _ ->
                    dialog.dismiss()
                }
            }

            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }


        //全折按鈕(小計折扣: 全部都算，不過已經小計折扣過的項目不納入考慮)
        binding.btnMultipleCharge.setOnClickListener {
            //平均更新給目前所有的折扣項目，並且依照數量比例進行分攤

            if (cartList.isNotEmpty()){
                lifecycleScope.launch(Dispatchers.IO) {
                    val sumT = productDBManager.getProductByID("00")
                    Cno+=1  //購物車序號+1
                    if (sumT!=null){    //存在全折商品物件 (虛構商品)， 不包含在實際購物清單
                        lifecycleScope.launch( Dispatchers.Main) {  //切到主線呈加入商品
                            //開啟alertDialog讓收銀員輸入人工折扣
                            val alertDialogBuilder = AlertDialog.Builder(requireContext())
                            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.entertext, null)
                            alertDialogBuilder.setView(dialogView)

                            //顯示折數
                            val myEditText = dialogView.findViewById<EditText>(R.id.edtEnterTxtContent)
                            //顯示標題
                            val customTitle = dialogView.findViewById<TextView>(R.id.txtEnterTxtTitle)
                            customTitle.text = "請輸入全折數"

                            alertDialogBuilder.setPositiveButton("確定") { _, _ ->
                                val discountText = myEditText.text.toString()

                                val copyCartList = mutableListOf<CartItem>()

                                //由最後一項開始檢查是否有 全折項目，如果有就計算全折項目以後的
                                for (i in cartList.size-1 downTo 0){
                                    val item = cartList[i]

                                    if (item.productItem == sumT){  //找到全折項目就停止
                                        break
                                    }else{
                                        copyCartList.add(item)
                                    }
                                }
                                //取得反轉的購物車檢查清單後反轉
                                copyCartList.reverse()

                                if (copyCartList.isNotEmpty()){         //確認進行折扣的清單是否為空
                                    if (discountText.isNotEmpty()) {    //確認輸入的折數是否合理
                                        // 確保discountText是數值
                                        val discountValue = discountText.toIntOrNull()?: 0
                                        if (discountValue<=100) {   //允許輸入折扣最大百分比
                                            //計算copyCartList的總價
                                            val copySum: MutableList<Double> = mutableListOf()
                                            for (i in copyCartList){
                                                if (nowLoginMember!=null){
                                                    copySum.add((i.quantity * i.productItem.memPrc + i.discountS).roundToInt().toDouble())
                                                } else{
                                                    copySum.add((i.quantity * i.productItem.unitPrc + i.discountS).roundToInt().toDouble())
                                                }
                                            }

                                            //依照比例分攤全折
                                            var copyIndex = copySum.size-1
                                            for (i in cartList.size-1 downTo 0){
                                                val item = cartList[i]

                                                if (item.productItem == sumT){  //找到全折項目就停止
                                                    break
                                                }else{
                                                    cartList[i].discountT = copySum[copyIndex] /copySum.sum() * (copySum.sum() * (discountValue) / 100.00 - copySum.sum())
                                                    copyIndex--
                                                }
                                            }

                                            // 根據輸入的折數更新cartList
                                            cartList.add(CartItem(Cno, sumT, 0, 0.0, copySum.sum() * (discountValue) / 100.00 - copySum.sum()))
                                            // 加載更新後的購物車
                                            loadFilterProduct()
                                        }else {
                                            Toast.makeText(requireContext(), "請輸入有效的折扣數值", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }else{
                                    Toast.makeText(requireContext(),"沒有可進行小計折扣的項目",Toast.LENGTH_SHORT).show()
                                }
                            }

                            alertDialogBuilder.setNegativeButton("取消") { dialog, _ ->
                                dialog.dismiss()
                            }

                            val alertDialog = alertDialogBuilder.create()
                            alertDialog.show()
                        }
                    }
                    else{
                        Log.d("不合規的商品","dfs")
                    }
                }
                loadFilterProduct()
            }else{
                Toast.makeText(requireContext(),"還沒有加入任何商品",Toast.LENGTH_SHORT).show()
            }
        }


        //按鈕清除輸入貨號(送出購物車)
        binding.btnClear.setOnClickListener {
            binding.edtSearchRow.setText("")
        }

        //按下小計按鈕後送出
        binding.btnDeal.setOnClickListener {
            if (totalSumPro < 0){
                Toast.makeText(requireContext(),"送出金額不可為負",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),"送出金額 ${(totalSumPro).roundToInt()}",Toast.LENGTH_SHORT).show()
            }

            //目前不允許送出0元發票
            if (cartList.isNotEmpty() && totalSumPro >= 0) {
                //送出合併後的購物車
                val intent = Intent(requireContext(), Payment::class.java)
                intent.putExtra("cartList_key", cartList as? Serializable)

                intent.putExtra("now_member",nowLoginMember as? Serializable)
                intent.putExtra("total_price",totalSumPro)
                startActivity(intent)
            }
        }

        return root
    }

    //進行更正作業
    private fun merchantClearProcess() {
        if (cartList.isNotEmpty()){
            // 安全鎖
            var safeDeleteCheck = true
            // 先用複製的清單確認刪除最後一項是否會導致關聯遭到破壞
            val copyFilter: MutableList<Product> = ArrayList(cartList.map { it.productItem }) // 使用複製的清單進行檢查
            val lastItem = copyFilter[copyFilter.size-1]    // 前先儲存最後一項

            copyFilter.removeAt(copyFilter.size-1)    // 刪除最後一項

            // 創建一個新的協程，並在其中執行異步操作
            lifecycleScope.launch(Dispatchers.IO) {
                // 確認點案的項目是否會影響 折價券關係
                if (!couponDeleteCheck(lastItem,copyFilter)) { // 檢查刪除後商品是否會破壞關聯
                    Log.d("警告", "刪除商品 ${lastItem.pName} 可能會破壞折價券關聯，請先刪除折價券")
                    safeDeleteCheck = false
                }
                // 如果所有檢查都通過，執行刪除操作
                if (safeDeleteCheck) {
                    //刪除最後一項購物車物件

                    //刪除前先將記錄保存更正紀錄
                    Dno+=1  //更新更正清單項次
                    deleteCartList.add(DeletedCartItem(Dno,cartList.last()))
                    println("已經保存更正紀錄到清單")

                    for (i in deleteCartList){
                        println("第${i.deleteOrder}筆刪除內容: ${i.cartItem}")
                    }

                    //更新購物車清單內容
                    Cno-=1
                    cartList.remove(cartList.last())    //刪除最後一項
                    println("刪除購物車物件成功")

                    for (i in cartList){
                        println("目前商品貨號: ${i.productItem.pluMagNo} 數量: ${i.quantity}")
                    }

                    //如果是全折物件被刪除，需要清除依照比例分攤的折扣額(discT)，會持續走訪直到遇見全折物件或是整個購物車 (刪除全折物件後再進行此步驟) ex: m1 m2 t1 m3 m4 t2(刪除t2 後連帶重製m3 m4 遇到t1 停止)
                    if(lastItem.pluMagNo == "0000000"){ //全折物件
                        for (i in cartList.size-1 downTo 0){
                            if (cartList[i].productItem.pluMagNo == "0000000")   //遇到全折物件就停止
                                break
                            else{
                                cartList[i].discountT = 0.0 //重新初始化折扣比例
                                println("初始化 ${cartList[i].productItem.pName}")
                            }
                        }
                    }

                    // 使用主執行緒進行UI操作
                    withContext(Dispatchers.Main) {
                        //詢問用alertDialog詢問是否要進行更正作業
                        val deleteCheckDialogBuilder = AlertDialog.Builder(requireContext())
                        deleteCheckDialogBuilder.setTitle("更正作業")
                        deleteCheckDialogBuilder.setMessage("請問您確定要刪除 ${lastItem.pName} 嗎?")
                        //確定更正
                        deleteCheckDialogBuilder.setPositiveButton("確定") { _, _ ->
                            // 刪除掃描商品提示訊息(商品名稱)
                            Toast.makeText(requireContext(), "刪除: ${lastItem.pName}", Toast.LENGTH_SHORT).show()

                            //重新載入清單
                            loadFilterProduct()
                        }
                        //取消更正
                        deleteCheckDialogBuilder.setNegativeButton("取消") { _, _ ->
                        }

                        //顯示alertDialog
                        val alertDialog = deleteCheckDialogBuilder.create()
                        alertDialog.show()
                    }
                }
            }
        } else {
            Toast.makeText(requireContext(),"購物車為空，無法進行更正",Toast.LENGTH_SHORT).show()
        }
    }

    //確認優惠券能否放入清單(必須超過總價)       //input: 折價券
    private suspend fun couponAddCheck(addCoupon: Product): Boolean {

        val cartProductList = cartList.map { it.productItem }   //目前的購物車商品集合

        val selectItem = withContext(Dispatchers.IO) {
            productDBManager.getCouponMainByPluMagNo(addCoupon.pluMagNo) as CouponMain //確認所選項目是否為折價券
        }

        val singlePrc: Double = if (nowLoginMember != null){
            if (addCoupon.memPrc < addCoupon.unitPrc){
                addCoupon.memPrc
            }else{
                addCoupon.unitPrc
            }
        }else{
            addCoupon.unitPrc
        }    //產品單價(較低的)

        //這邊抓coupon Main對應的所有coupon Detail 商品項目去跟 filterList 比對(多筆的SEQ_NO都放入清單: 不同的where 條件)

        //已知 pluType='75' 類別
        return when (selectItem.DISC_TYPE) {
            "0" -> { //折扣券
                //看是否有 明細檔 (正確分類)
                var subCheck = 0    //折扣券檢核碼 (99:許可)

                val result = lifecycleScope.async(Dispatchers.IO) {
                    val detail = productDBManager.getCouponDetailBypluMagNo(selectItem.DISC_PLU_MagNo)  //多筆明細檔

                    if (detail != null) {
                        val detailLength = detail.size  //幾個 明細檔
                        val cycleCheckMutableList = mutableListOf<Product>()  //排除型折價券檢集合
                        Log.d("折價券","有 ${detailLength}個規則分支")   //確認明細數量，要每張都確認曾能回報

                        if (selectItem.BASE_TYPE == "1"){   //計算其中一項符合的情況
                            for (detailItem in detail){     //逐一確認明細檔(所有項目都符合才會許可 重點是有幾張券，每張都要符合)
                                Log.d("折價券細項","序列號: ${detailItem.SEQ_NO}")
                                //確認折價券是否在期限內
                                if (!isCouponValid(detailItem)){
                                    Log.d("折價券適用間檢查","超過折價券期間")
                                    subCheck = 6
                                    break
                                }else{
                                    //符合期限後進行更詳細的檢查
                                    val matchPLU = detailItem.PLU_MagNo == null || cartProductList.any { it.pluMagNo == detailItem.PLU_MagNo }  //檢查明細檔
                                    val matchDEP = detailItem.DEP_No == null || cartProductList.any { it.DEP_No == detailItem.DEP_No }
                                    val matchCAT = detailItem.CAT_No == null || cartProductList.any { it.CAT_No == detailItem.CAT_No }
                                    val matchVEN = detailItem.VEN_No == null || cartProductList.any { it.VEN_No == detailItem.VEN_No }

                                    //確認一般檢核碼
                                    subCheck = if (matchPLU && matchDEP && matchCAT && matchVEN){   //與折價券明細黨完全一致的情況
                                        Log.d("檢查結果","條件A $matchPLU,條件A $matchDEP,條件A $matchCAT,條件A $matchVEN,")
                                        99
                                    }else{      //每個商品沒有比對的狀況都不同 (類別、廠牌...，這邊會記錄)

                                        if (!matchPLU){
                                            1
                                        }else if (!matchDEP){
                                            2
                                        }else if (!matchCAT){
                                            3
                                        }else if (!matchVEN){
                                            4
                                        }else{
                                            5
                                        }
                                    }
                                }

                                //找到符合的就break (A) ，選擇的券號 其中一項 明細檔符合 就停止比對
                                if (subCheck == 99){
                                    break
                                }
                            }
                        }else if(selectItem.BASE_TYPE == "2"){
                            //先將所有折價券的明細檔合併為一個集合
                            val allProductList  = productDBManager.getAllProductTable()

                            for (detailItem in detail){ //走訪所有折價券明細檔
                                Log.d("折價券細項","序列號: ${detailItem.SEQ_NO}")

                                //折價券已過期
                                if (!isCouponValid(detailItem)){
                                    Log.d("折價券適用間檢查","超過折價券期間")
                                    subCheck = 6
                                    break
                                }

                                if (allProductList!=null){
                                    val matchedProducts = allProductList.filter { product ->    //單一折價券明細的集合
                                        val matchPLU = detailItem.PLU_MagNo == null || product.pluMagNo == detailItem.PLU_MagNo
                                        val matchDEP = detailItem.DEP_No == null || product.DEP_No == detailItem.DEP_No
                                        val matchCAT = detailItem.CAT_No == null || product.CAT_No == detailItem.CAT_No
                                        val matchVEN = detailItem.VEN_No == null || product.VEN_No == detailItem.VEN_No

                                        matchPLU && matchDEP && matchCAT && matchVEN
                                    }

                                    Log.d("折價券品項","品項: $matchedProducts")

                                    // 將符合排除型折價券的商品加入清單中
                                    for (i in matchedProducts){
                                        cycleCheckMutableList.add(i)
                                    }
                                }
                            }

                            //未超過折價券試用期間
                            if (subCheck != 6){
                                val exclusiveList = cartProductList.subtract(   //去除掉排除項目之後的集合
                                    cycleCheckMutableList.toSet()
                                )

                                //檢查是否只有排除項目
                                if (exclusiveList.isEmpty()) {
                                    subCheck = 7
                                    println("列表中只有排除項項目存在")
                                } else {
                                    subCheck = 99
                                    println("列表中有排除項以外的項目")
                                }
                            }
                        }
                    }

                    // 將等待協程結果返回
                    subCheck
                }

                //回報檢核碼
                withContext(Dispatchers.Main){
                    //回傳檢核碼
                    when (result.await()){
                        1 -> {
                            Toast.makeText(requireContext(),"不符合折價券需求的貨號",Toast.LENGTH_SHORT).show()
                        }2 ->{
                        Toast.makeText(requireContext(),"不符合折價券需求的部門",Toast.LENGTH_SHORT).show()
                    }3 ->{
                        Toast.makeText(requireContext(),"不符合折價券需求的分類",Toast.LENGTH_SHORT).show()
                    }4 ->{
                        Toast.makeText(requireContext(),"不符合折價券需求的廠商",Toast.LENGTH_SHORT).show()
                    }5 ->{
                        Toast.makeText(requireContext(),"折價券需求的異常",Toast.LENGTH_SHORT).show()
                    }6 ->{
                        Toast.makeText(requireContext(),"超過折價券期限",Toast.LENGTH_SHORT).show()
                    }7 -> {
                        Toast.makeText(requireContext(),"不符合折價券使用規範(僅有排除類商品)",Toast.LENGTH_SHORT).show()
                    }99 -> {
                        Toast.makeText(requireContext(),"成功新增折價券",Toast.LENGTH_SHORT).show()
                    }else ->{
                        Toast.makeText(requireContext(),"不合規的檢核碼(A)",Toast.LENGTH_SHORT).show()
                    }
                    }

                    // 等待協程結果( 依照類別或是排除決定
                    when (selectItem.BASE_TYPE) {
                        "1" -> {        //折價券主檔類別為1 其中一項符合即可 (出現一個就可以)
                            if (result.await() == 99){
                                true
                            }else{
                                false
                            }
                        }
                        "2" -> {        //折價券主檔類別為2 必須所有都不符合 (出現條件外的就可以用)
                            if (result.await() == 99){
                                true
                            }else{
                                false
                            }
                        }
                        else -> {
                            true
                        }
                    }
                }
            }
            "1" -> {   //打折券
                //如果折價券金額小於總價則許可加入
                totalSumUnitPrice >= singlePrc
            }
            else -> {
                //非折價券類型的商品可直接加入
                true
            }
        }
    }

    //確認優惠券能否移出清單(必須新移除對應的優惠券)
    private fun couponDeleteCheck(deleteItem: Product, copyFilter: MutableList<Product>): Boolean {
        // 檢查刪除項目是否為折價券，如果是直接返回 true
        if (deleteItem.pluType == "75") {
            Log.d("類型", "折價券")
            return true
        }

        //折價券比較狀況
        val cycleCheckMutableList = mutableListOf<Boolean>()  //比對的折價券是否符合狀況

        //輪流檢查每一個折價券是否有對應的項目(是否有不滿足條件的折價券)
        for (item in cartList.map { it.productItem }){      //走訪商品項次清單
            if (item.pluType == "75"){          //折價券商品
                //確認該折價券的主檔和明細檔
                val couponMain = productDBManager.getCouponMainByPluMagNo(item.pluMagNo)
                val couponDetail = productDBManager.getCouponDetailBypluMagNo(item.pluMagNo)

                //確保折價券主檔和明細檔都存在
                //確認是正向還是負向表列
                if (couponMain != null && couponDetail != null) {
                    when(couponMain.BASE_TYPE){
                        "1" ->{
                            //冰淇淋折價券 對不到 冰淇淋/null 就會跳出警告
                            var detailCheck = false
                            //逐一檢查每一個折價券明細
                            for (detail in couponDetail){
                                //確認刪除商品後 是否會造成不滿足折價券的狀況
                                // 正向表列只要其中一項符合即可

                                //符合期限後進行更詳細的檢查
                                val matchPLU = detail.PLU_MagNo == null || copyFilter.any { it.pluMagNo == detail.PLU_MagNo }  //檢查明細檔
                                val matchDEP = detail.DEP_No == null || copyFilter.any { it.DEP_No == detail.DEP_No }
                                val matchCAT = detail.CAT_No == null || copyFilter.any { it.CAT_No == detail.CAT_No }
                                val matchVEN = detail.VEN_No == null || copyFilter.any { it.VEN_No == detail.VEN_No }

                                //對應到符合的商品項目
                                if (matchPLU && matchDEP && matchCAT && matchVEN){   //與折價券明細黨完全一致的情況
                                    cycleCheckMutableList.add(true)                  //其中一項符合即可 (該折價券主檔檢查完畢)
                                    detailCheck = true
                                }
                            }

                            //檢查完都沒有符合的項目
                            if (!detailCheck){
                                cycleCheckMutableList.add(false)    //指定為不許可 (沒有對應的折價券)
                            }
                        }
                        "2" ->{
                            //菸酒類排除型折價券 對到只有菸酒類 會跳出警告

                            //先將所有折價券的明細檔合併為一個集合
                            val allProductList  = productDBManager.getAllProductTable() //所有商品
                            val exclusiveList = mutableListOf<Product>()

                            for (detail in couponDetail){ //走訪所有折價券明細檔
                                if (allProductList!=null){
                                    val matchedProducts = allProductList.filter { product ->    //單一折價券明細的集合
                                        val matchPLU = detail.PLU_MagNo == null || product.pluMagNo == detail.PLU_MagNo
                                        val matchDEP = detail.DEP_No == null || product.DEP_No == detail.DEP_No
                                        val matchCAT = detail.CAT_No == null || product.CAT_No == detail.CAT_No
                                        val matchVEN = detail.VEN_No == null || product.VEN_No == detail.VEN_No

                                        matchPLU && matchDEP && matchCAT && matchVEN
                                    }

                                    Log.d("折價券品項","品項: $matchedProducts")

                                    // 將符合排除型折價券的商品加入清單中
                                    for (i in matchedProducts){
                                        exclusiveList.add(i)    //將符合明細檔的商品內容加入清單，形成主黨對應的明細檔商品集
                                    }
                                }
                            }

                            //去除掉 互斥型商品 後的購物清單內容
                            var copyFilterMinus = copyFilter.subtract(   //移除掉deleteItem後的商品集 再去除 排除型明細檔商品集 後的結果
                                exclusiveList.toSet()
                            )

                            //去除掉折價券類型的商品(去除其他折價券的干擾項)
                            copyFilterMinus = copyFilterMinus.filter { it.pluType != "75" }.toSet()

                            //檢查是否只有排除項目
                            if (copyFilterMinus.isEmpty()) {
                                //忽視掉 折價券 類型商品
                                cycleCheckMutableList.add(false)    //指定為不許可 (只存在不適用類別的商品，不能用此折價券)
                            }else{
                                Log.d("清除互斥商品後紀錄",copyFilterMinus.toString())
                            }
                        }
                        else ->{
                            cycleCheckMutableList.add(false)    //正向、負向以外的類型 不許可
                        }
                    }
                }else{  //沒有明細檔的狀況
                    cycleCheckMutableList.add(true)     //許可(不用檢查明細檔)
                }
            }
        }

        //全部都是true就可以加入
        Log.d("明細檔檢查狀況",cycleCheckMutableList.toString())

        //回報檢查結果
        return if (cycleCheckMutableList.contains(false)){
            false    //有一個檢查錯誤就不許可進行更正(要求先刪除折價券)
        } else{
            true
        }
    }

    // 檢查現在的時間是否在折價券適用期間內
    private fun isCouponValid(coupon: CouponDetail): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(coupon.FROM_DATE) && now.isBefore(coupon.TO_DATE)
    }

    //重新載入購物車畫面
    @SuppressLint("SetTextI18n")
    private fun loadFilterProduct() {
        //先重製總價
        totalSumUnitPrice = 0.00

        if (isFirstCreation){
            Log.d("會空喔",cartList.map{ it.productItem.pName }.toString())

            //目前只要離開頁面就清除項目，不要紀錄
            //以下是因應螢幕旋轉，以viewmodel紀錄內容，避免生命週期重建導致資料流失(保留、清空資料會較麻煩)

            // 從 ViewModel 中讀取 filteredProductList
            //val productList = viewModel.filteredProductList.value
//            if (!productList.isNullOrEmpty()) {
//                val adapter = FilterProductAdapter(productList, selectedQuantities)
//                binding.grTableProduct.adapter = adapter
//                binding.grTableProduct.numColumns = 1
//                adapter.notifyDataSetChanged()
//            }
//            isFirstCreation = false
        }else{
            val adapter: FilterProductAdapter = if (nowLoginMember!=null){      //有會員登入
                FilterProductAdapter(cartList,true)
            }else{
                FilterProductAdapter(cartList,false)
            }

            lifecycleScope.launch(Dispatchers.Main) {
                binding.grTableProduct.adapter = adapter
                binding.grTableProduct.numColumns = 1
                adapter.notifyDataSetChanged()
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            //變更小計金額
            for (item in cartList) {
                //計算單項小計
                val totalPrice: Int = if(nowLoginMember != null){    //確認是否為會員
                    //防止會員價比折扣價還高的狀況
                    if (item.productItem.memPrc > item.productItem.unitPrc){
                        (item.productItem.unitPrc * item.quantity + item.discountS).roundToInt()   //適用較低的價格
                    }else{
                        (item.productItem.memPrc * item.quantity + item.discountS).roundToInt()
                    }
                }else{
                    (item.productItem.unitPrc * item.quantity + item.discountS).roundToInt()
                }

                println("單項小計 $totalPrice")

                totalSumUnitPrice += totalPrice
            }

            println("總金額(未全折): $totalSumUnitPrice")

            //確認是否有全折項目
            totalSumPro = totalSumUnitPrice
            for (i in cartList){
                if (i.productItem.pluMagNo != "0000000"){   //跳過加總顯示項目，避免重複計算
                    totalSumPro += i.discountT.roundToInt()

                    println("全折項目 ${i.discountT}")
                }
            }

            println("總金額(已全折): ${totalSumPro.roundToInt()}")


            //當總小計大於0時，顯示總小計
            if (totalSumPro>0){
                binding.txtTotalDollar.visibility = View.VISIBLE    //開啟可視化
                binding.txtTotalDollar.text = "總計: ${totalSumPro.toInt()} 元" //顯示金額4捨5入
            }else{
                binding.txtTotalDollar.visibility = View.INVISIBLE   //金額小於0不可視
            }
        }
    }

//    //確認掃描結果能否加入購物車存在對應商品
    private fun productScanCheck(productMagno: String) {
        val cartProductItem = cartList.map { it.productItem }   //目前購物車所有商品集合
        var errorHintCode = 0   //錯誤代碼 (0: 掃描取消,1: 重複商品, 2: 不存在商品)

        //確認Dao商品是否包含掃描項目
        lifecycleScope.launch {
            val product = withContext(Dispatchers.IO) {
                productDBManager.getProductByMagNo(productMagno)
            }
            if (product != null) {
                Log.d("存在對應商品", product.pName)
                // 檢查是否存在相同商品
                if (!cartProductItem.contains(product)) {

                    //確認是否為特殊條件才能加入的商品(像是折價券...)
                    if (product.pluType == "75"){       //折價券商品
                        //確認是否可加入折價券商品
                        if (couponAddCheck(product)){       //確認折價券能否加入清單
                            //將掃描結果加入購物車物件
                            Cno+=1  //更新購物車項次
                            cartList.add(CartItem(Cno,product,-1))    //項次+1

                            Log.d("加入商品", cartList.last().productItem.pName)

//                            // 如果商品已經存在，數量指定為-1
                            existItemCheck = true
                        }else{
                            errorHintCode = 3   //未達折價券指定金額
                            Log.d("未達折價券指定金額","not enough total price for this coupon")
                            existItemCheck = false
                        }
                    }else{          //非折價券商品
                        //將掃描結果加入購物車物件
                        Cno+=1  //更新購物車項次
                        cartList.add(CartItem(Cno,product,1,0.00))    //項次+1

                        Log.d("加入商品", cartList.last().productItem.pName)
                        existItemCheck = true
                    }

                } else {
                    errorHintCode = 1   //重複商品
                    Log.d("商品已存在於清單中","exist product")

                    Cno+=1
                    cartList.add(CartItem(Cno,product,1,0.00))    //項次+1 (允許重複商品加入)

                    existItemCheck = true  //如果允許重複加入，這邊更改為true
                }
            } else {
                errorHintCode = 2   //不存在商品
                Log.d("不存在此商品", "not exist product")
                existItemCheck = false
            }

            //前台顯示的購物車清單
            withContext(Dispatchers.Main) {
                //變更GridView顯示項目
                loadFilterProduct()

                if (existItemCheck) {
                    Toast.makeText(requireContext(), "加入商品: ${cartList.last().productItem.pName}", Toast.LENGTH_SHORT).show()
                } else {
                    when (errorHintCode) {
                        0 -> Toast.makeText(requireContext(), "掃描取消", Toast.LENGTH_SHORT).show()
                        1 -> Toast.makeText(requireContext(), "商品已存在於清單中", Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(requireContext(), "不存在此商品", Toast.LENGTH_SHORT).show()
                        3 -> Toast.makeText(requireContext(), "未達折價券指定金額", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //開啟鏡頭掃描商品
    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("請對準條碼進行掃描")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.setRequestCode(REQUEST_CODE_SCAN)

        //啟動掃描頁
        barcodeScannerLauncher.launch(integrator.createScanIntent())
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

    //離開頁面的處理
    override fun onDestroyView() {
        super.onDestroyView()

        //離開頁面就清除 購物車清單
        cartList.clear()

        _binding = null
    }
}