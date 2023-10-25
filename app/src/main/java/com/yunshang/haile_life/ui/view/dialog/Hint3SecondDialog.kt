package com.yunshang.haile_life.ui.view.dialog

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.DialogHint3SecondBinding

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
class Hint3SecondDialog private constructor(private val builder: Builder) :
    AppCompatDialogFragment() {
    private val HINT_3S_TAG = "hint_3s_tag"
    private lateinit var mBinding: DialogHint3SecondBinding

    private val countDownTimer by lazy {
        object : CountDownTimer(3 * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                mBinding.btnHint3sDialogYes.text =
                    "${StringUtils.getString(R.string.i_know)}（${millisUntilFinished / 1000 + 1}s）"
            }

            override fun onFinish() {
                dismiss()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogHint3SecondBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_sffffff_r8)
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

        //内容
        mBinding.tvHint3sDialogContent.text = builder.msg

        mBinding.btnHint3sDialogYes.setOnClickListener {
            dismiss()
        }

        countDownTimer.start()
    }

    override fun onDestroyView() {
        countDownTimer.cancel()
        super.onDestroyView()
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        isCancelable = true
        show(manager, HINT_3S_TAG)
    }

    internal class Builder(val msg: CharSequence) {

        /**
         * 构建
         */
        fun build(): Hint3SecondDialog = Hint3SecondDialog(this)
    }
}