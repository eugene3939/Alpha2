package com.example.alpha2.myObject

import com.example.alpha2.DBManager.Product.Product
import java.io.Serializable

data class CartItem(val sequence:Int,
                    val productItem: Product,
                    var quantity: Int,
                    var discountS: Double = 0.00,    //人工折扣
                    var discountT: Double = 0.00,    //總合小計折扣
                    var info: String ?= "normal"     //商品備註 (組合商品: comb, 全折: discT)
                    ):Serializable