package com.yunshang.haile_life.ui.activity.order

import android.graphics.Color
import androidx.fragment.app.Fragment
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.OrderListViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityOrderListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.fragment.OrderFragment

class OrderListActivity : BaseBusinessActivity<ActivityOrderListBinding, OrderListViewModel>(
    OrderListViewModel::class.java
) {
    private val orderListFragment by lazy {
        OrderFragment().apply {
            arguments = intent.extras
        }
    }

    override fun layoutId(): Int = R.layout.activity_order_list

    override fun initView() {
        window.statusBarColor = Color.WHITE
        supportFragmentManager.beginTransaction().add(R.id.fl_order_list_parent, orderListFragment)
            .commit()
    }

    override fun initData() {
    }
}