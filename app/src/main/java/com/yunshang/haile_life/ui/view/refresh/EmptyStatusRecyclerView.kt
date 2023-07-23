package com.yunshang.haile_life.ui.view.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.LayoutEmptyRecyclerViewBinding

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/23 10:46
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class EmptyStatusRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val mBinding: LayoutEmptyRecyclerViewBinding

    // 空状态图
    var listStatusImgResId: Int = R.mipmap.icon_list_content_empty

    // 空状态图
    var listStatusTxtResId: Int = R.string.empty_content

    init {
        mBinding = LayoutEmptyRecyclerViewBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.layout_empty_recycler_view, this, true)
        )
        refreshStatusView()

        val array = context.obtainStyledAttributes(attrs, R.styleable.EmptyStatusRecyclerView)
        listStatusImgResId = array.getResourceId(
            R.styleable.EmptyStatusRecyclerView_emptyImgRes,
            R.mipmap.icon_list_content_empty
        )
        listStatusTxtResId = array.getResourceId(
            R.styleable.EmptyStatusRecyclerView_emptyTxtRes,
            R.string.empty_content
        )
        array.recycle()
    }

    val recyclerView: RecyclerView
        get() = mBinding.rvRefreshList

    fun setListStatus(imgResId: Int? = null, txtResId: Int? = null) {
        imgResId?.let {
            listStatusImgResId = it
        }
        txtResId?.let {
            listStatusTxtResId = it
        }

        if (null != imgResId && null != txtResId) {
            refreshStatusView()
        }
    }

    private fun refreshStatusView() {
        mBinding.tvListStatus.setText(listStatusTxtResId)
        mBinding.tvListStatus.setCompoundDrawablesWithIntrinsicBounds(
            0,
            listStatusImgResId,
            0,
            0
        )
    }

    /**
     * 切换状态界面
     */
    fun changeStatusView(isEmpty: Boolean) {
        mBinding.rvRefreshList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        mBinding.tvListStatus.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }
}