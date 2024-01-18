package com.yunshang.haile_life.ui.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.entities.ShopActivityEntity
import com.yunshang.haile_life.databinding.DialogShopActivitiesBinding

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogShopActivitiesBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawableResource(0)
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