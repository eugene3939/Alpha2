package com.example.alpha2.myAdapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class ChooseItemListAdapter(private val context: Context, private val dataList: List<String>): BaseAdapter()  {

    // 定義一個點擊監聽器接口
    interface OnItemClickListener {
        fun onButtonClicked(position: Int)
    }

    // 儲存點擊監聽器的變量
    private var listener: CouponAdapter.OnItemClickListener? = null

    // 提供一個方法來設置點擊監聽器
    fun setOnItemClickListener(listener: CouponAdapter.OnItemClickListener) {
        this.listener = listener
    }

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
        TODO("Not yet implemented")
    }
}