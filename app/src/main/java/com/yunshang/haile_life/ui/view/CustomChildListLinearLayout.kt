package com.yunshang.haile_life.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.yunshang.haile_life.R

/**
 * Title : 适用于代码添加入自定义布局的LinearLayout
 * Author: Lsy
 * Date: 2023/4/21 13:19
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CustomChildListLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayoutCompat(context, attrs) {
    var layoutId: Int = -1//child布局

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CustomChildListLinearLayout)
        layoutId = array.getResourceId(R.styleable.CustomChildListLinearLayout_itemLayout, -1)
        array.recycle()
    }

    /**
     * 构建子布局
     */
    fun <B : ViewDataBinding, D> buildChild(
        itemList: List<D>?,
        layoutParams: LayoutParams? = null,
        start: Int? = null,
        onChildBinding: (index: Int, childBinding: B, data: D) -> Unit
    ) {
        if (-1 == layoutId) {
            return
        }
        if (itemList.isNullOrEmpty()) {
            visibility = GONE
            return
        }
        visibility = VISIBLE

        start?.let {
            if (childCount > start) {
                removeViews(it, childCount - start)
            }
        } ?: removeAllViews()
        val inflater = LayoutInflater.from(context)
        itemList.forEachIndexed { index, any ->
            val mBinding = DataBindingUtil.inflate<B>(inflater, layoutId, null, false)
            onChildBinding.invoke(index, mBinding, any)
            addView(
                mBinding.root,
                layoutParams ?: LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}