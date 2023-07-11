package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import androidx.constraintlayout.widget.ConstraintLayout
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.MineViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.FragmentMineBinding
import com.yunshang.haile_life.ui.activity.shop.NearByShopActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishShopListActivity

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/7 13:58
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class MineFragment : BaseBusinessFragment<FragmentMineBinding, MineViewModel>(
    MineViewModel::class.java, BR.vm
) {

    override fun layoutId(): Int = R.layout.fragment_mine

    override fun initEvent() {
        super.initEvent()
    }

    override fun initView() {
        //设置顶部距离
        mBinding.ibMineSetting.layoutParams.let {
            (it as ConstraintLayout.LayoutParams).topMargin =
                StatusBarUtils.getStatusBarHeight() + DimensionUtils.dip2px(requireContext(), 20f)
        }

        mBinding.tvMineHaixin.setOnClickListener {
            goRechargeStarfishShopList()
        }
        mBinding.tvMineHaixinTitle.setOnClickListener {
            goRechargeStarfishShopList()
        }
        mBinding.tvFunServiceWalletHaixin.setOnClickListener {
            goRechargeStarfishShopList()
        }

        mBinding.ivMineRecharge.setOnClickListener {
            startActivity(Intent(requireContext(), NearByShopActivity::class.java).apply {
                putExtras(IntentParams.NearByShopParams.pack(true))
            })
        }
    }

    private fun goRechargeStarfishShopList() {
        startActivity(Intent(requireContext(), RechargeStarfishShopListActivity::class.java))
    }

    override fun initData() {
        mBinding.shared = mSharedViewModel
        mViewModel.requestData {
            if (null == mSharedViewModel.userInfo.value) {
                mSharedViewModel.requestUserInfo()
            }
        }
    }
}