package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.SelectAppointmentDeviceViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.AppointDevice
import com.yunshang.haile_life.databinding.ActivitySelectAppointmentDeviceBinding
import com.yunshang.haile_life.databinding.ItemSelectAppointDeviceBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter

class SelectAppointmentDeviceActivity :
    BaseBusinessActivity<ActivitySelectAppointmentDeviceBinding, SelectAppointmentDeviceViewModel>(
        SelectAppointmentDeviceViewModel::class.java
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemSelectAppointDeviceBinding, AppointDevice>(
            R.layout.item_select_appoint_device, BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                setResult(RESULT_OK, Intent().apply {
                    putExtras(IntentParams.SelectAppointDeviceParams.packResult(item))
                })
                finish()
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_select_appointment_device

    override fun backBtn(): View = mBinding.includeTitleList.barTitleListTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.shopId = IntentParams.SelectAppointDeviceParams.parseShopId(intent)
        mViewModel.specValueId = IntentParams.SelectAppointDeviceParams.parseSpecValueId(intent)
        mViewModel.unit = IntentParams.SelectAppointDeviceParams.parseUnit(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.appointDeviceList.observe(this) {
            mBinding.includeTitleList.refreshView.finishRefresh()
            mAdapter.refreshList(it, true)
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        IntentParams.SelectAppointDeviceParams.parseCategoryName(
            intent
        )?.let {
            mBinding.includeTitleList.barTitleListTitle.setTitle(
                com.lsy.framelib.utils.StringUtils.getString(
                    R.string.select_work_device,
                    it.replace("机", "")
                )
            )
        }

        mBinding.includeTitleList.refreshView.setOnRefreshListener {
            mViewModel.requestAppointDevice()
        }

        mBinding.includeTitleList.rvTitleListList.layoutManager = LinearLayoutManager(this)
        ContextCompat.getDrawable(this, R.drawable.divide_size8)?.let {
            mBinding.includeTitleList.rvTitleListList.addItemDecoration(
                DividerItemDecoration(
                    this@SelectAppointmentDeviceActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.includeTitleList.rvTitleListList.adapter = mAdapter
    }

    override fun initData() {
        mViewModel.requestAppointDevice()
    }
}