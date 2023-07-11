package com.yunshang.haile_life.ui.view.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/11 20:32
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
abstract class BaseRecyclerAdapter<D> : RecyclerView.Adapter<MyViewHolder>() {

    val list: MutableList<D> = mutableListOf()

    /**
     * 刷新数据
     * @param isRefresh 是否替换列表
     * @param list 数据列表
     */
    fun refreshList(list: MutableList<out D>?, isRefresh: Boolean = false) {
        if (isRefresh) {
            this.list.clear()
        }
        if (null == list) {
            if (isRefresh) {
                notifyDataSetChanged()
            }
            return
        }

        val start = this.list.size
        this.list.addAll(list)
        if (isRefresh) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(start, list.size)
        }
    }

    /**
     * 删除指定数据
     */
    fun deleteItem(predicate: (D) -> Boolean) {
        val index = list.indexOfFirst(predicate)
        if (-1 != index) {
            list.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemCount(): Int = list.size
}

class MyViewHolder(itemView: View) :
    RecyclerView.ViewHolder(itemView) {

    fun <T : ViewDataBinding> bind(br: Int, entity: Any?): T? =
        DataBindingUtil.bind<T>(itemView)?.apply {
            setVariable(br, entity)
        }
}