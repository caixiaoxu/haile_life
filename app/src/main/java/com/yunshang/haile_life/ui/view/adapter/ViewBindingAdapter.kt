package com.yunshang.haile_life.ui.view.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.Group
import androidx.core.view.MarginLayoutParamsCompat
import androidx.databinding.BindingAdapter
import com.yunshang.haile_life.ui.view.CommonTitleActionBar
import com.yunshang.haile_life.ui.view.MultiTypeTextView
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView
import com.yunshang.haile_life.utils.GlideUtils
import kotlin.math.abs

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/10 12:22
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object ViewBindingAdapter {
    @BindingAdapter("title")
    @JvmStatic
    fun CommonTitleActionBar.setAttr(title: String?) {
        setTitle(title)
    }

    /**
     * 显示标题
     */
    @BindingAdapter("title")
    @JvmStatic
    fun CommonTitleActionBar.setTitle(title: String?) {
        getTitle().text = title ?: ""
    }

    /**
     * 显示控件
     */
    @BindingAdapter("visibility")
    @JvmStatic
    fun View.setVisibility(isShow: Boolean?) {
        visibility = if (true == isShow) View.VISIBLE else View.GONE
    }

    /**
     * TextView属性
     */
    @BindingAdapter(
        "drawStart",
        "drawTop",
        "drawEnd",
        "drawBottom",
        "tRes",
        "tMoney",
        "marginStart",
        "txtSize",
        "txtColor",
        requireAll = false
    )
    @JvmStatic
    fun TextView.setTVAttr(
        dsRes: Int?,
        dtRes: Int?,
        deRes: Int?,
        dbRes: Int?,
        tRes: Int?,
        tMoney: Double?,
        mS: Float?,
        txtSize: Float?,
        txtColor: Int?,
    ) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
            dsRes ?: 0,
            dtRes ?: 0,
            deRes ?: 0,
            dbRes ?: 0
        )
        tRes?.let {
            setText(tRes)
        }
        tMoney?.let {
            text = "${if (tMoney < 0) "-" else ""}￥ ${abs(tMoney)}"
        }
        mS?.let {
            if (layoutParams is ViewGroup.MarginLayoutParams)
                MarginLayoutParamsCompat.setMarginStart(
                    layoutParams as ViewGroup.MarginLayoutParams,
                    mS.toInt()
                )
        }
        txtSize?.let {
            textSize = it
        }
        txtColor?.let {
            setTextColor(it)
        }
    }

    @BindingAdapter("bgResIds", "txtColors", "type", requireAll = false)
    @JvmStatic
    fun MultiTypeTextView.setBgResIds(bgs: IntArray?, colors: IntArray?, type: Int?) {
        bgs?.let {
            bgResIds = it
        }
        colors?.let {
            txtColors = it
        }
        type?.let {
            this.type = it
        }
    }

    /**
     * ImageView 加载图片
     */
    @BindingAdapter("imgRes", "imgUrl", requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(res: Int?, url: String?) {
        res?.let {
            setImageResource(res)
        }
        url?.let {
            GlideUtils.loadImage(this, url)
        }
    }
}