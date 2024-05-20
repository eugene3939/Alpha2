package com.example.alpha2.ui.notifications

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.alpha2.DBManager.Payment.PaymentDetail
import com.example.alpha2.DBManager.Payment.PaymentMain
import com.example.alpha2.DBManager.Payment.PaymentManager
import com.example.alpha2.databinding.FragmentNotificationsBinding
import com.example.alpha2.myAdapter.SearchPaymentDetailAdapter
import kotlin.math.roundToInt

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    private lateinit var paymentDBManager: PaymentManager       //付款主檔 (取得付款相關Dao資料)

    private var nowSearchPaymentMain: PaymentMain?= null      //目前查詢的支付主檔，預設為空
    private var nowSearchPaymentDetail: MutableList<PaymentDetail>?= null      //目前查詢的支付明細檔，預設為空


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this)[NotificationsViewModel::class.java]

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //初始化Dao
        paymentDBManager = PaymentManager(requireContext())

        //按下查詢後找出對應的銷售主檔
        binding.btnInvoiceSearch.setOnClickListener {
            val searchText = binding.edtInvoiceText.text
            if (searchText != null){  //存在輸入內容

                //搜尋符合的項目
                val accessPaymentMain = paymentDBManager.searchPaymentMainByTXN_GUINo(searchText.toString())
                val accessPaymentDetail = paymentDBManager.searchPaymentDetailByTXN_GUINo(searchText.toString())

                if (accessPaymentMain!=null){
                    nowSearchPaymentMain = accessPaymentMain
                    nowSearchPaymentDetail = accessPaymentDetail
                    //更新查詢顯示文字
                    updateShowText()
                    Toast.makeText(requireContext(), "查詢單號: ${accessPaymentMain.TXN_GUIBegNo}", Toast.LENGTH_LONG).show()
                }
                else
                    Toast.makeText(requireContext(), "查無單號: $searchText", Toast.LENGTH_LONG).show()
            }
        }

        //顯示符合的前一筆購買清單
        return root
    }

    //更新查詢顯示文字
    @SuppressLint("SetTextI18n")
    private fun updateShowText() {
        //明細總和
        if (nowSearchPaymentMain != null){
            binding.txtSearchSalesAmount.text = nowSearchPaymentMain!!.TXN_TotPayAmt.roundToInt().toString()    //發票金額
            //銷售數量要去掉小計折扣選項
            var discT_No = 0
            for (i in nowSearchPaymentDetail!!){
                if (i.PLU_No == "0000000"){    //存在全折項目
                    discT_No+=1
                }
            }
            binding.txtSearchSalesNumber.text = (nowSearchPaymentMain!!.TXN_TotQty-discT_No).toString()        //銷售數量
            binding.txtSearchDiscountValue.text = (nowSearchPaymentMain!!.TXN_TotDiscS + nowSearchPaymentMain!!.TXN_TotDiscT).roundToInt().toString()      //折扣總額
            binding.txtSearchSalesTotalPrice.text = nowSearchPaymentMain!!.TXN_TotGUI.toString()      //銷售總額
        }

        //銷售商品資訊
        if (!nowSearchPaymentDetail.isNullOrEmpty()){
            try {
                val myAdapter = SearchPaymentDetailAdapter(nowSearchPaymentDetail!!,memberCheck = true)

                binding.lsInvoiceSearchResult.adapter = myAdapter

            }catch (e: Exception){
                Log.e("錯誤",e.toString())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}