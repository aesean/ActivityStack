package com.aesean.activitystack.view.recyclerview

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import java.util.*

typealias OnCreateViewCallback<T> = (view: View, viewHolder: RecyclerView.ViewHolder, dataHolder: () -> T) -> Unit
typealias OnBindViewCallback<T> = (view: View, data: T) -> Unit
typealias ViewGenerator = (parent: ViewGroup) -> View

typealias ViewResGenerator = (parent: ViewGroup) -> Int

interface ViewHolderBuilder<T> {
    fun createView(viewGenerator: ViewGenerator): ViewHolderBinder<T>
    fun layoutRes(viewResGenerator: ViewResGenerator): ViewHolderBinder<T>
}

interface ViewHolderBinder<T> {
    fun onViewCreated(callback: OnCreateViewCallback<T>): ViewHolderBinder<T>
    fun onItemViewClicked(listener: View.OnClickListener): ViewHolderBinder<T>
    fun onViewClicked(@IdRes viewId: Int, listener: View.OnClickListener): ViewHolderBinder<T>
    fun onBindView(callback: OnBindViewCallback<T>): ViewHolderBinder<T>
}

class ViewHolderImpl<T> : ViewHolderBuilder<T>, ViewHolderBinder<T> {

    var viewGenerator: ((parent: ViewGroup) -> View)? = null
    var createViewCallbackList: MutableList<OnCreateViewCallback<T>> = LinkedList()
    var bindViewCallbackList: MutableList<OnBindViewCallback<T>> = LinkedList()
    var onItemViewClickedListeners: MutableList<View.OnClickListener> = LinkedList()
    var onViewClickedListeners: MutableList<Pair<Int, View.OnClickListener>> = LinkedList()

    override fun createView(viewGenerator: ViewGenerator): ViewHolderBinder<T> {
        this.viewGenerator = viewGenerator
        return this
    }

    override fun layoutRes(viewResGenerator: ViewResGenerator): ViewHolderBinder<T> = this.also {
        this.viewGenerator = { parent ->
            LayoutInflater.from(parent.context)
                .inflate(viewResGenerator(parent), parent, false)
        }
    }

    override fun onViewCreated(callback: OnCreateViewCallback<T>): ViewHolderBinder<T> =
        this.also { this.createViewCallbackList.add(callback) }

    override fun onItemViewClicked(listener: View.OnClickListener): ViewHolderBinder<T> =
        this.also { onItemViewClickedListeners.add(listener) }

    override fun onViewClicked(
        viewId: Int,
        listener: View.OnClickListener
    ): ViewHolderBinder<T> =
        this.also { onViewClickedListeners.add(Pair(viewId, listener)) }

    fun performViewCreate(view: View, viewHolder: RecyclerView.ViewHolder, dataHolder: () -> Any) {
        onItemViewClickedListeners.forEach { listener ->
            viewHolder.itemView.setOnClickListener(listener)
        }
        onViewClickedListeners.forEach { (id, view) ->
            viewHolder.itemView.findViewById<View>(id).setOnClickListener(view)
        }
        createViewCallbackList.forEach { action ->
            action(view, viewHolder) {
                @Suppress("UNCHECKED_CAST")
                dataHolder() as T
            }
        }
    }

    fun performBind(view: View, data: Any) {
        this.bindViewCallbackList.forEach { callback ->
            @Suppress("UNCHECKED_CAST")
            callback(view, data as T)
        }
    }

    override fun onBindView(callback: OnBindViewCallback<T>): ViewHolderBinder<T> = this.also {
        this.bindViewCallbackList.add(callback)
    }

}

abstract class AbsAdapter : RecyclerView.Adapter<ViewHolder>() {

    var enableViewTypeCheck = false

    abstract fun getData(position: Int): Any

    private val generatorMap = SparseArray<ViewHolderImpl<*>?>()

    private val interfaceMap = mutableMapOf<Class<*>, Int>()
    private val viewTypeCache = mutableMapOf<Class<*>, Int>()

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
            java.lang.Void.TYPE to java.lang.Void::class.java
        )
    }

    fun <T> register(dataType: Class<T>): ViewHolderBuilder<T> =
        ViewHolderImpl<T>().apply {
            val newViewType = primitiveMap[dataType] ?: dataType
            val type = System.identityHashCode(newViewType)
            if (newViewType.isInterface) {
                interfaceMap[newViewType] = type
            } else {
                viewTypeCache[newViewType] = type
            }
            generatorMap.put(type, this)
        }

    inline fun <reified T> register(): ViewHolderBuilder<T> {
        return register(T::class.java)
    }

    inline fun <reified T> registerView(crossinline viewGenerator: ViewGenerator): ViewHolderBinder<T> {
        return register(T::class.java).createView { viewGenerator(it) }
    }

    inline fun <reified T> registerViewRes(crossinline viewResGenerator: ViewResGenerator): ViewHolderBinder<T> {
        return register(T::class.java).layoutRes { viewResGenerator(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val holderGeneratorImpl: ViewHolderImpl<*> = generatorMap.get(viewType)
            ?: throw IllegalArgumentException("viewType didn't register. viewType = $viewType")
        val viewHolder = holderGeneratorImpl.viewGenerator
            ?.let { ViewHolder(it(parent)) }
            ?: throw IllegalArgumentException("ViewHolderGenerator can't be null.")
        holderGeneratorImpl.performViewCreate(viewHolder.itemView, viewHolder) {
            getData(viewHolder.bindingAdapterPosition)
        }
        return viewHolder
    }

    protected fun checkItemViewType(list: List<Any>) {
        if (enableViewTypeCheck) {
            list.forEach { it.toViewType() }
        }
    }

    final override fun getItemViewType(position: Int): Int {
        return getData(position).toViewType()
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun Class<*>.toViewType(): Int {
        val dataClassType = this
        val cachedType = viewTypeCache[dataClassType]
        if (cachedType == null) {
            interfaceMap.entries.forEach { entry ->
                val key = entry.key
                val value = entry.value
                if (key.isAssignableFrom(dataClassType)) {
                    viewTypeCache[dataClassType] = value
                    return value
                }
            }
            val msg = "$dataClassType didn't register. Have you forgotten to call register(...)? "
            throw IllegalArgumentException(msg)
        } else {
            return cachedType
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun Any.toViewType(): Int {
        val data = this
        val dataClassType = this::class.java
        try {
            return dataClassType.toViewType()
        } catch (e: IllegalArgumentException) {
            throw IllegalStateException("data = $data", e)
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

class ViewHolder(view: View) : RecyclerView.ViewHolder(view)