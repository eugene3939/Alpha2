package com.example.alpha2.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.DBManager.Product.ProductManager
import com.example.alpha2.DBManager.User.UserManager
import com.example.alpha2.databinding.FragmentHomeBinding
import com.google.zxing.integration.android.IntentIntegrator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)
    private lateinit var productDBManager: ProductManager //商品Dao

    private var productCategoryList: MutableList<String> = mutableListOf("食物", "飲料")   //商品類別(只會顯示常用商品，或是沒有條碼可掃描的商品)

    //List儲存商品篩選結果(依據文字搜尋或欄位搜尋結果)
    private var filteredProductList: List<Product> = emptyList()

    private val REQUEST_CODE_CAMERA = 1001
    private val REQUEST_CODE_SCAN = 1002 // 新增這行，定義掃描請求碼

    private var scanText: String? = null

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

        //主頁會顯示的商品類別，方便用戶選擇
        setupSpinner()

        //點按掃描按鈕，用Toast.makeText顯示掃描內容
        binding.btBarcodeScanner.setOnClickListener {
            startBarcodeScanner()
        }

        return root
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

    //辨識鏡頭掃描內容
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SCAN) {
            val result = IntentIntegrator.parseActivityResult(resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(requireContext(), "掃描取消", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), "掃描內容: ${result.contents}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    //開啟掃描鏡頭
    private fun startBarcodeScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setPrompt("請對準條碼進行掃描")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(false)
        integrator.setRequestCode(REQUEST_CODE_SCAN)
        integrator.initiateScan()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}