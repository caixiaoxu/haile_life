package com.yunshang.haile_life.ui.activity.device

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.CardManagerViewModel
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.data.entities.CardEntity
import com.yunshang.haile_life.databinding.ActivityCardManagerBinding
import com.yunshang.haile_life.databinding.ItemCardManagerBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView

class CardManagerActivity : BaseBusinessActivity<ActivityCardManagerBinding, CardManagerViewModel>(
    CardManagerViewModel::class.java
) {
    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemCardManagerBinding, CardEntity>(
            R.layout.item_card_manager,
            BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                CommonBottomSheetDialog.Builder(
                    "选择操作", listOf(SearchSelectParam(0, "解除绑定"))
                ).apply {
                    selectModel = 1
                    onValueSureListener = object :
                        CommonBottomSheetDialog.OnValueSureListener<SearchSelectParam> {
                        override fun onValue(data: SearchSelectParam?) {
                            CommonDialog.Builder(
                                "解绑后卡不可使用"
                            ).apply {
                                title = "是否确认解绑"
                                negativeTxt = StringUtils.getString(R.string.cancel)
                                setPositiveButton(StringUtils.getString(R.string.sure)) {
                                    mViewModel.unBindCard(item.cardSn) {
                                        SToast.showToast(this@CardManagerActivity, "解绑成功")
                                        mBinding.rvCardManagerList.requestRefresh(true)
                                    }
                                }
                            }.build().show(supportFragmentManager)
                        }
                    }

                }.build().show(supportFragmentManager)
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_card_manager

    override fun backBtn(): View = mBinding.barCardManagerTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()

        LiveDataBus.with(BusEvents.CARD_BINDING_STATUS)?.observe(this) {
            mBinding.rvCardManagerList.requestRefresh(true)
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.rvCardManagerList.layoutManager = LinearLayoutManager(this)
        ContextCompat.getDrawable(this, R.drawable.divide_size12)?.let {
            mBinding.rvCardManagerList.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.rvCardManagerList.adapter = mAdapter
        mBinding.rvCardManagerList.enableRefresh = false
        mBinding.rvCardManagerList.enableLoadMore = false
        mBinding.rvCardManagerList.requestData = object :
            CommonRefreshRecyclerView.OnRequestDataListener<CardEntity>() {
            override fun requestData(
                isRefresh: Boolean,
                page: Int,
                pageSize: Int,
                callBack: (responseList: ResponseList<out CardEntity>?) -> Unit
            ) {
                mViewModel.requestCardList(page, pageSize, callBack)
            }

            override fun onCommonDeal(
                responseList: ResponseList<out CardEntity>,
                isRefresh: Boolean
            ): Boolean {
                mBinding.rvCardManagerList.visibility =
                    if (0 == responseList.total) View.GONE else View.VISIBLE
                return super.onCommonDeal(responseList, isRefresh)
            }
        }

        mBinding.btnCardManagerAdd.setOnClickListener {
            startActivity(Intent(this, CardBindingActivity::class.java))
        }
    }

    override fun initData() {
        mBinding.rvCardManagerList.requestRefresh(true)
    }
}