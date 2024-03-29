package com.example.alpha2.myAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.R

class FilterProductAdapter(private val dataList: List<Product>,private val numberInf: MutableMap<Product, Int>) : BaseAdapter() {
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
            holder = ViewHolder(view, numberInf, position+1)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val product = dataList[position]
        holder.bind(product, numberInf, position + 1)

        return view!!
    }

    class ViewHolder(itemView: View, numberInf: MutableMap<Product, Int>, productId: Int) {
        //請見selectitem.xml
        private val productID: TextView = itemView.findViewById(R.id.txt_productIndex)
        private val productName: TextView = itemView.findViewById(R.id.txt_productName)
        private val productPrice: TextView = itemView.findViewById(R.id.txt_productPrice)
        private val productSum: TextView = itemView.findViewById(R.id.txt_productSum)

        private val shopNumber: TextView = itemView.findViewById(R.id.txt_product_buy_number)
        @SuppressLint("SetTextI18n")
        fun bind(product: Product, numberInf: MutableMap<Product, Int>, ID: Int) {
            productID.text = "$ID"
            // 使用 Glide 或其他圖片載入庫載入商品圖片
            shopNumber.text = "x ${numberInf[product]}"

            productName.text = truncateString(product.pName, 20)
            productPrice.text = "單價: ${product.unitPrc} 元"
            productSum.text = "小計: ${product.unitPrc * numberInf[product]!!} 元"     //單向小計
        }

        //限制字串長度
        private fun truncateString(input: String, maxLength: Int): String {
            return if (input.length > maxLength) {
                input.substring(0, maxLength - 3) + "..."
            } else {
                input
            }
        }
    }
}