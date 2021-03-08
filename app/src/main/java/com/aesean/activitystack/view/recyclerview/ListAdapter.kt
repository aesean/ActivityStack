package com.aesean.activitystack.view.recyclerview

import android.annotation.SuppressLint
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil

class ListAdapter(
    diffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {

        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
) : AbsAdapter() {

    private val differ: AsyncListDiffer<Any> = AsyncListDiffer(this, diffCallback)

    private var list: List<Any> = emptyList()

    fun submitList(list: List<Any>) {
        checkItemViewType(list)
        val newList = list.toTypedArray().toList()
        this.list = newList
        differ.submitList(newList)
    }

    override fun getItemCount() = differ.currentList.size

    override fun getData(position: Int): Any = list[position]
}
