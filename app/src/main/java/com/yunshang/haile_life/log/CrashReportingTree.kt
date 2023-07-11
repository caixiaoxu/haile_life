package com.yunshang.haile_life.log

import timber.log.Timber

/**
 * Title : 日志上报
 * Author: Lsy
 * Date: 2023/3/20 10:09
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
//                return
//            }
//            FakeCrashLibrary.log(priority, tag, message)
//            if (t != null) {
//                if (priority == Log.ERROR) {
//                    FakeCrashLibrary.logError(t)
//                } else if (priority == Log.WARN) {
//                    FakeCrashLibrary.logWarning(t)
//                }
//            }
    }
}