package com.example.alpha2.myAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.alpha2.R
import com.example.alpha2.myObject.CartItem
import kotlin.math.roundToInt

class FilterProductAdapter(private val dataList: MutableList<CartItem>,private val memberCheck: Boolean) : BaseAdapter() {
    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.selectitem, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val item = dataList[position]
        holder.bind(item,memberCheck)

        return view!!
    }

    class ViewHolder(itemView: View){
        //請見selectitem.xml
        private val productID: TextView = itemView.findViewById(R.id.txt_productIndex)
        private val productName: TextView = itemView.findViewById(R.id.txt_productName)
        private val productPrice: TextView = itemView.findViewById(R.id.txt_productPrice)
        private val productDiscount: TextView = itemView.findViewById(R.id.txt_product_discount)
        private val productSum: TextView = itemView.findViewById(R.id.txt_productSum)

        private val shopNumber: TextView = itemView.findViewById(R.id.txt_product_buy_number)
        @SuppressLint("SetTextI18n")
        fun bind(item: CartItem,memberCheck: Boolean) {
            productID.text = "${item.sequence}"
            // 使用 Glide 或其他圖片載入庫載入商品圖片
            shopNumber.text = "x ${item.quantity}"

            productDiscount.text = "${item.discountS.toInt()}"

            productName.text = truncateString(item.productItem.PLU_PrnName, 20)

            //如果是會員就顯示會員價，非會員unitPrice
            if (memberCheck){
                //防止會員價比折扣價還高的狀況 (適用較低價格)
                if (item.productItem.memPrc > item.productItem.unitPrc){
                    productPrice.text = "${item.productItem.unitPrc.roundToInt()}"
                    productSum.text = "${(item.productItem.unitPrc * item.quantity + item.discountS).roundToInt()} 元"     //單向小計
                }else{
                    productPrice.text = "${item.productItem.memPrc.roundToInt()}"
                    productSum.text = "${(item.productItem.memPrc * item.quantity + item.discountS).roundToInt()} 元"     //單向小計
                }
            }else{
                productPrice.text = "${item.productItem.unitPrc.roundToInt()}"
                productSum.text = "${(item.productItem.unitPrc * item.quantity + item.discountS).roundToInt()} 元"     //單向小計
            }

            //如果是 "小計折扣" 就顯示全折金額
            if (item.productItem.PLU_No == "00"){
                productDiscount.text = (item.discountT).roundToInt().toString()     //折扣額
                productSum.text = "${(item.discountT).roundToInt()} 元"  //小計
            }
        }

        //限制字串長度
        private fun truncateString(input: String, maxLength: Int): String {
            return if (input.length > maxLength) {

                val insertIndex  = 10 //第七個字元進行換行
                val builder = StringBuilder(input)
                builder.insert(insertIndex, "\n")
                builder.toString()
            } else {
                input
            }
        }
    }
}