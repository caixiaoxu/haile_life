package com.yunshang.haile_life.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.CompressFileEngine
import com.luck.picture.lib.engine.CropFileEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnKeyValueResultCallbackListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.DateUtils
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropImageEngine
import com.yunshang.haile_life.utils.engine.GlideEngine
import timber.log.Timber
import top.zibin.luban.Luban
import top.zibin.luban.OnNewCompressListener
import java.io.File

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/1 19:57
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object PictureSelectUtils {
    /**
     * 拍照
     * @param context
     * @param callback 回调
     */
    fun takePicture(
        context: Context,
        callback: (isSuccess: Boolean, result: ArrayList<LocalMedia>?) -> Unit
    ) {
        // 拍照
        PictureSelector.create(context)
            .openCamera(SelectMimeType.ofImage())//拍照类型
            .setCompressEngine(ImageCompressFileEngine())//压缩
            .setCropEngine(ImageCropFileCropEngine())//裁剪
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    result.forEachIndexed { index, localMedia ->
                        Timber.i("拍照图片${index}path输出:${localMedia.path}")
                        Timber.i("拍照图片${index}availablePath输出:${localMedia.availablePath}")
                        Timber.i("拍照图片${index}cutPath输出:${localMedia.cutPath}")
                        Timber.i("拍照图片${index}compressPath输出:${localMedia.compressPath}")
                        Timber.i("拍照图片${index}realPath输出:${localMedia.realPath}")
                    }
                    callback(true, result)
                }

                override fun onCancel() {
                    Timber.i("拍照图片输出失败")
                    callback(false, null)
                }
            })
    }

    fun pictureForAlbum(
        context: Context,
        maxNum: Int,
        callback: (isSuccess: Boolean, result: ArrayList<LocalMedia>?) -> Unit
    ) {
        // 相册
        PictureSelector.create(context)
            .openGallery(SelectMimeType.ofImage())//相册筛选类型
            .setImageEngine(GlideEngine)//加载图片引擎
            .setMaxSelectNum(maxNum)//最大选择数
            .setCompressEngine(ImageCompressFileEngine())//压缩
            .setCropEngine(ImageCropFileCropEngine())//裁剪
            .forResult(object : OnResultCallbackListener<LocalMedia> {
                override fun onResult(result: ArrayList<LocalMedia>) {
                    result.forEachIndexed { index, localMedia ->
                        Timber.i("选择图片${index}path输出:${localMedia.path}")
                        Timber.i("选择图片${index}availablePath输出:${localMedia.availablePath}")
                        Timber.i("选择图片${index}cutPath输出:${localMedia.cutPath}")
                        Timber.i("选择图片${index}compressPath输出:${localMedia.compressPath}")
                        Timber.i("选择图片${index}realPath输出:${localMedia.realPath}")
                    }
                    callback(true, result)
                }

                override fun onCancel() {
                    Timber.i("选择图片输出失败")
                    callback(false, null)
                }
            })
    }

    /**
     * 图片压缩引擎
     */
    class ImageCompressFileEngine : CompressFileEngine {
        override fun onStartCompress(
            context: Context,
            source: java.util.ArrayList<Uri>,
            call: OnKeyValueResultCallbackListener?
        ) {
            Luban.with(context).load(source).ignoreBy(10240).setRenameListener { filePath ->
                val indexOf = filePath.lastIndexOf(".")
                val postfix = if (indexOf != -1) filePath.substring(indexOf) else ".jpg"
                DateUtils.getCreateFileName("CMP_") + postfix
            }.filter { path ->
                if (PictureMimeType.isUrlHasImage(path) && !PictureMimeType.isHasHttp(path)) {
                    true
                } else !PictureMimeType.isUrlHasGif(path)
            }.setCompressListener(object : OnNewCompressListener {
                override fun onStart() {}
                override fun onSuccess(source: String, compressFile: File) {
                    call?.onCallback(source, compressFile.absolutePath)
                }

                override fun onError(source: String, e: Throwable) {
                    call?.onCallback(source, null)
                }
            }).launch()
        }
    }

    /**
     * 图片裁剪引擎
     */
    class ImageCropFileCropEngine : CropFileEngine {
        override fun onStartCrop(
            fragment: Fragment,
            srcUri: Uri,
            destinationUri: Uri,
            dataSource: java.util.ArrayList<String>,
            requestCode: Int
        ) {
            UCrop.of(srcUri, destinationUri, dataSource).run {
                withOptions(UCrop.Options().apply {
                    setHideBottomControls(true)
                    setFreeStyleCropEnabled(true)
                    setShowCropFrame(true)
                    setShowCropGrid(true)
                })
                setImageEngine(object : UCropImageEngine {
                    override fun loadImage(
                        context: Context,
                        url: String?,
                        imageView: ImageView?
                    ) {
                        Glide.with(context).load(url).override(180, 180).into(imageView!!)
                    }

                    override fun loadImage(
                        context: Context,
                        url: Uri?,
                        maxWidth: Int,
                        maxHeight: Int,
                        call: UCropImageEngine.OnCallbackListener<Bitmap>?
                    ) {
                        Glide.with(context).asBitmap().load(url).override(maxWidth, maxHeight)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    call?.onCall(resource)
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    call?.onCall(null)
                                }
                            })
                    }
                })
                start(fragment.requireActivity(), fragment, requestCode)
            }
        }
    }
}