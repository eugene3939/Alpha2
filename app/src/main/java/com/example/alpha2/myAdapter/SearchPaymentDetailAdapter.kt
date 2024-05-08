package com.example.alpha2.myAdapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.alpha2.DBManager.Payment.PaymentDetail
import com.example.alpha2.R


class SearchPaymentDetailAdapter(private val paymentDetails: List<PaymentDetail>, private val memberCheck: Boolean) : BaseAdapter() {
    override fun getCount(): Int {
        return paymentDetails.size
    }

    override fun getItem(position: Int): Any {
        return paymentDetails[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.search_invoice, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        // 填充佈局元素的數據，例如：holder.textView.text = "Some Text"
        val details = getItem(position) as PaymentDetail

        holder.TxtNo.text = (position+1).toString()
        holder.TxtBarcode.text = details.PLU_No
        holder.TxtPName.text = details.PLU_Name
        holder.TxtAmount.text = details.TXN_Qty.toString()
        holder.TxtPrice.text = details.TXN_SaleAmt.toString()
        holder.TxtDiscount.text = "0"
        holder.TxtTotal.text = details.TXN_SaleAmt.toString()

        return view!!
    }

    private class ViewHolder(view: View) {
        val TxtNo: TextView = view.findViewById(R.id.txtInvoiceResult)              //項次
        val TxtBarcode: TextView = view.findViewById(R.id.txtInvoiceBarcode)        //條碼
        val TxtPName: TextView = view.findViewById(R.id.txtInvoicePName)            //名稱
        val TxtAmount: TextView = view.findViewById(R.id.txtInvoiceAmount)          //數量
        val TxtPrice: TextView = view.findViewById(R.id.txtInvoicePrice)            //單價
        val TxtDiscount: TextView = view.findViewById(R.id.txtInvoiceDiscount)      //折扣
        val TxtTotal: TextView = view.findViewById(R.id.txtInvoiceCountingResult)   //小計
    }
}