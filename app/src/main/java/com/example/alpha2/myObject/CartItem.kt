package com.example.alpha2.myObject

import com.example.alpha2.DBManager.Product.Product
import java.io.Serializable
                                                                                        //人工折扣                  //總合小計折扣
data class CartItem(val sequence:Int, val productItem: Product, var quantity: Int, var discountS: Double = 0.00, var discountT: Double = 0.00):Serializable