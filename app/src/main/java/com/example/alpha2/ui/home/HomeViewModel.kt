package com.example.alpha2.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.alpha2.DBManager.Product.Product

class HomeViewModel : ViewModel() {

    val filteredProductList = MutableLiveData<MutableList<Product>>()
}