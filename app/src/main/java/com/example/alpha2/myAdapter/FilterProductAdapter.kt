package com.example.alpha2.myAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.R

class FilterProductAdapter(private val dataList: List<Product>) : BaseAdapter() {
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

        val product = dataList[position]
        holder.bind(product)

        return view!!
    }

    class ViewHolder(itemView: View) {
        //請見selectitem.xml
        private val productName: TextView = itemView.findViewById(R.id.txt_productName)
        private val productType: TextView = itemView.findViewById(R.id.txt_productType)
        private val productPrice: TextView = itemView.findViewById(R.id.txt_productPrice)
        private val productNumber: TextView = itemView.findViewById(R.id.txt_productNumber)

        val shopNumber: TextView = itemView.findViewById(R.id.txt_product_buy_number)
        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            // 使用 Glide 或其他圖片載入庫載入商品圖片
            shopNumber.text = "x 1"

            productName.text = truncateString(product.pName, 20)
            productType.text = product.pType
            productPrice.text = "${product.unitPrc}元"
            productNumber.text = "${product.pNumber}個"
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