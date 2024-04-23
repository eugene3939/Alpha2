package com.example.alpha2

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.ui.home.HomeFragment
import com.example.alpha2.ui.home.HomeViewModel

class Payment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 使用 intent 獲取 商品清單
        try {
            val receivedFilteredList = intent.getSerializableExtra("filteredList_key") as? MutableList<*>
            val receivedSelectedQuantities = intent.getSerializableExtra("quantities_key") as? MutableMap<*, *>
            Log.d("調用商品清單測試", receivedFilteredList.toString())
            Log.d("調用商品數量測試", receivedSelectedQuantities.toString())
        }catch (e: Exception){
            Log.d("傳遞失敗","空的intent")
        }
    }
}