/**
 * Android 配置
 */
object AndroidConfig {
    const val compileSdk = 33
    const val namespace = "com.yunshang.haile_life"
    val defaultConfig = DefaultConfig()

    class DefaultConfig {
        val applicationId = "com.yunshang.haile_life"
        val minSdk = 21
        val targetSdk = 33
        val versionCode = 10001
        val versionName = "1.0.1"
    }
}