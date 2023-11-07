package com.yunshang.haile_life.utils

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.luck.picture.lib.entity.LocalMedia
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog

/**
 * Title :
 * Author: Lsy
 * Date: 2023/5/31 17:34
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object DialogUtils {

    /**
     * 显示图片选择弹窗
     */
    fun showImgSelectorDialog(
        activity: AppCompatActivity,
        maxNum: Int,
        showTake: Boolean = true,
        showAlbum: Boolean = true,
        callback: (isSuccess: Boolean, result: ArrayList<LocalMedia>?) -> Unit
    ) {
        val list = arrayListOf<SearchSelectParam>()
        if (showTake) {
            list.add(SearchSelectParam(1, StringUtils.getString(R.string.for_take)))
        }
        if (showAlbum) {
            list.add(SearchSelectParam(2, StringUtils.getString(R.string.for_album)))
        }

        CommonBottomSheetDialog.Builder("", list).apply {
            selectModel = 1
            onValueSureListener = object :
                CommonBottomSheetDialog.OnValueSureListener<SearchSelectParam> {
                override fun onValue(data: SearchSelectParam?) {
                    if (1 == data!!.id) {
                        PictureSelectUtils.takePicture(activity, callback)
                    } else {
                        PictureSelectUtils.pictureForAlbum(activity, maxNum, callback = callback)
                    }
                }
            }
        }.build().show(activity.supportFragmentManager)
    }

    /**
     * 检测权限弹窗
     */
    fun checkPermissionDialog(
        context: Context,
        manager: FragmentManager,
        permissions: Array<String>,
        msg: String,
        needSuccessCallBack: Boolean = true,
        negativeCallback: (() -> Unit)? = null,
        positiveCallback: () -> Unit
    ): Boolean =
        SystemPermissionHelper.checkPermissions(context, permissions).also { hasPermissions ->
            if (!hasPermissions) {
                CommonDialog.Builder(msg).apply {
                    setNegativeButton(StringUtils.getString(R.string.reject)) {
                        negativeCallback?.invoke()
                    }
                    setPositiveButton(StringUtils.getString(R.string.agree)) {
                        positiveCallback()
                    }
                }.build().show(manager)
            } else if (needSuccessCallBack) {
                positiveCallback()
            }
        }
}