package com.example.alpha2.myObject

import android.os.Parcel
import android.os.Parcelable
import com.example.alpha2.DBManager.Product.Product
import java.io.Serializable

data class CartItem(val sequence:Int, val productItem: Product, var quantity: Int, val discount: Double = 0.00):Serializable