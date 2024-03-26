package com.example.alpha2.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.example.alpha2.myAdapter.FilterProductAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

                            //處理完後重設掃描商品狀態
                            existItemCheck = false
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

//        將dao資料填寫進去list的方法
//        lifecycleScope.launch(Dispatchers.IO) {
//            productCategoryList = productDBManager.getCategoryList("pType")
//            withContext(Dispatchers.Main) {
//                setupSpinner()
//            }
//        }

        //變更GridView顯示項目
        loadFilterProduct()

        //點擊開啟掃描器
        binding.btBarcodeScanner.setOnClickListener {
            startBarcodeScanner()
        }

        //主頁會顯示的商品類別，方便用戶選擇
        setupSpinner()

        return root
    }

    //螢幕旋轉或其他因素導致資料流失時會重新載入資料
    private fun loadFilterProduct() {
        if (isFirstCreation) {
            Log.d("會空喔", filteredProductList.toString())
            // 從 ViewModel 中讀取 filteredProductList
            val productList = viewModel.filteredProductList.value
            if (productList != null && productList.isNotEmpty()) {
                val adapter = FilterProductAdapter(productList)
                binding.grTableProduct.adapter = adapter
                binding.grTableProduct.numColumns = 1
                adapter.notifyDataSetChanged()
            }
            isFirstCreation = false
        }else{
            Log.d("不空喔",filteredProductList.toString())
            viewModel.filteredProductList.postValue(filteredProductList)    //更新到ViewModel
            val adapter = FilterProductAdapter(filteredProductList)
            binding.grTableProduct.adapter = adapter
            binding.grTableProduct.numColumns = 1
            adapter.notifyDataSetChanged()
        }
    }

    //設定下拉式清單顯示類別
    private fun setupSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            productCategoryList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spProductType.adapter = adapter

        binding.spProductType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // 選中項目的邏輯
                val selectedItem = parent?.getItemAtPosition(position).toString()
                Log.d("選中項目", selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // 未選中項目的邏輯
                Log.d("選中項目", "未選中向任何項目")
            }
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
