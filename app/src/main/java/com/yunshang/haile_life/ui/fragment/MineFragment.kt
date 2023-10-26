package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import android.net.Uri
import androidx.constraintlayout.widget.ConstraintLayout
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.ViewUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.MineViewModel
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.FragmentMineBinding
import com.yunshang.haile_life.ui.activity.order.OrderListActivity
import com.yunshang.haile_life.ui.activity.personal.DiscountCouponActivity
import com.yunshang.haile_life.ui.activity.personal.SettingActivity
import com.yunshang.haile_life.ui.activity.personal.WalletBalanceActivity
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

        mBinding.ibMineSetting.setOnClickListener {
            startActivity(Intent(requireContext(), SettingActivity::class.java))
        }

        mBinding.tvMineBalance.setOnClickListener {
            goWalletBalance()
        }
        mBinding.tvMineBalanceTitle.setOnClickListener {
            goWalletBalance()
        }
        mBinding.tvFunServiceWalletBalance.setOnClickListener {
            goWalletBalance()
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

        mBinding.tvMineDiscounts.setOnClickListener {
            goDiscountCouponList()
        }
        mBinding.tvMineDiscountsTitle.setOnClickListener {
            goDiscountCouponList()
        }
        mBinding.tvFunServiceWalletDiscounts.setOnClickListener {
            goDiscountCouponList()
        }

        mBinding.tvFunServiceService.setOnClickListener {
            if (!ViewUtils.isFastDoubleClick()) {
                // 调用系统浏览器
                val uri: Uri = Uri.parse(Constants.SERVICE_URL)
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)

//                startActivity(Intent(requireContext(), WebViewActivity::class.java).apply {
//                    putExtras(
//                        IntentParams.WebViewParams.pack(
//                            Constants.SERVICE_URL,
//                            noCache = true
//                        )
//                    )
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                })
            }
        }

        mBinding.btnMineRecharge.setOnClickListener {
            startActivity(Intent(requireContext(), NearByShopActivity::class.java).apply {
                putExtras(IntentParams.NearByShopParams.pack(true))
            })
        }

        mBinding.clMineOrder.setOnClickListener {
            goOrderList()
        }
        mBinding.tvMineOrderUnpaid.setOnClickListener {
            goOrderList(100)
        }
        mBinding.tvMineOrderRunning.setOnClickListener {
            goOrderList(500)
        }
        mBinding.tvMineOrderFinished.setOnClickListener {
            goOrderList(1000)
        }
        mBinding.tvMineOrderRefund.setOnClickListener {
            goOrderList(2099)
        }
    }

    private fun goWalletBalance() {
        mViewModel.balance.value?.let {
            startActivity(Intent(requireContext(), WalletBalanceActivity::class.java).apply {
                putExtras(IntentParams.WalletBalanceParams.pack(it))
            })
        }
    }

    private fun goRechargeStarfishShopList() {
        startActivity(Intent(requireContext(), RechargeStarfishShopListActivity::class.java))
    }

    private fun goDiscountCouponList() {
        startActivity(Intent(requireContext(), DiscountCouponActivity::class.java))
    }

    private fun goOrderList(status: Int? = null) {
        startActivity(
            Intent(requireContext(), OrderListActivity::class.java).apply {
                putExtras(IntentParams.OrderListParams.pack(status = status))
            }
        )
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