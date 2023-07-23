package com.yunshang.haile_life.ui.view.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.CustomRefreshRecyclerViewBinding
import com.yunshang.haile_life.ui.view.adapter.BaseRecyclerAdapter

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
class CommonRefreshRecyclerView<D> @JvmOverloads constructor(
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
    var adapter: BaseRecyclerAdapter<D>? = null
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

    // 禁用/启动刷新
    var enableRefresh: Boolean = true
        set(value) {
            field = value
            mBinding.refreshLayout.setEnableRefresh(value)
        }

    // 禁用/启动加载
    var enableLoadMore: Boolean = true
        set(value) {
            field = value
            mBinding.refreshLayout.setEnableLoadMore(value)
        }

    var enableEmptyStatus: Boolean = false

    // 页数
    var page: Int = 1

    // 默认页数
    var pageSize: Int = 20

    // 请求数据
    var requestData: OnRequestDataListener<D>? = null

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

        // 刷新
        mBinding.refreshLayout.setOnRefreshListener {
            requestData(true)
        }

        // 加载
        mBinding.refreshLayout.setOnLoadMoreListener {
            requestData(false)
        }
    }

    /**
     * 设置列表空值
     */
    fun setListStatus(imgResId: Int? = null, txtResId: Int? = null) {
        enableEmptyStatus = true
        mBinding.rvRefreshList.setListStatus(imgResId, txtResId)
    }

    /**
     * 请求数据
     */
    private fun requestData(isRefresh: Boolean) {
        if (isRefresh) page = 1
        requestData?.requestData(isRefresh, page, pageSize) {
            if (isRefresh) {
                onRefresh(it)
            } else
                onLoadMore(it)
        }
    }

    /**
     * 刷新
     * @param isRefreshLayout 是否需要刷新界面
     */
    fun requestRefresh(isRefreshLayout: Boolean = false) {
        if (isRefreshLayout && enableRefresh) {
            mBinding.refreshLayout.autoRefresh()
        } else {
            requestData(true)
        }
    }

    /**
     * 加载
     * @param isLoadMoreLayout 是否需要加载界面
     */
    fun requestLoadMore(isLoadMoreLayout: Boolean = true) {
        if (isLoadMoreLayout && enableLoadMore) {
            mBinding.refreshLayout.autoLoadMore()
        } else {
            requestData(false)
        }
    }

    /**
     * 刷新数据
     */
    private fun onRefresh(list: ResponseList<out D>?) {
        mBinding.refreshLayout.finishRefresh()
        list?.let {
            refreshDate(it, true)
        }
    }

    /**
     * 加载数据
     */
    private fun onLoadMore(list: ResponseList<out D>?) {
        mBinding.refreshLayout.finishLoadMore()
        list?.let {
            refreshDate(it)
        }
    }

    /**
     * 刷新数据
     * @param it 数据列表
     * @param isRefresh 是否刷新
     */
    private fun refreshDate(it: ResponseList<out D>, isRefresh: Boolean = false) {
        //判断 当前页 数量不为0，页数加1
        if (0 < it.pageSize) {
            page++
        }

        // 自定义处理
        if (true == requestData?.onCommonDeal(it, isRefresh)
            || true == if (isRefresh) requestData?.onRefresh(it) else requestData?.onLoadMore(it)
        ) {
            return
        }

        // 显示空状态
        if (enableEmptyStatus) {
            mBinding.rvRefreshList.changeStatusView(isRefresh && 0 == it.total)
        }

        // 刷新数据
        adapter?.refreshList(it.items, isRefresh)

        // 判断列表数量 >= 总数据数量
        if ((adapter?.itemCount ?: 0) >= it.total) {
            mBinding.refreshLayout.setEnableLoadMore(false)
        }
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
            isRefresh: Boolean,
            page: Int,
            pageSize: Int,
            callBack: (responseList: ResponseList<out D>?) -> Unit
        )

        /**
         * 公共处理事件
         * @param responseList 返回的列表数据
         * @return 是否拦截后续操作
         */
        open fun onCommonDeal(responseList: ResponseList<out D>, isRefresh: Boolean): Boolean =
            false

        /**
         * 刷新数据
         * @param responseList 返回的列表数据
         * @return 是否拦截后续操作
         */
        open fun onRefresh(responseList: ResponseList<out D>): Boolean = false

        /**
         * 加载数据
         * @param responseList 返回的列表数据
         * @return 是否拦截后续操作
         */
        open fun onLoadMore(responseList: ResponseList<out D>): Boolean = false
    }
}