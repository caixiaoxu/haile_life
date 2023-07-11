package com.yunshang.haile_life.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.yunshang.haile_life.R

/**
 * Title : Gson加载工具类
 * Author: Lsy
 * Date: 2023/4/12 14:03
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object GlideUtils {

    /**
     * 加载图片，设置默认图和失败图
     */
    @JvmStatic
    fun loadImage(
        imageView: ImageView,
        url: String?,
        options: RequestOptions? = null,
        default: Int = R.mipmap.icon_default_head,
        err: Int = R.mipmap.icon_default_head
    ) {
        val build = Glide.with(imageView).load(url).placeholder(default).error(err)
        options?.let {
            build.apply(options)
        } ?: build.fitCenter()
        build.into(imageView)
    }
}