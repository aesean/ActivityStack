package com.aesean.activitystack.view.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import java.util.*

typealias OnCreateViewCallback<T> = (dataHolder: () -> T, view: View) -> Unit
typealias OnBindViewCallback<T> = (view: View, data: T) -> Unit
typealias ViewGenerator = (parent: ViewGroup) -> View
typealias ViewTypeGenerator = (clazz: Class<*>) -> Int

interface ViewHolderViewBuilder<T> {
    fun setView(viewGenerator: ViewGenerator): ViewHolderViewBinder<T>
    fun setView(@LayoutRes layoutRes: Int): ViewHolderViewBinder<T>
}

interface ViewHolderViewBinder<T> {
    fun onViewCreated(callback: OnCreateViewCallback<T>): ViewHolderViewBinder<T>
    fun onBindView(callback: OnBindViewCallback<T>): ViewHolderViewBinder<T>
}

private class ViewHolderGeneratorImpl<T> : ViewHolderViewBuilder<T>, ViewHolderViewBinder<T> {
    var viewGenerator: ((parent: ViewGroup) -> View)? = null
    var createViewCallbackList: MutableList<OnCreateViewCallback<T>> = LinkedList()
    var bindViewCallbackList: MutableList<OnBindViewCallback<T>> = LinkedList()

    override fun setView(viewGenerator: ViewGenerator) = this.also {
        this.viewGenerator = viewGenerator
    }

    override fun setView(layoutRes: Int) = this.also {
        this.viewGenerator = { parent ->
            LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        }
    }

    override fun onViewCreated(callback: OnCreateViewCallback<T>): ViewHolderViewBinder<T> =
            this.also { this.createViewCallbackList.add(callback) }

    fun performViewCreate(dataHolder: () -> Any, view: View) {
        createViewCallbackList.forEach { action ->
            action({
                @Suppress("UNCHECKED_CAST")
                dataHolder() as T
            }, view)
        }
    }

    fun performBind(view: View, data: Any) {
        this.bindViewCallbackList.forEach { callback ->
            @Suppress("UNCHECKED_CAST")
            callback(view, data as T)
        }
    }

    override fun onBindView(callback: OnBindViewCallback<T>): ViewHolderViewBinder<T> = this.also {
        this.bindViewCallbackList.add(callback)
    }
}

abstract class AbsAdapter : RecyclerView.Adapter<ViewHolder>() {

    abstract fun getData(position: Int): Any

    private val generatorMap = SparseArray<ViewHolderGeneratorImpl<*>?>()

    var viewTypeGenerator: ViewTypeGenerator = { clazz -> System.identityHashCode(clazz) }

    companion object {
        private val primitiveMap = mapOf(
                java.lang.Boolean.TYPE to java.lang.Boolean::class.java,
                java.lang.Character.TYPE to java.lang.Character::class.java,
                java.lang.Byte.TYPE to java.lang.Byte::class.java,
                java.lang.Short.TYPE to java.lang.Short::class.java,
                java.lang.Integer.TYPE to java.lang.Integer::class.java,
                java.lang.Float.TYPE to java.lang.Float::class.java,
                java.lang.Long.TYPE to java.lang.Long::class.java,
                java.lang.Double.TYPE to java.lang.Double::class.java,
                java.lang.Void.TYPE to java.lang.Void::class.java)
    }

    fun <T> register(dataType: Class<T>): ViewHolderViewBuilder<T> =
            ViewHolderGeneratorImpl<T>().apply {
                val type = primitiveMap[dataType] ?: dataType
                generatorMap.put(viewTypeGenerator(type), this)
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holderGenerator: ViewHolderGeneratorImpl<*> = generatorMap.get(viewType)
                ?: throw IllegalArgumentException("viewType didn't register. viewType = $viewType")
        val viewHolder = holderGenerator.viewGenerator
                ?.let { ViewHolder(it(parent)) }
                ?: throw IllegalArgumentException("ViewHolderGenerator can't be null.")
        holderGenerator.performViewCreate({
            getData(viewHolder.adapterPosition)
        }, viewHolder.itemView)
        return viewHolder
    }

    final override fun getItemViewType(position: Int): Int {
        val data = getData(position)
        return viewTypeGenerator(data::class.java).apply {
            requireNotNull(generatorMap[this]) {
                "class didn't register. Have you forgotten to call register(...)? " +
                        "class = ${data.javaClass}, data = $data"
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        generatorMap.get(getItemViewType(position))
                ?.performBind(holder.itemView, getData(position))
                ?: throw IllegalArgumentException(
                        "class didn't register. Have you forgotten to call register(...)? "
                                + "class = ${getData(position)}"
                )
    }
}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    constructor(parent: ViewGroup, @LayoutRes layoutRes: Int) :
            this(LayoutInflater.from(parent.context).inflate(layoutRes, parent, false))
}