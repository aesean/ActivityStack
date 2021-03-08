package com.aesean.activitystack.view.recyclerview

import android.util.SparseIntArray
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import java.util.*

private fun <T> MutableList<T>.replace(fromIndex: Int, toIndex: Int, list: List<T>) {
    this.subList(fromIndex, toIndex).apply {
        clear()
        addAll(list)
    }
}

class MultiTypeListAdapter @JvmOverloads constructor(diffCallback: DiffUtil.ItemCallback<Any> = object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return areItemsTheSame(oldItem, newItem)
    }
}) : AbsAdapter() {

    private val differ = AsyncListDiffer(this, diffCallback)

    private val mainList = ArrayList<Any>()
    private val typeIndexArray = SparseIntArray()

    fun submitList(type: Int, list: List<Any>) {
        performSubmitList(type, list.toTypedArray().asList())
    }

    fun toType(position: Int): Int {
        var typeIndex = 0
        for (i in 1 until typeIndexArray.size()) {
            if (position < typeIndexArray[typeIndexArray.keyAt(i)]) {
                return typeIndexArray.keyAt(typeIndex)
            }
            typeIndex = i
        }
        return typeIndexArray.keyAt(typeIndex)
    }

    private fun performSubmitList(type: Int, list: List<Any>) {
        fun updateTypeIndexArray(fromIndex: Int, diff: Int) {
            for (i in fromIndex until typeIndexArray.size()) {
                val keyAt = typeIndexArray.keyAt(i)
                val value = typeIndexArray.get(keyAt)
                typeIndexArray.put(keyAt, value + diff)
            }
        }

        val keyIndex = typeIndexArray.indexOfKey(type)
        if (keyIndex < 0) {
            val shouldInsertIndex = -keyIndex - 1
            val mainListIndex: Int = when {
                shouldInsertIndex < typeIndexArray.size() -> {
                    val nextKey = typeIndexArray.keyAt(shouldInsertIndex)
                    val nextIndex = typeIndexArray.get(nextKey)

                    updateTypeIndexArray(nextIndex, list.size)
                    nextIndex
                }
                else -> mainList.size
            }

            typeIndexArray.put(type, mainListIndex)
            mainList.replace(mainListIndex, mainListIndex, list)
        } else {
            val fromIndex = typeIndexArray.get(typeIndexArray.keyAt(keyIndex), -1)
            val toIndex = if (keyIndex == typeIndexArray.size() - 1) {
                mainList.size
            } else {
                typeIndexArray.get(typeIndexArray.keyAt(keyIndex + 1), mainList.size).also {
                    val diff = list.size - (it - fromIndex)
                    updateTypeIndexArray(keyIndex + 1, diff)
                }
            }

            require(fromIndex <= toIndex) { "type = $type, keyIndex = $keyIndex, fromIndex = $fromIndex, toIndex = $toIndex, mainList = $mainList, list = $list, typeIndexArray = $typeIndexArray" }
            mainList.replace(fromIndex, toIndex, list)
        }

        differ.submitList(mainList.toList())
    }

    override fun getItemCount() = mainList.size

    override fun getData(position: Int): Any = mainList[position]
}