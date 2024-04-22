package com.example.alpha2.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ImageFormat
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
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
import com.example.alpha2.DBManager.Product.CouponDetail
import com.example.alpha2.DBManager.Product.CouponMain
import com.example.alpha2.R
import com.example.alpha2.myAdapter.CouponAdapter
import com.example.alpha2.myAdapter.FilterProductAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.time.LocalDateTime

//不允許螢幕旋轉，螢幕旋轉容易導致資料流失

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)
    private lateinit var productDBManager: ProductManager //商品Dao
    private lateinit var memberDBManager: MemberManager   //會員Dao

    private var productCategoryList: MutableList<String> = mutableListOf("食物", "飲料")   //商品類別(只會顯示常用商品，或是沒有條碼可掃描的商品)

    //List儲存商品篩選結果(依據文字搜尋或欄位搜尋結果)
    private var filteredProductList: MutableList<Product> = mutableListOf()

    private val REQUEST_CODE_SCAN = 1002 // 新增這行，定義掃描請求碼

    private var existItemCheck = false      //檢查掃描商品是否存在於Dao

    //activity首次創建(初次開啟或是螢幕旋轉)
    private var isFirstCreation = true

    // 定義一個映射來存儲商品和它們的選擇數量
    private var selectedQuantities = mutableMapOf<Product, Int>()

    //總小計金額
    private var totalSumUnitPrice = 0

    //目前會員
    private var nowLoginMember: Member? = null

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
                            binding.txtMemberClass.text = "會員: ${scanMember.cardNo}"

                            Log.d("取得用戶資料",nowLoginMember.toString())

                            stateHintCode = 0
                        } else {
                            nowLoginMember = null       //重置會員名稱
                            binding.txtMemberClass.text = "非會員"
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

                var errorHintCode = 0   //錯誤代碼 (0: 掃描取消,1: 重複商品, 2: 不存在商品)
                if (scanResult.contents == null) {
                    Toast.makeText(requireContext(), "掃描取消", Toast.LENGTH_LONG).show()
                } else {
                    //確認Dao商品是否包含掃描項目
                    lifecycleScope.launch {
                        val product = withContext(Dispatchers.IO) {
                            productDBManager.getProductByMagNo(scanResult.contents)
                        }
                        if (product != null) {
                            Log.d("存在對應商品", product.pName)
                            // 檢查是否存在相同商品
                            if (!filteredProductList.contains(product)) {

                                //確認是否為特殊條件才能加入的商品(像是折價券...)
                                if (product.pluType == "75"){       //折價券商品

                                    //確認是否可加入折價券商品
                                    if (couponAddCheck(product)){       //確認折價券能否加入清單
                                        //將掃描到的商品加入列表中
                                        filteredProductList.add(product)

                                        Log.d("加入商品", filteredProductList.last().pName)

                                        // 如果商品已經存在，數量指定為-1
                                        selectedQuantities[product] = -1

                                        existItemCheck = true
                                    }else{
                                        errorHintCode = 3   //未達折價券指定金額
                                        Log.d("未達折價券指定金額","not enough total price for this coupon")
                                        existItemCheck = false
                                    }

                                }else{
                                    //將掃描到的商品加入列表中
                                    filteredProductList.add(product)

                                    Log.d("加入商品", filteredProductList.last().pName)

                                    // 如果商品已經存在，數量指定為1
                                    selectedQuantities[product] = 1

                                    existItemCheck = true
                                }

                            } else {
                                errorHintCode = 1   //重複商品
                                Log.d("商品已存在於清單中","exist product")

                                existItemCheck = false
                            }
                        } else {
                            errorHintCode = 2   //不存在商品
                            Log.d("不存在此商品", "not exist product")
                            existItemCheck = false
                        }

                        // 生成只包含 pName 的列表
                        val productNameList = filteredProductList.map { it.pName }

                        withContext(Dispatchers.Main) {
                            //變更GridView顯示項目
                            loadFilterProduct()

                            if (existItemCheck) {
                                Toast.makeText(requireContext(), "加入商品: ${filteredProductList.last().pName}", Toast.LENGTH_SHORT).show()
                                Log.d("商品清單", productNameList.toString())
                            } else {
                                when (errorHintCode) {
                                    0 -> Toast.makeText(requireContext(), "掃描取消", Toast.LENGTH_SHORT).show()
                                    1 -> Toast.makeText(requireContext(), "商品已存在於清單中", Toast.LENGTH_SHORT).show()
                                    2 -> Toast.makeText(requireContext(), "不存在此商品", Toast.LENGTH_SHORT).show()
                                    3 -> Toast.makeText(requireContext(), "未達折價券指定金額", Toast.LENGTH_SHORT).show()
                                }
                                Log.d("商品清單", productNameList.toString())
                            }
                        }
                    }
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
        val userID = sharedPreferences.getString("userId", "")

        //確認用戶身分
        if (userID!=null){
            val accessUser = userDBManager.getUserById(userID)
            if (accessUser != null) {
                binding.textDashboard.text = "收銀員: ${accessUser.name}"
            }
        }else{
            binding.textDashboard.text = "Null user access"
        }

        //變更GridView顯示項目
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

        //點擊gridView變更數量
        binding.grTableProduct.setOnItemClickListener { parent, _, position, _ ->
            //Toast.makeText(requireContext(),"點按項目: ${parent.getItemAtPosition(position)}",Toast.LENGTH_SHORT).show()

            //目前點按項目
            val clickItem = parent.getItemAtPosition(position) as Product //進行強制轉型確認商品類別

            // 顯示 BottomView
            bottomSheetDialog.show()

            if (btnPlus1 != null && btnMinus1 != null && btnConfirm!=null) {
                //點擊商品的目前數量
                val selectScanNumber = selectedQuantities[filteredProductList[position]]
                //點擊數量(如果是折價券就變成負金額)
                var changeAmount= selectScanNumber
                if (clickItem.pluType == "75"){
                    if (changeAmount != null) {
                        changeAmount *= (-1)
                    }
                }

                //顯示點數量
                if (edtNumber != null && changeAmount != null) {
                    //如果是折價券商品就變成負金額
                    edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                }

                // 添加文字變更監聽器
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
                    if (changeAmount!=null)
                        changeAmount = changeAmount!! + 1       //點擊數量+1
                    Log.d("數量","$changeAmount")

                    //顯示點擊數量
                    if (edtNumber != null && changeAmount != null) {
                        edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                    }
                }
                btnMinus1.setOnClickListener {
                    if (changeAmount!=null && changeAmount!! >=1)   //數量最少要是1
                        changeAmount = changeAmount!! - 1           //點擊數量-1

                    Log.d("數量","$changeAmount")

                    //顯示點擊數量
                    if (edtNumber != null && changeAmount != null) {
                        edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                    }
                }
                btnConfirm.setOnClickListener {
                    // 如果變更後的數量變成 0 就刪除選擇商品
                    if (changeAmount == 0) {
                        // 安全鎖
                        var safeDeleteCheck = true
                        // 先用複製的清單確認刪除是否會導致關聯遭到破壞
                        val copyFilter: MutableList<Product> = ArrayList(filteredProductList) // 使用複製的清單進行檢查
                        copyFilter.removeAt(position)

                        // 創建一個新的協程，並在其中執行異步操作
                        lifecycleScope.launch(Dispatchers.IO) {
                            // 確認點案的項目是否會影響 折價券關係
                            if (!couponDeleteCheck(clickItem,copyFilter)) { // 檢查刪除後商品是否會破壞關聯
                                Log.d("警告", "刪除商品 ${clickItem.pName} 可能會破壞折價券關聯，請先刪除折價券")
                                safeDeleteCheck = false
                            }

                            // 如果所有檢查都通過，執行刪除操作
                            if (safeDeleteCheck) {
                                // 使用主執行緒進行UI操作
                                withContext(Dispatchers.Main) {
                                    // 刪除掃描商品提示訊息(商品名稱)
                                    Log.d("刪除前提示","開始刪除")
                                    Toast.makeText(requireContext(), "刪除: ${filteredProductList[position].pName}", Toast.LENGTH_SHORT).show()
                                    selectedQuantities.remove(filteredProductList[position])
                                    filteredProductList.removeAt(position)

                                    //重新載入清單
                                    loadFilterProduct()
                                }
                            }
                        }
                    }else{
                        //更新成新數量
                        if (changeAmount != null) {
                            //如果商品類別是否為折價券
                            if (clickItem.pluType == "75"){ //如果屬於折價券類別，就將數量變成負值
                                selectedQuantities[filteredProductList[position]] = changeAmount!! * -1
                            }else{
                                selectedQuantities[filteredProductList[position]] = changeAmount!!
                            }

                            Log.d("商品數量","${selectedQuantities[filteredProductList[position]]}")
                        }
                    }

                    //重新載入清單
                    loadFilterProduct()

                    bottomSheetDialog.dismiss()     //結束bottomView
                }
            }
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

            //確認輸入文字是否為會員
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
                                binding.txtMemberClass.text = "會員: $memberCardNumber"

                                Log.d("取得用戶資料",nowLoginMember.toString())
                                memberBottomDialog.dismiss()
                            }
                        }else{
                            Log.d("沒有此會員",memberCardNumber)

                            nowLoginMember = null //清除登錄會員紀錄

                            withContext(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "沒有此會員: $memberCardNumber", Toast.LENGTH_SHORT).show()

                                //重設會員提示文字
                                binding.txtMemberClass.text = "非會員"
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

        //輸入貨號後新增商品
        binding.btnSearch.setOnClickListener {
            //搜尋文字
            val searchMgaNo: String = binding.edtSearchRow.text.toString()

            //掃描清單名稱列表
            val filterNameList: List<String> = filteredProductList.map { it.pluMagNo }

            lifecycleScope.launch(Dispatchers.IO) {
                //結果代碼
                var resultCode = 0

                Log.d("filterNameLists內容",filterNameList.toString())
                Log.d("輸入內容",searchMgaNo)

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
                                filteredProductList.add(selectItem)
                                selectedQuantities[selectItem] = -1
                                Log.d("加入商品", selectItem.pName)
                                resultCode = 99
                            } else {
                                Log.d("不符合折價券規格", "未達折價券指定最低金額")
                                resultCode = 3
                            }
                        }else{
                            // 如果商品已經存在，數量指定為1
                            selectedQuantities[productDBManager.getProductByMagNo(searchMgaNo)!!] = 1

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
                        99 -> Toast.makeText(requireContext(),"新增成功",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //主程序外更新顯示內容
            loadFilterProduct()
        }

        // 定義下拉列表中的折扣選項
        val items = arrayOf("不適用折扣","會員折扣", "折價券", "檔期促銷")
        // 創建一個 ArrayAdapter 來設置 Spinner 的選項內容
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        // 設置下拉列表的風格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // 將 Adapter 設置給 Spinner
        binding.spDiscountList.adapter = adapter
        // 處理用戶選擇的事件
        binding.spDiscountList.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //選中該項目的處理
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                val simpleBottomDialog = BottomSheetDialog(requireContext())
                simpleBottomDialog.setContentView(R.layout.couponlist)

                //選擇折價券選項後開啟BottomView
                if (selectedItem == "折價券"){
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

                        //確認是否送出
                        var sentCouponItem: String?

                        withContext(Dispatchers.Main){
                            val couponAdapter = CouponAdapter(requireContext(), showUpList ?: emptyList())
                            grCouponList?.adapter = couponAdapter

                            couponAdapter.setOnItemClickListener(object : CouponAdapter.OnItemClickListener {

                                //取得按鈕點按位置
                                override fun onButtonClicked(position: Int) {

                                    //顯示折價券貨號
                                    val selectPluMagNo = filterPluList!![position].pluMagNo
                                    //Log.d("所選貨號", selectPluMagNo)

                                    sentCouponItem = selectPluMagNo

                                    Log.d("折扣券號",sentCouponItem.toString())

                                    // 送出折價券商品
                                    if (sentCouponItem != null) {
                                        // 將外部操作也放在協程內部
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            // 在主線程中執行資料庫操作
                                            val product = productDBManager.getProductByMagNo(sentCouponItem!!)

                                            // 將商品加入購物車 (必須不包含在已知清單內)
                                            if (product != null && !filteredProductList.contains(product)) {
                                                withContext(Dispatchers.Main) {
                                                    //確認優惠券能否放入清單
                                                    if(couponAddCheck(product)){
                                                        filteredProductList.add(product)
                                                        // 如果商品已經存在，數量指定為1
                                                        selectedQuantities[product] = -1

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
                                    } else {
                                        Log.d("優惠券檢查", "尚未更新")
                                    }
                                }
                            })

                            simpleBottomDialog.show()
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 沒被選中的處理
            }
        }

        // 定義下拉列表中的折扣選項
        val items2 = arrayOf("生鮮商品","預購商品", "菸酒類商品", "贈品")
        // 創建一個 ArrayAdapter 來設置 Spinner 的選項內容
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items2)
        // 設置下拉列表的風格
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // 將 Adapter 設置給 Spinner
        binding.spDiscountList2.adapter = adapter2
        // 處理用戶選擇的事件

        //按鈕清除輸入貨號
        binding.btnClear.setOnClickListener {
            binding.edtSearchRow.setText("")
        }

        //按下小計按鈕後送出
        binding.btnDeal.setOnClickListener {
            if (totalSumUnitPrice < 0){
                Toast.makeText(requireContext(),"送出金額不可為負",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),"送出金額 $totalSumUnitPrice",Toast.LENGTH_SHORT).show()
            }
        }

        return root
    }

    //確認商品能否移出清單(必須新移除對應的優惠券)
    private fun couponDeleteCheck(deleteItem: Product, copyFilter: MutableList<Product>): Boolean {
        // 檢查刪除項目是否為折價券，如果是直接返回 true
        if (deleteItem.pluType == "75") {
            Log.d("類型", "折價券")
            return true
        }

        // 遍歷 copyFilter，確保在刪除 A 商品之前不刪除符合 A' 折價券條件的商品
        for (pList in copyFilter) {
            if (pList.pluType == "75") { // 檢查 copyFilter 中的折價券商品
                val couponMainItem = productDBManager.getCouponMainByPluMagNo(pList.pluMagNo)
                val couponDetail = productDBManager.getCouponDetailBypluMagNo(pList.pluMagNo)

                // 確保折價券主檔和明細檔都存在
                if (couponMainItem != null && couponDetail != null) {
                    // 檢查折價券的指定比對邏輯
                    if (couponMainItem.BASE_TYPE == "1") { // 完全符合條件
                        // 逐一檢查折價券明細條件
                        for (detail in couponDetail) {
                            // 如果 A 商品符合 A' 折價券的條件，則返回 false，表示不能刪除 A 商品
                            if ((deleteItem.pluMagNo == detail.PLU_MagNo || detail.PLU_MagNo == null) &&
                                (deleteItem.DEP_No == detail.DEP_No || detail.DEP_No == null) &&
                                (deleteItem.CAT_No == detail.CAT_No || detail.CAT_No == null) &&
                                (deleteItem.VEN_No == detail.VEN_No || detail.VEN_No == null)
                            ) {
                                Log.d("刪除前警示","請先刪除對應的折價券商品")
                                return false
                            }
                        }
                    }else if (couponMainItem.BASE_TYPE == "2") { // 必須互斥
                        // 逐一檢查折價券明細條件
                        for (detail in couponDetail) {
                            // 如果 A 商品與 A' 折價券的條件有任一符合，則返回 false，表示不能刪除 A 商品
                            if (!((deleteItem.pluMagNo == detail.PLU_MagNo || detail.PLU_MagNo == null) &&
                                (deleteItem.DEP_No == detail.DEP_No || detail.DEP_No == null) &&
                                (deleteItem.CAT_No == detail.CAT_No || detail.CAT_No == null) &&
                                (deleteItem.VEN_No == detail.VEN_No || detail.VEN_No == null))
                            ) {
                                Log.d("刪除前警示","請先刪除對應的折價券商品")
                                return false
                            }
                        }
                    }
                }
            }
        }

        // 如果迴圈運行完畢仍未找到符合條件的情況，則返回 true，表示可以刪除 A 商品
        return true
    }


    //確認優惠券能否放入清單(必須超過總價)       //input: 折價券
    private suspend fun couponAddCheck(product: Product): Boolean {
        val selectItem = withContext(Dispatchers.IO) {
            productDBManager.getCouponMainByPluMagNo(product.pluMagNo) as CouponMain //確認所選項目是否為折價券
        }

        var singlePrc = 0   //產品單價(較低的)
        singlePrc = if (nowLoginMember != null){
            if (product.memPrc < product.unitPrc){
                product.memPrc
            }else{
                product.unitPrc
            }
        }else{
            product.unitPrc
        }

        //這邊改成抓coupon Main對應的所有coupon Detail 商品項目去跟 filterList 比對(多筆的SEQ_NO都放入清單: 不同的where 條件)

        //已知 pluType='75' 類別
        return when (selectItem.DISC_TYPE) {
            "0" -> { //折扣券
                //看是否有 明細檔 (正確分類)
                var subCheck = 0    //折扣券檢核碼 (99:許可)

                val result = lifecycleScope.async(Dispatchers.IO) {
                    val detail = productDBManager.getCouponDetailBypluMagNo(selectItem.DISC_PLU_MagNo)  //多筆明細檔

                    if (detail != null) {
                        val detailLength = detail.size  //幾個 明細檔
                        var cycleCheckMutableList = mutableListOf<Product>()  //排除型折價券檢核碼(99:只有自己的類別)
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
                                    val matchPLU = detailItem.PLU_MagNo == null || filteredProductList.any { it.pluMagNo == detailItem.PLU_MagNo }  //檢查明細檔
                                    val matchDEP = detailItem.DEP_No == null || filteredProductList.any { it.DEP_No == detailItem.DEP_No }
                                    val matchCAT = detailItem.CAT_No == null || filteredProductList.any { it.CAT_No == detailItem.CAT_No }
                                    val matchVEN = detailItem.VEN_No == null || filteredProductList.any { it.VEN_No == detailItem.VEN_No }

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

                            for (detailItem in detail){
                                Log.d("折價券細項","序列號: ${detailItem.SEQ_NO}")

                                if (allProductList!=null){
                                    val matchedProducts = allProductList.filter { product ->
                                        val matchPLU = detailItem.PLU_MagNo == null || product.pluMagNo == detailItem.PLU_MagNo
                                        val matchDEP = detailItem.DEP_No == null || product.DEP_No == detailItem.DEP_No
                                        val matchCAT = detailItem.CAT_No == null || product.CAT_No == detailItem.CAT_No
                                        val matchVEN = detailItem.VEN_No == null || product.VEN_No == detailItem.VEN_No

                                        matchPLU && matchDEP && matchCAT && matchVEN
                                    }

                                    Log.d("折價券品項","品項: $matchedProducts")

                                    // 將檢查結果的結果添加到 MutableList 中
                                    for (i in matchedProducts){
                                        cycleCheckMutableList.add(i)
                                    }
                                }
                            }

                            //未超過折價券試用期間
                            if (subCheck != 6){
                                val exclusiveList = filteredProductList.subtract(
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

    // 檢查現在的時間是否在折價券適用期間內
    fun isCouponValid(coupon: CouponDetail): Boolean {
        val now = LocalDateTime.now()
        return now.isAfter(coupon.FROM_DATE) && now.isBefore(coupon.TO_DATE)
    }

    //螢幕旋轉或其他因素導致資料流失時會重新載入資料
    @SuppressLint("SetTextI18n")
    private fun loadFilterProduct() {

        //先重製總價
        totalSumUnitPrice = 0

        if (isFirstCreation){
            Log.d("會空喔",filteredProductList.toString())

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

            Log.d("不空喔",filteredProductList.toString())
            viewModel.filteredProductList.postValue(filteredProductList)    //更新到ViewModel

            val adapter: FilterProductAdapter
            if (nowLoginMember!=null){      //有會員登入
                adapter = FilterProductAdapter(filteredProductList, selectedQuantities,true)
            }else{
                adapter = FilterProductAdapter(filteredProductList, selectedQuantities,false)
            }

            lifecycleScope.launch(Dispatchers.Main) {
                binding.grTableProduct.adapter = adapter
                binding.grTableProduct.numColumns = 1
                adapter.notifyDataSetChanged()
            }
        }

        //確認優惠券條件是否能繼續使用
        lifecycleScope.launch {
            for (item in filteredProductList){
                if(item.pluType == "75"){
                    Log.d("清單檢查","折扣券")

//                    if (!couponAddCheck(item)){     //不滿足折扣券使用條件
//                         filteredProductList.remove(item)   //刪除該項目，並且重新導入清單
//                    }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            //變更小計金額
            for (product in filteredProductList) {
                // 檢查商品是否在 selectNumberMap 中，如果沒有，預設選擇數量為 0
                val selectNumber = selectedQuantities.getOrDefault(product, 0)

                var totalPrice = 0

                //計算單項小計
                if(nowLoginMember != null){    //確認是否為會員
                    //防止會員價比折扣價還高的狀況
                    if (product.memPrc > product.unitPrc){
                        totalPrice = product.unitPrc * selectNumber     //適用較低的價格
                    }else{
                        totalPrice = product.memPrc * selectNumber
                    }
                }else{
                    totalPrice = product.unitPrc * selectNumber
                }

                totalSumUnitPrice += totalPrice
            }

            //當總小計大於0時，顯示總小計
            if (totalSumUnitPrice>0){
                binding.txtTotalDollar.visibility = View.VISIBLE    //開啟可視化
                binding.txtTotalDollar.text = "總計: $totalSumUnitPrice 元"
            }else{
                binding.txtTotalDollar.visibility = View.INVISIBLE   //金額小於0不可視
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

    override fun onDestroyView() {
        super.onDestroyView()

        //離開頁面就清除 掃描清單、掃描個數
        selectedQuantities.clear()
        filteredProductList.clear()

        _binding = null
    }
}