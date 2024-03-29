package com.example.alpha2.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.example.alpha2.R
import com.example.alpha2.myAdapter.FilterProductAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

//不允許螢幕旋轉，螢幕旋轉容易導致資料流失

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)
    private lateinit var productDBManager: ProductManager //商品Dao

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

    //鏡頭開啟時處理條碼邏輯
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
                                //將掃描到的商品加入列表中
                                filteredProductList.add(product)

                                Log.d("加入商品", filteredProductList.last().pName)

                                // 如果商品已經存在，數量指定為1
                                selectedQuantities[product] = 1

                                existItemCheck = true

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

        // 創建一個 BottomSheetDialog
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
        binding.grTableProduct.setOnItemClickListener { _, _, position, _ ->
            //Toast.makeText(requireContext(),"位置: $position",Toast.LENGTH_SHORT).show()

            // 顯示 BottomView
            bottomSheetDialog.show()

            if (btnPlus1 != null && btnMinus1 != null && btnConfirm!=null) {

                //點擊商品的目前數量
                val selectScanNumber = selectedQuantities[filteredProductList[position]]
                //點擊數量
                var changeAmount = selectScanNumber

                //顯示點數量
                if (edtNumber != null && changeAmount != null) {
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
                    if (changeAmount!=null && changeAmount!! >=1)  //數量最少要是1
                        changeAmount = changeAmount!! - 1       //點擊數量-1
                    Log.d("數量","$changeAmount")

                    //顯示點擊數量
                    if (edtNumber != null && changeAmount != null) {
                        edtNumber.text = Editable.Factory.getInstance().newEditable(changeAmount.toString())
                    }
                }
                btnConfirm.setOnClickListener {
                    //如果變更後的數量變成0就刪除選擇商品
                    if (changeAmount == 0){
                        //刪除掃描商品提示訊息(商品名稱)
                        Toast.makeText(requireContext(),"刪除: ${filteredProductList[position].pName}",Toast.LENGTH_SHORT).show()

                        selectedQuantities.remove(filteredProductList[position])
                        filteredProductList.removeAt(position)
                    }else{
                        //更新成新數量
                        if (changeAmount != null) {
                            selectedQuantities[filteredProductList[position]] = changeAmount!!

                            Log.d("商品數量","${selectedQuantities[filteredProductList[position]]}")
                        }
                    }

                    //重新載入清單
                    loadFilterProduct()

                    bottomSheetDialog.dismiss()     //結束bottomView
                }
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

                //輸入貨號不包含在清單，且存在於商品目錄，可新增
                if (!filterNameList.contains(searchMgaNo) && productDBManager.getProductByMagNo(searchMgaNo)!=null){

                    //和規的貨號，加入清單
                    try {
                        filteredProductList.add(productDBManager.getProductByMagNo(searchMgaNo)!!)
                        Log.d("加入商品", productDBManager.getProductByMagNo(searchMgaNo)!!.pName)

                        // 如果商品已經存在，數量指定為1
                        selectedQuantities[productDBManager.getProductByMagNo(searchMgaNo)!!] = 1

                        resultCode = 99

                    }catch (e: Exception){
                        println(e)
                    }

                }else{
                    //已存在或是不合規定的訂單編號
                    if (productDBManager.getProductByMagNo(searchMgaNo)==null){
                        Log.d("貨號搜尋錯誤","不存在的貨號")
                        resultCode = 1
                    }else if (filterNameList.contains(searchMgaNo)){
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
                        99 -> Toast.makeText(requireContext(),"新增成功",Toast.LENGTH_SHORT).show()
                    }
                }
            }

            //主程序外更新顯示內容
            loadFilterProduct()
        }

        //按鈕清除輸入貨號
        binding.btnClear.setOnClickListener {
            binding.edtSearchRow.setText("")
        }

        return root
    }

    //螢幕旋轉或其他因素導致資料流失時會重新載入資料
    @SuppressLint("SetTextI18n")
    private fun loadFilterProduct() {

        //先重製總價
        totalSumUnitPrice = 0

        if (isFirstCreation){
            Log.d("會空喔",filteredProductList.toString())

            //離開頁面就清除項目，不要紀錄
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
            val adapter = FilterProductAdapter(filteredProductList, selectedQuantities)
            binding.grTableProduct.adapter = adapter
            binding.grTableProduct.numColumns = 1
            adapter.notifyDataSetChanged()
        }

        //變更小計金額
        for (product in filteredProductList) {
            // 檢查商品是否在 selectNumberMap 中，如果沒有，預設選擇數量為 0
            val selectNumber = selectedQuantities.getOrDefault(product, 0)
            // 計算單一小計
            val totalPrice = product.unitPrc * selectNumber

            totalSumUnitPrice += totalPrice
        }

        //當總小計大於0時，顯示總小計
        if (totalSumUnitPrice>0){
            binding.txtTotalDollar.visibility = View.VISIBLE
            binding.txtTotalDollar.text = "總計: $totalSumUnitPrice 元"
        }else{
            binding.txtTotalDollar.visibility = View.INVISIBLE
        }
    }

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
        _binding = null
    }
}