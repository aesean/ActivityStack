package com.aesean.activitystack.utils

import android.util.Log
import android.util.Printer
import java.lang.reflect.Field
import java.lang.reflect.Modifier


private const val DISPATCHING_KEY = ">>>>> Dispatching to "
private const val FINISHED_KEY = "<<<<< Finished to "

private fun String.findTargetEndIndex(): Int? {
    if (this.startsWith(DISPATCHING_KEY)) {
        val endKey = ": "
        return this.lastIndexOf(endKey) + endKey.length
    }
    return null
}

private fun String.findHandlerEndIndex(): Int? {
    val endKey = "}"
    return this.indexOf(endKey).takeIf { it >= 0 }?.apply {
        this + endKey.length
    }
}

private fun String.toWhat(): String? {
    return this.findTargetEndIndex()?.let {
        substring(it)
    }
}

private fun String.toCallback(): String? {
    if (this.startsWith(DISPATCHING_KEY)) {
        findHandlerEndIndex()?.also {
            val endKey = ": "
            return this.substring(it + " ".length, this.lastIndexOf(endKey))
        }
    }
    if (this.startsWith(FINISHED_KEY)) {
        findHandlerEndIndex()?.also {
            return this.substring(it + " ".length)
        }
    }
    return null
}

private fun String.toTargetClassName(): String? {
    val prefix = "Handler ("
    this.indexOf(prefix)
            .takeIf { it >= 0 }
            ?.also {
                return this.substring(it + prefix.length, this.lastIndexOf(")"))
            }
    return null
}

private fun String.toTarget(): String? {
    fun find(key: String): String? {
        if (!this.startsWith(key)) {
            return null
        }
        val offset = key.length
        val endKey = "}"
        return this.substring(offset, this.indexOf(endKey, offset) + endKey.length)
    }

    find(DISPATCHING_KEY)?.apply {
        return this
    }
    find(FINISHED_KEY)?.apply {
        return this
    }
    return null
}


fun toName(clazz: Class<*>, value: Int): String? {
    return toName(clazz, value, Modifier.FINAL)
}

fun toName(clazz: Class<*>, value: Int, modifiers: Int): String? {
    val m = modifiers or Modifier.STATIC
    for (field in clazz.declaredFields) {
        if ((field.modifiers and m) == m) {
            field.isAccessible = true
            try {
                val type = field.type
                if (type == Int::class.java || type == Integer.TYPE) {
                    if (value == field.get(null) as Int) {
                        return field.name
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return ""
}

private val defaultFilterArray = arrayOf("android.app.ActivityThread\$H"
        , "android.view.ViewRootImpl\$ViewRootHandler"
        , "com.android.internal.view.IInputConnectionWrapper\$MyHandler")

private val defaultFilter: (s: String) -> Boolean = { s ->
    //    var result = false
//    for (f in defaultFilterArray) {
//        if (s.contains(f)) {
//            result = true
//            break
//        }
//    }
//    result
    true
}

private val defaultMap: Map<String, Class<*>> = mapOf(Pair("android.app.ActivityThread\$H", ActivityThreadH::class.java),
        Pair("android.view.ViewRootImpl\$ViewRootHandler", ViewRootHandler::class.java),
        Pair("com.android.internal.view.IInputConnectionWrapper\$MyHandler", IInputConnectionWrapper::class.java),
        Pair("android.view.Choreographer\$FrameHandler", FrameHandlerH::class.java),
        Pair("android.hardware.display.DisplayManagerGlobal\$DisplayListenerDelegate", DisplayListenerDelegateH::class.java),
        Pair("android.view.inputmethod.InputMethodManager\$H", InputMethodManagerH::class.java))

class HandlerPrinter(private val tag: String = "HandlerPrinter"
                     , val filter: (s: String) -> Boolean = defaultFilter
                     , val classMap: Map<String, Class<*>> = defaultMap
                     , val looperProxy: LooperProxy) {

    companion object {
        fun newInstance(looperProxy: LooperProxy): HandlerPrinter {
            return HandlerPrinter(looperProxy = looperProxy)
        }
    }

    private fun log(s: String) {
        Log.d(tag, s)
    }

    private val map: MutableMap<String, Int> = HashMap()
    private val list = ArrayList<MutableMap<String, String>>()

    fun register(key: String, clazz: Class<*>, modifiers: IntArray = intArrayOf(Modifier.FINAL or Modifier.STATIC or Modifier.PRIVATE, Modifier.FINAL or Modifier.STATIC or Modifier.PUBLIC)) {
        val index = map[key]
        if (index != null) {
            generateMap(list[index], clazz, modifiers)
        } else {
            val size = list.size
            list.add(generateMap(HashMap(), clazz, modifiers))
            map[key] = size
        }
    }

    private fun generateMap(map: MutableMap<String, String>?, clazz: Class<*>, modifiers: IntArray): MutableMap<String, String> {
        val result = map ?: HashMap()
        fun generate(fields: Array<Field>) {
            for (field in fields) {
                field.isAccessible = true
                for (modifier in modifiers) {
                    if (field.modifiers == modifier) {
                        try {
                            val obj: Any? = field.get(null)
                            result[obj.toString()] = field.name
                        } catch (e: Exception) {
                        }
                        break
                    }
                }
            }
        }
        generate(clazz.declaredFields)
        generate(clazz.fields)
        return result
    }

    private fun get(key: String, name: String): String {
        val index = map[key]
        index?.apply {
            return list[this][name].toString()
        }
        return ""
    }

    fun start() {
        classMap.entries.forEach {
            val key = it.key
            val value = it.value
            register(key, value)
        }
        looperProxy.addMessageLogging { x ->
            if (filter(x)) {
                val key = x.toTargetClassName()
                if (key == null) {
                    log(x)
                } else {
                    log(x + " " + get(key, x.toWhat().toString()))
                }
            }
        }
    }

    interface PrinterProxy {
        fun setMessageLogging(printer: Printer)
    }
}