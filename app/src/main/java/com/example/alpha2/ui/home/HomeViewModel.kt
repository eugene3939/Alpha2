package com.example.alpha2.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.alpha2.DBManager.Product.Product

class HomeViewModel : ViewModel() {
    var filteredProductList = mutableListOf<Product>()       //商品項次
    var selectedQuantities = mutableMapOf<Product, Int>()                   //項次對應的數量
}