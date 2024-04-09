package com.example.alpha2.myAdapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import com.example.alpha2.DBManager.Product.Product
import com.example.alpha2.R

class CouponAdapter(private val context: Context, private val dataList: List<String>): BaseAdapter() {
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
            view = LayoutInflater.from(context).inflate(R.layout.couponitem, parent, false)
            holder = ViewHolder()
            holder.couponID = view.findViewById(R.id.txtCouponID)
            holder.couponName = view.findViewById(R.id.txtCouponName)
            holder.couponPrice = view.findViewById(R.id.txtCouponPrice)
            holder.couponConfirmButton = view.findViewById(R.id.btnCouponConfirm)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        val data = dataList[position]

        val couponData = data.split(", ")

        if (couponData.size == 3) {
            holder.couponID?.text = couponData[0]
            holder.couponName?.text = couponData[1]
            holder.couponPrice?.text = couponData[2]
        } else {
            Log.e("CouponAdapter", "Invalid data format at position $position: $data")
        }

        // 設置按鈕的點擊監聽器
        holder.couponConfirmButton?.setOnClickListener {
            // 在這裡執行按鈕點擊時的操作
            Log.d("CouponAdapter", "Button clicked at position $position")
            // 你可以在這裡執行任何想要做的操作，例如打開新的活動或者彈出對話框等
        }

        return view!!
    }

    private class ViewHolder {
        var couponID: TextView? = null
        var couponName: TextView? = null
        var couponPrice: TextView? = null
        var couponConfirmButton: Button ?= null
    }
}