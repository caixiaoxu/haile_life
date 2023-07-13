package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/7 15:29
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ADEntity(
    val createTime: String,
    val imageSize: String,
    val images: List<ADImage>,
    val showType: Int,
    val slotKey: String,
    val slotName: String
)

data class ADImage(
    val alert: String,
    val closeButtonPosition: String,
    val id: Int,
    val imageSize: String,
    val imageUrl: String,
    val linkType: Int, // 0: "无", 3: "app内H5", 4: "app外H5", 5: "app原生路径", 6: "app外唤醒"
    val linkUrl: String,
    val name: String,
    val rate: Int,
    val sortValue: Int,
    val topOnSlotId: String
)