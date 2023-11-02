package com.yunshang.haile_life.ui.view.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.CustomRefreshRecyclerViewBinding
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/14 16:29
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CommonLoadMoreRecyclerView<D> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val mBinding: CustomRefreshRecyclerViewBinding

    // 排版布局
    var layoutManager: LayoutManager? = null
        set(value) {
            mBinding.rvRefreshList.recyclerView.layoutManager = value
            field = value
        }

    // 适配器
    var adapter: CommonRecyclerAdapter<*, D>? = null
        set(value) {
            mBinding.rvRefreshList.recyclerView.adapter = value
            field = value
        }

    /**
     * 分割线
     */
    fun addItemDecoration(decor: ItemDecoration) {
        mBinding.rvRefreshList.recyclerView.addItemDecoration(decor)
    }

    // 页数
    var page: Int = 1

    // 默认页数
    var pageSize: Int = 20

    // 请求数据
    var requestData: OnRequestDataListener<D>? = null

    // 是否启用空状态
    var enableEmptyStatus: Boolean = false

    init {
        mBinding = CustomRefreshRecyclerViewBinding.bind(
            LayoutInflater.from(context).inflate(R.layout.custom_refresh_recycler_view, null, false)
        )
        addView(mBinding.root)

        val array = context.obtainStyledAttributes(attrs, R.styleable.CommonRefreshRecyclerView)
        enableEmptyStatus = array.getBoolean(
            R.styleable.CommonRefreshRecyclerView_enableEmptyStatus, false
        )
        setListStatus(
            array.getResourceId(
                R.styleable.CommonRefreshRecyclerView_emptyImgRes,
                R.mipmap.icon_list_content_empty
            ), array.getResourceId(
                R.styleable.CommonRefreshRecyclerView_emptyTxtRes,
                R.string.empty_content
            )
        )
        array.recycle()

        mBinding.refreshLayout.setEnableRefresh(false)

        // 加载
        mBinding.refreshLayout.setOnLoadMoreListener {
            requestData(false)
        }
    }

    /**
     * 设置空值
     */
    fun setListStatus(imgResId: Int? = null, txtResId: Int? = null) {
        mBinding.rvRefreshList.setListStatus(imgResId, txtResId)
    }

    /**
     * 请求数据
     */
    private fun requestData(isRefresh: Boolean) {
        if (isRefresh) page = 1
        requestData?.requestData(page, pageSize) {
            onLoadMore(it, isRefresh)
        }
    }

    /**
     * 加载
     * @param isRefresh 是否重新加载
     * @param isLoadMoreLayout 是否需要加载界面
     */
    fun requestLoadMore(isRefresh: Boolean = false, isLoadMoreLayout: Boolean = true) {
        if (isRefresh) {
            mBinding.refreshLayout.setEnableLoadMore(true)
            requestData(true)
        } else {
            if (isLoadMoreLayout) {
                mBinding.refreshLayout.autoLoadMore()
            } else {
                requestData(false)
            }
        }
    }

    /**
     * 加载数据
     */
    private fun onLoadMore(list: MutableList<out D>?, isRefresh: Boolean) {
        mBinding.refreshLayout.finishLoadMore()
        list?.let {
            refreshDate(it, isRefresh)
        }
    }

    /**
     * 刷新数据
     * @param it 数据列表
     */
    private fun refreshDate(it: MutableList<out D>, isRefresh: Boolean) {
        //判断 当前页 数量不为0，页数加1
        if (0 < it.size) {
            page++
        }

        // 自定义处理
        if (true == requestData?.onLoadMore(it)) {
            return
        }

        // 显示空状态
        if (enableEmptyStatus) {
            mBinding.rvRefreshList.changeStatusView(isRefresh && 0 == it.size)
        }

        // 刷新数据
        adapter?.refreshList(it, isRefresh)

        if (it.size <= pageSize)
            mBinding.refreshLayout.setEnableLoadMore(false)
    }

    /**
     * 请求监听
     */
    abstract class OnRequestDataListener<D> {
        /**
         * 请示数据
         * @param page 页数
         * @param pageSize 请求数量
         * @param callBack 请求回调
         */
        abstract fun requestData(
            page: Int,
            pageSize: Int,
            callBack: (responseList: MutableList<out D>?) -> Unit
        )

        /**
         * 加载数据
         * @param responseList 返回的列表数据
         * @return 是否拦截后续操作
         */
        fun onLoadMore(responseList: MutableList<out D>): Boolean = false
    }
}