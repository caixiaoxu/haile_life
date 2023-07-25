package com.yunshang.haile_life.ui.view.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.lsy.framelib.utils.AppManager
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.databinding.DialogUpdateAppBinding
import kotlin.math.roundToInt

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/12 17:10
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
class UpdateAppDialog private constructor(private val builder: Builder) :
    AppCompatDialogFragment() {
    private val UPDATE_TAG = "update_tag"
    private lateinit var mBinding: DialogUpdateAppBinding

    // 权限
    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                startUpdate()
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
        mBinding = DialogUpdateAppBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        //宽高
        dialog?.window?.setLayout(
            resources.getDimensionPixelOffset(R.dimen.common_dialog_w),
            resources.getDimensionPixelOffset(R.dimen.update_dialog_h)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvUpdateAppVersion.text = builder.appVersion.versionName
        mBinding.tvUpdateAppContent.text = builder.appVersion.updateLog

        mBinding.btnUpdateAppYes.setOnClickListener {
            requestPermissions.launch(
                SystemPermissionHelper.readWritePermissions()
                    .plus(SystemPermissionHelper.installPackagesPermissions())
            )
        }
    }

    private fun startUpdate() {
        showProgress(true)
        builder.positiveClickListener?.invoke { curSize, totalSize, result ->
            when (result) {
                -1 -> showProgress(false)
                1 -> if (builder.appVersion.forceUpdate) {
                    AppManager.finishAllActivity()
                } else {
                    dismiss()
                }
                else -> updateProgress(curSize, totalSize)
            }
        }
    }

    fun showProgress(isShow: Boolean) {
        mBinding.btnUpdateAppYes.visibility = View.GONE
        mBinding.groupProgress.visibility = View.VISIBLE
        if (isShow) {
            mBinding.tvUpdateProgressValue.setText(R.string.in_prepare)
        }
    }

    fun updateProgress(curSize: Long, totalSize: Long) {
        mBinding.pbUpdateProgress.progress = ((curSize * 100) / totalSize).toInt()
        mBinding.tvUpdateProgressValue.text =
            "${(curSize * 1.0 / 1024 / 1024).roundToInt()}Mb/${(totalSize * 1.0 / 1024 / 1024).roundToInt()}Mb"
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        isCancelable = !builder.appVersion.forceUpdate
        show(manager, UPDATE_TAG)
    }

    internal class Builder(val appVersion: AppVersionEntity) {

        // 同意按钮文本
        var positiveClickListener: ((refreshView: (curSize: Long, totalSize: Long, result: Int) -> Unit) -> Unit)? =
            null

        /**
         * 构建
         */
        fun build(): UpdateAppDialog = UpdateAppDialog(this)
    }
}