package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.StarfishRefundDetailViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityStarfishRefundDetailBinding
import com.yunshang.haile_life.databinding.ItemStarfishRefundDetailListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.utils.DialogUtils

class StarfishRefundDetailActivity :
    BaseBusinessActivity<ActivityStarfishRefundDetailBinding, StarfishRefundDetailViewModel>(
        StarfishRefundDetailViewModel::class.java, BR.vm
    ) {

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                mViewModel.refundDetail.value?.serviceTelephone?.let {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$it")))
                }
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.activity_starfish_refund_detail

    override fun backBtn(): View = mBinding.barStarfishRefundDetailTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.refundId = IntentParams.StarfishRefundDetailParams.parseRefundId(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.refundDetail.observe(this) {
            it?.let {
                if (mBinding.llStarfishRefundDetailList.childCount > 1) {
                    mBinding.llStarfishRefundDetailList.removeViews(
                        0,
                        mBinding.llStarfishRefundDetailList.childCount - 1
                    )
                }
                val inflater = LayoutInflater.from(this@StarfishRefundDetailActivity)
                it.userTokenCoinRefundItemRecordDTOList.forEach { record ->
                    val childBinding = DataBindingUtil.inflate<ItemStarfishRefundDetailListBinding>(
                        inflater,
                        R.layout.item_starfish_refund_detail_list,
                        null,
                        false
                    )
                    childBinding.item = record
                    mBinding.llStarfishRefundDetailList.addView(
                        childBinding.root,
                        mBinding.llStarfishRefundDetailList.size - 1,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        )
                    )
                }
            }
        }
    }

    override fun initView() {

        mBinding.tvRefundDetailContact.setOnClickListener {
            DialogUtils.checkPermissionDialog(
                this,
                supportFragmentManager,
                SystemPermissionHelper.callPhonePermissions(),
                "需要权限来拨打电话"
            ) {
                requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
            }
        }
        val titleW = DimensionUtils.dip2px(this, 86f)
        unifyTitleW(mBinding.includeRefundDetailAccount.tvItemTitle, titleW)
        unifyTitleW(mBinding.includeRefundDetailName.tvItemTitle, titleW)
        unifyTitleW(mBinding.includeRefundDetailUserAccount.tvItemTitle, titleW)
        unifyTitleW(mBinding.includeRefundDetailOrderNo.tvItemTitle, titleW)
        unifyTitleW(mBinding.includeRefundDetailApplyTime.tvItemTitle, titleW)
        unifyTitleW(mBinding.includeRefundDetailRefundTime.tvItemTitle, titleW)
    }

    private fun unifyTitleW(tvItemTitle: AppCompatTextView, w: Int) {
        tvItemTitle.layoutParams = tvItemTitle.layoutParams.apply {
            width = w
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}