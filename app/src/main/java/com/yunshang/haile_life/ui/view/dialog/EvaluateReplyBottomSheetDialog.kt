package com.yunshang.haile_life.ui.view.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.weight.loading.LoadDialogMgr
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.ScreenUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.EvaluateReplyListViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.EvaluateReplyEntity
import com.yunshang.haile_life.data.extend.isGreaterThan0
import com.yunshang.haile_life.databinding.DialogEvaluateReplyBottomSheetBinding
import com.yunshang.haile_life.databinding.ItemEvaluateDetailsPicBinding
import com.yunshang.haile_life.databinding.ItemEvaluateReplyBinding
import com.yunshang.haile_life.ui.activity.common.PicBrowseActivity
import com.yunshang.haile_life.ui.activity.order.EvaluateDetailsActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView


/**
 * Title : 日期选择Dialog
 * Author: Lsy
 * Date: 2023/4/11 09:47
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class EvaluateReplyBottomSheetDialog(private val refresh: () -> Unit) :
    BottomSheetDialogFragment() {
    private val EVALUATE_REPLY_BOTTOM_SHEET_TAG = "evaluate_reply_bottom_sheet_tag"
    private lateinit var mBinding: DialogEvaluateReplyBottomSheetBinding

    private val mViewModel by lazy {
        ViewModelProvider(
            this,
            this.defaultViewModelProviderFactory
        )[EvaluateReplyListViewModel::class.java]
    }

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemEvaluateReplyBinding, EvaluateReplyEntity>(
            R.layout.item_evaluate_reply,
            BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.tvEvaluateReplyDetails?.setOnClickListener {
                Intent(
                    requireContext(),
                    EvaluateDetailsActivity::class.java
                ).apply {
                    putExtras(
                        IntentParams.OrderEvaluateDetailsParams.pack(
                            item.id,
                        )
                    )
                }
            }
            // 图片
            mItemBinding?.glEvaluateReplyPic?.let {
                val space = DimensionUtils.dip2px(requireContext(), 14f)
                val pH = DimensionUtils.dip2px(requireContext(), 12f)
                val itemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                    requireContext(), 16f
                ) * 2 - space * 3 - pH * 2) / 4
                buildImageView(mItemBinding.glEvaluateReplyPic, item.pictures, itemWH, space)
            }
        }
    }

    private fun buildImageView(
        gridLayout: GridLayout,
        pictures: List<String>?,
        itemWH: Int,
        space: Int
    ) {
        gridLayout.removeAllViews()
        if (pictures.isNullOrEmpty()) {
            gridLayout.visibility(false)
        } else {
            val inflater = LayoutInflater.from(requireContext())
            val columnCount = gridLayout.columnCount
            pictures.forEachIndexed { index, url ->
                gridLayout.addView(
                    DataBindingUtil.inflate<ItemEvaluateDetailsPicBinding>(
                        inflater,
                        R.layout.item_evaluate_details_pic,
                        null,
                        false
                    ).apply {
                        ivEvaluateDetailsPic.loadImage(imgHeadUrl = url)
                        root.setOnClickListener {
                            startActivity(
                                Intent(
                                    requireContext(),
                                    PicBrowseActivity::class.java
                                ).apply {
                                    putExtras(IntentParams.PicParams.pack(url))
                                })
                        }
                    }.root, GridLayout.LayoutParams().apply {
                        width = itemWH
                        height = itemWH
                        setMargins(
                            if (0 != (index % columnCount)) space else 0,
                            if ((index / columnCount) > 0) space else 0,
                            0,
                            0
                        )
                    }
                )
            }
            gridLayout.visibility(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CommonBottomSheetDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            if (this is BottomSheetDialog) {
                setOnShowListener {
                    behavior.isHideable = false
                    // dialog上还有一层viewGroup，需要设置成透明
                    (requireView().parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogEvaluateReplyBottomSheetBinding.inflate(inflater, container, false)
        initEvent()
        return mBinding.root
    }

    private fun initEvent() {
        // 请求等待条
        mViewModel.loadingStatus.observe(viewLifecycleOwner) {
            if (it) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        mViewModel.replayNum.observe(viewLifecycleOwner) {
            mBinding.tvEvaluateReplyDialogTitle.text =
                "商家回复${if (it.isGreaterThan0()) "·$it" else ""}"
        }

        LiveDataBus.with(BusEvents.EVALUATE_REPLY_STATUS)?.observe(this) {
            mViewModel.requestReplyNumAsync()
        }
    }

    /**
     * 显示加载界面
     */
    private fun showLoading() {
        LoadDialogMgr.showLoadingDialog(childFragmentManager)
    }

    /**
     * 隐藏加载界面
     */
    private fun hideLoading() {
        LoadDialogMgr.hideLoadingDialog(childFragmentManager)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvEvaluateReplyDialogClose.setOnClickListener {
            dismiss()
        }

        mBinding.ibEvaluateReplyClear.setOnClickListener {
            mViewModel.submitEvaluateViewAllReply() {
                refresh()
                dismiss()
            }
        }

        mBinding.rvEvaluateReplyDialogList.layoutManager = LinearLayoutManager(requireContext())
        mBinding.rvEvaluateReplyDialogList.adapter = mAdapter
        mBinding.rvEvaluateReplyDialogList.requestData =
            object : CommonRefreshRecyclerView.OnRequestDataListener<EvaluateReplyEntity>() {
                override fun requestData(
                    isRefresh: Boolean,
                    page: Int,
                    pageSize: Int,
                    callBack: (responseList: ResponseList<out EvaluateReplyEntity>?) -> Unit
                ) {
                    mViewModel.requestData(page, pageSize, callBack)
                }
            }

        mBinding.rvEvaluateReplyDialogList.requestRefresh()
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        show(manager, EVALUATE_REPLY_BOTTOM_SHEET_TAG)
    }
}