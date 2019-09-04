package com.aesean.activitystack.view.recyclerview

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

class ListAdapter @JvmOverloads constructor(diffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }
}) : AbsAdapter() {

    private val differ: AsyncListDiffer<Any> = diffCallback.let {
        AsyncListDiffer(this, diffCallback)
    }

    private var list: List<Any> = emptyList()

    fun submitList(list: List<Any>) {
        checkItemViewType(list)
        val newList = list.toTypedArray().toList()
        this.list = newList
        differ.submitList(newList)
    }

    override fun getItemCount() = list.size

    override fun getData(position: Int): Any = list[position]
}
