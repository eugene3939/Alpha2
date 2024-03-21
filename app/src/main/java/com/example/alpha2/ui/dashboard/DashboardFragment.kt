package com.example.alpha2.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.alpha2.DBManager.User.UserManager
import com.example.alpha2.databinding.FragmentDashboardBinding

//POS主頁面
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var userDBManager: UserManager       //用戶Dao (用封裝的方式獲取Dao)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //初始化Dao
        userDBManager = UserManager(requireContext())

        val sharedPreferences = requireContext().getSharedPreferences("loginUser", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userId", "")

        //確認用戶身分
        if (userID!=null){
            val accessUser = userDBManager.getUserById(userID)
            if (accessUser != null) {
                binding.textDashboard.text = "收銀員: ${accessUser.name}"
            }
        }else{
            binding.textDashboard.text = "Null user access"
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}