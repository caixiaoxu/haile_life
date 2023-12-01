package com.yunshang.haile_life.ui.view.dialog

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.entities.ShopNoticeEntity
import com.yunshang.haile_life.databinding.DialogShopNoticeBinding
import com.yunshang.haile_life.databinding.ItemDialogShopNoticeBinding
import com.yunshang.haile_life.utils.string.StringUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/17 11:39
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class ShopNoticeDialog(
    private val noticeList: MutableList<ShopNoticeEntity>,
    private val canCancel: Boolean = true,
    private val callback: (() -> Unit)? = null
) :
    AppCompatDialogFragment() {
    private val SHOP_NOTICE_TAG = "shop_notice_tag"
    private lateinit var mBinding: DialogShopNoticeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogShopNoticeBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.llShopNoticeContentList.buildChild<ItemDialogShopNoticeBinding, ShopNoticeEntity>(
            noticeList
        ) { _, childBinding, data ->
            childBinding.item = data
            StringUtils.loadHtmlText(childBinding.tvShopNoticeContent, data.templateContent)
        }

        mBinding.btnShopNoticeIKnow.setOnClickListener {
            dismiss()
            callback?.invoke()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        StringUtils.clearHtmlCaches()
    }

    override fun onResume() {
        super.onResume()
        //宽高
        dialog?.window?.setLayout(
            resources.getDimensionPixelOffset(R.dimen.common_dialog_w),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        isCancelable = canCancel
        show(manager, SHOP_NOTICE_TAG)
    }
}