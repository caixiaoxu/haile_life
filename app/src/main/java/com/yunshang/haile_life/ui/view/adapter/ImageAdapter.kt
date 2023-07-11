package com.yunshang.haile_life.ui.view.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lsy.framelib.utils.DimensionUtils
import com.youth.banner.adapter.BannerAdapter
import com.yunshang.haile_life.ui.view.RoundImageView
import kotlinx.coroutines.flow.callbackFlow


/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/6 18:11
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class ImageAdapter<T>(
    mDatas: List<T>,
    private val imageUrl: (data: T) -> String,
    private val callback: ((data: T, pos: Int) -> Unit)? = null
) :
    BannerAdapter<T, ImageAdapter.BannerViewHolder>(mDatas) {

    override fun onCreateHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        return BannerViewHolder(RoundImageView(parent.context).apply {
            radius = DimensionUtils.dip2px(parent.context, 8f).toFloat()
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        })
    }

    override fun onBindView(holder: BannerViewHolder, data: T, position: Int, size: Int) {
        Glide.with(holder.imageView)
            .load(imageUrl(data))
            .into(holder.imageView)
        holder.imageView.setOnClickListener {
            callback?.invoke(data, position)
        }
    }

    class BannerViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView) {}
}