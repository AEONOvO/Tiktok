package com.example.tiktok.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter

//封装了 ListAdapter 的通用基类，提供了一般性的逻辑
abstract class BaseAdapter<VH:RecyclerView.ViewHolder,T>(diffUtil: DiffUtil.ItemCallback<T>) : ListAdapter<T,VH>(diffUtil) {

    protected var mList:ArrayList<T> = ArrayList()

    // 清空列表
    fun clearList(){
        mList.clear()
        submitList(mList.toList())
    }

    //追加列表数据（增量刷新）
    fun appendList(list:List<T>){
        mList.addAll(list)
        submitList(mList.toList())
    }

}
