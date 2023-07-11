package com.yunshang.haile_life.business.event

/**
 * Title : 事件
 * Author: Lsy
 * Date: 2023/4/6 17:30
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object BusEvents {

    const val LOGIN_STATUS = "login_status"

    const val WECHAT_LOGIN = "wechat_login"

    const val WXPAY_STATUS = "wxpay_status"

    // 订单支付
    const val PAY_SUCCESS_STATUS = "pay_success_status"

    // 订单提交
    const val ORDER_SUBMIT_STATUS = "order_submit_status"

    // 订单取消
    const val ORDER_CANCEL_STATUS = "order_cancel_status"

    // 预约单使用
    const val APPOINT_ORDER_USE_STATUS = "appoint_order_use_status"

    // 充值成功
    const val RECHARGE_SUCCESS_STATUS = "recharge_success_status"
}