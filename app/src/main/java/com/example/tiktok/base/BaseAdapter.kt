package com.example.tiktok.base

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import android.view.View

abstract class BaseAdapter<VH:RecyclerView.ViewHolder,T>(diffUtil: DiffUtil.ItemCallback<T>) : ListAdapter<T,VH>(diffUtil) {
    protected var mList:ArrayList<T> = ArrayList()
    private  var itemClickListener:OnItemClickListener?=null
    //列表项点击监听器
    fun setOnClickListener(itemClickListener:OnItemClickListener){
        this.itemClickListener = itemClickListener
    }
    //为列表项添加点击事件
    fun View.setOnItemClick(position: Int) {
        setOnClickListener {
            itemClickListener?.onItemClick(mList[position])
        }
    }
    fun clearList(){
        mList.clear()
        submitList(mList.toList())
    }

    //追加列表数据（增量刷新）
    fun appendList(list:List<T>){
        mList.addAll(list)
        submitList(mList.toList())
    }

    //获取当前列表数据
    fun getData(): ArrayList<T> {
        return mList
    }

    //返回列表项数量
    override fun getItemCount(): Int {
        return mList.size
    }


    //列表项点击事件接口
    interface OnItemClickListener{
        fun<E>onItemClick(item:E)
    }
}
