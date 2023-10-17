package com.yunshang.haile_life.ui.view.adapter

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.request.RequestOptions
import com.yunshang.haile_life.ui.view.CommonTitleActionBar
import com.yunshang.haile_life.ui.view.MultiTypeTextView
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

    @BindingAdapter("width", "height")
    @JvmStatic
    fun View.setAttr(width: Int?, height: Int?) {
        width?.let {
            layoutParams?.width = width
        }
        height?.let {
            layoutParams?.height = height
        }
    }

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
        "drawStart", "drawTop", "drawEnd", "drawBottom",
        "drawSD", "drawTD", "drawED", "drawBD",
        "paddingTS", "paddingTT", "paddingTE", "paddingTB",
        "tRes",
        "tMoney",
        "marginStart",
        "txtSize",
        "txtColor",
        "txtStateColor",
        "txtStyle",
        "txtBgRes",
        requireAll = false
    )
    @JvmStatic
    fun TextView.setTVAttr(
        dsRes: Int = 0,
        dtRes: Int = 0,
        deRes: Int = 0,
        dbRes: Int = 0,
        dsDraw: Drawable?,
        dtDraw: Drawable?,
        deDraw: Drawable?,
        dbDraw: Drawable?,
        pS: Float = 0f,
        pT: Float = 0f,
        pE: Float = 0f,
        pB: Float = 0f,
        tRes: Int?,
        tMoney: Double?,
        mS: Float?,
        txtSize: Float?,
        txtColor: Int?,
        txtStateColor: Int?,
        txtStyle: Int?,
        txtBgRes: Int?
    ) {

        if (dsRes != 0 || dtRes != 0 || deRes != 0 || dbRes != 0) {
            setCompoundDrawablesWithIntrinsicBounds(dsRes, dtRes, deRes, dbRes)
        }

        if (null != dsDraw || null != dtDraw || null != deDraw || null != dbDraw) {
            setCompoundDrawablesWithIntrinsicBounds(dsDraw, dtDraw, deDraw, dbDraw)
        }
        if (pS != 0f || pT != 0f || pE != 0f || pB != 0f) {
            setPadding(pS.toInt(), pT.toInt(), pE.toInt(), pB.toInt())
        }
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
        txtStateColor?.let {
            setTextColor(ResourcesCompat.getColorStateList(resources, it, null))
        }
        txtStyle?.let {
            typeface = when (it) {
                1 -> Typeface.DEFAULT_BOLD
                2 -> Typeface.defaultFromStyle(Typeface.ITALIC)
                else -> Typeface.defaultFromStyle(Typeface.NORMAL)
            }
        }
        txtBgRes?.let {
            setBackgroundResource(txtBgRes)
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
    @BindingAdapter("imgRes", "imgUrl", "imgHeadUrl", requireAll = false)
    @JvmStatic
    fun ImageView.loadImage(res: Int?, url: String?, imgHeadUrl: String?) {
        res?.let {
            setImageResource(res)
        }
        url?.let {
            GlideUtils.loadImage(this, url)
        }
        imgHeadUrl?.let {
            GlideUtils.loadImage(this, imgHeadUrl, RequestOptions.centerCropTransform())
        }
    }

    /**
     * LinearLayoutCompat 属性
     */
    @BindingAdapter("paddingS", "paddingT", "paddingE", "paddingB", requireAll = false)
    @JvmStatic
    fun LinearLayoutCompat.setLLCAttr(
        pS: Float = 0f,
        pT: Float = 0f,
        pE: Float = 0f,
        pB: Float = 0f
    ) {
        if (pS != 0f || pT != 0f || pE != 0f || pB != 0f) {
            setPadding(pS.toInt(), pT.toInt(), pE.toInt(), pB.toInt())
        }
    }
}