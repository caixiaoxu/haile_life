package com.yunshang.haile_life.ui.view.dialog

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.entities.OfficialAccountsEntity
import com.yunshang.haile_life.databinding.DialogOfficialAccountsBinding
import com.yunshang.haile_life.utils.BitmapUtils
import com.yunshang.haile_life.utils.DialogUtils
import com.yunshang.haile_life.utils.thrid.WeChatHelper

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/16 14:28
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OfficialAccountsDialog(private val officialAccounts: OfficialAccountsEntity) :
    AppCompatDialogFragment() {
    private var bitmap: Bitmap? = null
    private lateinit var mBinding: DialogOfficialAccountsBinding

    // 权限
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                save()
            } else {
                // 授权失败
                SToast.showToast(requireContext(), R.string.empty_permission)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogOfficialAccountsBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_sffffff_r8)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ibOfficialAccountsClose.setOnClickListener {
            dismiss()
        }

        // 生成二维码
        val wh = DimensionUtils.dip2px(requireContext(), 137f)
        BitmapUtils.createQRCodeBitmap(
            officialAccounts.url, wh, wh, "UTF-8", "H", "1",
            Color.BLACK, Color.WHITE
        )?.let { bitmap ->
            this.bitmap = bitmap
            mBinding.ivOfficialAccountsScanCode.setImageBitmap(bitmap)
            //点击保存
            mBinding.btnOfficialAccountsSaveQr.setOnClickListener {
                DialogUtils.checkPermissionDialog(
                    requireContext(),
                    childFragmentManager,
                    SystemPermissionHelper.readWritePermissions(),
                    "需要读写权限来保存二维码"
                ) {
                    requestPermissions.launch(SystemPermissionHelper.readWritePermissions())
                }
            }
        }

        mBinding.btnOfficialAccountsOpenMini.setOnClickListener {
            // 跳转小程序
            WeChatHelper.openWeChatMiniProgram("pages/tabbar/home", null, "gh_102c08f8d7a4")
            dismiss()
        }
    }

    /**
     * 保存
     */
    private fun save() {
        bitmap?.let {
            if (BitmapUtils.saveBitmapToGallery(requireContext(), "official_accounts.jpg", it)) {
                SToast.showToast(requireContext(), "保存成功")
                dismiss()
            } else {
                SToast.showToast(requireContext(), "保存失败")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        //宽高
        dialog?.window?.setLayout(
            resources.getDimensionPixelOffset(R.dimen.common_dialog_w),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}