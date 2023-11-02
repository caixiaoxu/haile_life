package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.AppointmentSubmitViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityAppointmentSubmitBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelItemBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class AppointmentSubmitActivity :
    BaseBusinessActivity<ActivityAppointmentSubmitBinding, AppointmentSubmitViewModel>(
        AppointmentSubmitViewModel::class.java, BR.vm
    ) {

    override fun layoutId(): Int = R.layout.activity_appointment_submit

    override fun backBtn(): View = mBinding.barAppointSubmitTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.shopId = IntentParams.ShopParams.parseShopId(intent)
    }

    override fun initEvent() {
        super.initEvent()
        val inflater = LayoutInflater.from(this@AppointmentSubmitActivity)

        // 规格
        mViewModel.specList.observe(this) { specList ->
            if (specList.isNotEmpty()) {
                mBinding.includeAppointSubmitSpec.clScanOrderConfig.let { cl ->
                    if (cl.childCount > 3) {
                        cl.removeViews(3, cl.childCount - 3)
                    }
                    specList.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            mViewModel.selectCategory.observe(this) {
                                itemBinding.code = it?.goodsCategoryCode
                            }
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->
                                mViewModel.selectSpec.observe(this) {
                                    (item.specValueId == it.specValueId).let { isSame ->
                                        if (isSame != rb.isChecked) {
                                            rb.isChecked = isSame
                                        }
                                    }
                                }
                                rb.setOnClickListener {
                                    mViewModel.selectSpec.value = item
                                    mViewModel.selectDevice.value = null
                                }
                            }
                            cl.addView(
                                itemBinding.root,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }
                    // 设置id
                    val idList = IntArray(specList.size) { it + 1 }
                    mBinding.includeAppointSubmitSpec.flowScanOrderItem.referencedIds = idList
                    mBinding.includeAppointSubmitSpec.flowScanOrderItem.visibility = View.VISIBLE
                }
            }
        }

        // 时长
        mViewModel.minuteList.observe(this) { minuteList ->
            if (minuteList.isNotEmpty()) {
                mViewModel.selectMinute.value = minuteList.firstOrNull()?.minute
                mBinding.includeAppointSubmitMinute.clScanOrderConfig.let { cl ->
                    if (cl.childCount > 3) {
                        cl.removeViews(3, cl.childCount - 3)
                    }
                    minuteList.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            mViewModel.selectCategory.observe(this) {
                                itemBinding.code = it?.goodsCategoryCode
                            }
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->
                                mViewModel.selectMinute.observe(this) {
                                    (item.minute == it).let { isSame ->
                                        if (isSame != rb.isChecked) {
                                            rb.isChecked = isSame
                                        }
                                    }
                                }
                                rb.setOnClickListener {
                                    mViewModel.selectMinute.value = item.minute
                                    mViewModel.selectDevice.value = null
                                }
                            }
                            cl.addView(
                                itemBinding.root,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }
                    // 设置id
                    val idList = IntArray(minuteList.size) { it + 1 }
                    mBinding.includeAppointSubmitMinute.flowScanOrderItem.referencedIds = idList
                    mBinding.includeAppointSubmitMinute.flowScanOrderItem.visibility = View.VISIBLE
                }
            }
        }

        mViewModel.selectCategory.observe(this) {
            mBinding.barAppointSubmitTitle.setTitle(it.goodsCategoryName)
            mViewModel.requestAppointSpecListAsync(this)
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS, Boolean::class.java)?.observe(this) {
            if (it) {
                finish()
            }
        }

        LiveDataBus.with(BusEvents.LOGIN_STATUS)?.observe(this) {
            mViewModel.requestData()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.viewAppointSubmitSelected.setOnClickListener {
            if (null == mViewModel.selectDevice.value) {
                SToast.showToast(this@AppointmentSubmitActivity, mViewModel.deviceTitle.value!!)
                return@setOnClickListener
            }

            startActivity(Intent(this, OrderSubmitActivity::class.java).apply {
                putExtras(
                    IntentParams.OrderSubmitParams.pack(
                        listOf(
                            IntentParams.OrderSubmitParams.OrderSubmitGood(
                                mViewModel.selectCategory.value!!.goodsCategoryCode,
                                mViewModel.selectDevice.value!!.goodsId,
                                mViewModel.selectDevice.value!!.goodsItemId,
                                "${if (true == mViewModel.isDryer.value) mViewModel.selectMinute.value else 1}"
                            )
                        ),
                        mViewModel.selectDevice.value!!.appointmentTime,
                        mViewModel.selectDevice.value!!.goodsName,
                        mViewModel.selectDevice.value!!.shopName
                    )
                )
            })
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}