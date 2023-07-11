package com.yunshang.haile_life.utils.thrid

import android.content.Context
import android.graphics.Bitmap
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory


/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/2 11:32
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
object WeChatHelper {
    //APPID
    const val APP_ID = "wx30e746ea916071ea"

    var iWXApi: IWXAPI? = null

    /**
     * 初始化
     */
    fun init(context: Context?) {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        iWXApi = WXAPIFactory.createWXAPI(context, APP_ID, false)
        iWXApi?.registerApp(APP_ID) // 将该app注册到微信
    }

    val isWxInstall: Boolean
        get() = iWXApi?.isWXAppInstalled ?: false

    /**
     * 微信登陆
     */
    fun openWeChatLogin() {
        val req = SendAuth.Req()
        req.scope = "snsapi_userinfo"
        req.state = "wechat_haile_life"
        //调起微信接口
        iWXApi?.sendReq(req)
    }

    /**
     * 调起微信支付
     *
     * @param appId
     * @param partnerId
     * @param prepayId
     * @param nonceStr
     * @param timeStamp
     * @param sign
     */
    fun openWeChatPay(
        appId: String?, partnerId: String?, prepayId: String?, nonceStr: String?,
        timeStamp: String?, sign: String?
    ) {
        //支付请求类
        val request = PayReq()
        request.appId = appId
        request.partnerId = partnerId
        request.prepayId = prepayId
        request.packageValue = "Sign=WXPay"
        request.nonceStr = nonceStr
        request.timeStamp = timeStamp
        request.sign = sign
        //调起微信支付
        iWXApi?.sendReq(request)
    }

    /**
     * 调起微信分享（图片分享到微信）
     *
     * @param bitmap 图片
     */
    fun openWeChatImgShare(bitmap: Bitmap?) {
        //初始化一个WXImageObject
        val img = WXImageObject(bitmap)
        val msg = WXMediaMessage()
        msg.mediaObject = img
        //构造一个Req
        val req = SendMessageToWX.Req()
        req.message = msg
        //分享场景
        req.scene = SendMessageToWX.Req.WXSceneSession
        //调用 api 接口，发送数据到微信
        iWXApi?.sendReq(req)
    }

    /**
     * 跳转到小程序
     *
     * @param params 参数
     */
    fun openWeChatMiniProgram(
        url: String? = null,
        params: String? = null,
        appId: String = "wx1519696063e9b048"
    ) {
        iWXApi?.sendReq(WXLaunchMiniProgram.Req().apply {
            // 填小程序原始id
            userName =appId
            //拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
            url?.let {
                path = url //"pages/goods/goods-list/index"
            }
            params?.let {
                extData = params
            }
            // 可选打开 开发版，体验版和正式版
            miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE
        })
    }

    /**
     * 调起微信分享 （h5链接分享）
     *
     * @param title
     * @param description
     * @param scene       分享场景 Session微信好友  Timeline朋友圈
     */
    fun openWeChatShare(title: String?, description: String?, scene: String) {
        //初始化一个WXWebpageObject，填写url
        val webpage = WXWebpageObject()
        webpage.webpageUrl = "http://cms.iclickdesign.com/shareWx.html"
        //用 WXWebpageObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(webpage)
        msg.title = title
        msg.description = description
        //链接压缩图
        //Bitmap thumbBmp = BitmapFactory.decodeResource(getResources(), R.drawable
        // .send_music_thumb);
        //msg.thumbData =Util.bmpToByteArray(thumbBmp, true);
        //构造一个Req
        val req = SendMessageToWX.Req()
        req.message = msg
        //分享场景
        if (scene == "Session") {
            req.scene = SendMessageToWX.Req.WXSceneSession
        } else if (scene == "Timeline") {
            req.scene = SendMessageToWX.Req.WXSceneTimeline
        }
        //调用 api 接口，发送数据到微信
        iWXApi?.sendReq(req)
    }
}