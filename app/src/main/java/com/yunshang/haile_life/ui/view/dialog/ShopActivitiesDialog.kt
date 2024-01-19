package com.yunshang.haile_life.ui.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.lsy.framelib.ui.weight.loading.LoadDialogMgr
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.CouponDTOS
import com.yunshang.haile_life.data.entities.ShopActivityEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.databinding.DialogShopActivitiesBinding
import com.yunshang.haile_life.databinding.ItemDialogShopActivityBinding
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Title : 常用的dialog
 * Author: Lsy
 * Date: 2023/4/4 09:55
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class ShopActivitiesDialog private constructor(private val builder: Builder) :
    AppCompatDialogFragment() {
    private val SHOP_ACTIVITY_TAG = "shop_activity_tag"
    private lateinit var mBinding: DialogShopActivitiesBinding
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogShopActivitiesBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        //宽高
        dialog?.window?.setLayout(
            resources.getDimensionPixelOffset(R.dimen.common_dialog_w),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.llDialogShopActivityList.buildChild<ItemDialogShopActivityBinding, CouponDTOS>(
            builder.shopActivity.activityCouponDTOS,
            LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        ) { _, childBinding, data ->
            childBinding.child = data
        }
        mBinding.btnDialogShopActivityGet.setOnClickListener {
            getShopActivity()
        }
        refreshBottomBtns()
        mBinding.btnDialogShopActivityClose.setOnClickListener {
            dismiss()
        }
    }

    private fun refreshBottomBtns() {
        mBinding.btnDialogShopActivityGet.visibility(1 == builder.shopActivity.activitySubTypeId && 2 != builder.shopActivity.status)
        mBinding.tvDialogShopActivityGetSuccess.visibility(2 == builder.shopActivity.activitySubTypeId || 2 == builder.shopActivity.status)
    }

    private fun getShopActivity() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                LoadDialogMgr.showLoadingDialog(childFragmentManager)
            }
            try {
                ApiRepository.dealApiResult(
                    mShopRepo.executeShopActivity(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "hashKey" to builder.activityHashKey,
                                "activityExecuteNodeId" to builder.activityExecuteNodeId,
                                "orderNo" to builder.orderNo,
                                "ifCollectCoupon" to true
                            )
                        )
                    )
                )
                builder.shopActivity.status = 2
                withContext(Dispatchers.Main) {
                    refreshBottomBtns()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                withContext(Dispatchers.Main) {
                    LoadDialogMgr.hideLoadingDialog(childFragmentManager)
                }
            }
        }
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        isCancelable = true
        show(manager, SHOP_ACTIVITY_TAG)
    }

    internal class Builder(
        val shopActivity: ShopActivityEntity,
        val activityExecuteNodeId: Int,
        val activityHashKey: String? = null,
        val orderNo: String? = null,
    ) {

        /**
         * 构建
         */
        fun build(): ShopActivitiesDialog = ShopActivitiesDialog(this)
    }
}