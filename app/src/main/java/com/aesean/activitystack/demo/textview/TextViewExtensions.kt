package com.aesean.activitystack.demo.textview


import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionManager
import com.aesean.activitystack.BuildConfig

private const val logTag = "TextViewLayout"

private val enableDebugLog = BuildConfig.DEBUG

private fun log(any: Any?) {
    if (enableDebugLog) {
        Log.d(logTag, any.toString())
    }
}

/**
 * TextViewBuilder
 * ellipsize textView with custom suffix.
 */
class TextViewSuffixWrapper(val textView: TextView) {
    private data class SuffixColor(val fromIndex: Int, val toIndex: Int, val color: Int?, val listener: View.OnClickListener? = null)

    var mainContent: CharSequence = textView.text
        set(value) {
            collapseCache = null
            field = value
        }
    var suffix: CharSequence? = null

    private var collapseCache: CharSequence? = null

    var isCollapsed: Boolean = false
        private set

    var targetLineCount: Int = 2
    var transition: Transition? = AutoTransition()

    private val textWrapper: (text: String, suffix: CharSequence, suffixIndex: Int) -> CharSequence =
            { text, suffix, suffixIndex ->
                SpannableStringBuilder(text).apply {
                    suffixColorList.forEach {
                        val start = suffixIndex + it.fromIndex
                        val end = suffixIndex + it.toIndex
                        it.listener?.also { listener ->
                            setSpan(object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    listener.onClick(widget)
                                }
                            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            textView.movementMethod = LinkMovementMethod.getInstance()
                        }
                        it.color?.also { color ->
                            setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }

    private val suffixColorList by lazy {
        mutableListOf<SuffixColor>()
    }

    init {
        if (textView.layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            throw RuntimeException("textView's width can't be wrap_content. Only support match_parent or specified size")
        }
    }

    fun suffixColor(fromIndex: Int, toIndex: Int, @ColorRes colorRes: Int) {
        val color = ResourcesCompat.getColor(textView.resources, colorRes, textView.context.theme)
        suffixColorList.add(SuffixColor(fromIndex, toIndex, color, null))
    }

    fun suffixColor(fromIndex: Int, toIndex: Int, listener: View.OnClickListener) {
        suffixColorList.add(SuffixColor(fromIndex, toIndex, null, listener))
    }

    fun suffixColor(fromIndex: Int, toIndex: Int, @ColorRes colorRes: Int, listener: View.OnClickListener) {
        val color = ResourcesCompat.getColor(textView.resources, colorRes, textView.context.theme)
        suffixColorList.add(SuffixColor(fromIndex, toIndex, color, listener))
    }

    @JvmOverloads
    fun collapse(animation: Boolean = true) {
        performCollapse(
                if (animation) {
                    transition
                } else {
                    null
                }
        )
    }

    @JvmOverloads
    fun toggle(animation: Boolean = true) {
        if (isCollapsed) {
            expand(animation)
        } else {
            collapse(animation)
        }
    }

    @JvmOverloads
    fun expand(animation: Boolean = true) {
        performExpand(
                if (animation) {
                    this.transition
                } else {
                    null
                }
        )
    }

    var sceneRoot: ViewGroup = textView.parent as ViewGroup

    private fun performExpand(transition: Transition?) {
        isCollapsed = false
        textView.expand(mainContent = mainContent, transition = transition, sceneRoot = sceneRoot)
    }

    private fun performCollapse(transition: Transition?) {
        isCollapsed = true
        fun defaultCollapse() {
            textView.maxLines = targetLineCount
            textView.ellipsize = TextUtils.TruncateAt.END
            val originText = textView.text
            textView.text = mainContent
            if (transition != null) {
                textView.apply {
                    val layout = this.layout
                    if (layout != null) {
                        val targetHeight = layout.height + paddingTop + paddingBottom
                        text = originText
                        maxLines = Int.MAX_VALUE

                        layoutParams.height = targetHeight
                        layoutParams = layoutParams
                        transition.addListener(object : TransitionListenerAdapter() {
                            override fun onTransitionEnd(transition: Transition) {
                                transition.removeListener(this)
                                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                                layoutParams = layoutParams
                                maxLines = targetLineCount
                                text = mainContent
                            }

                            override fun onTransitionCancel(transition: Transition) {
                                transition.removeListener(this)
                            }

                        })
                    }
                    TransitionManager.beginDelayedTransition(sceneRoot, transition)
                }
            }
        }

        if (suffix == null) {
            defaultCollapse()
        } else {
            if (collapseCache != null) {
                if (collapseCache == mainContent) {
                    return
                }
                if (transition != null) {
                    textView.setTextWithAnimator(content = collapseCache!!, transition = transition, sceneRoot = sceneRoot)
                } else {
                    textView.maxLines = targetLineCount
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.text = collapseCache
                }
            } else {
                textView.collapse(
                        mainContent = mainContent,
                        suffix = suffix!!,
                        targetLineCount = targetLineCount,
                        transition = transition,
                        sceneRoot = sceneRoot,
                        onSuccess = { text -> collapseCache = text },
                        onFailed = {
                            defaultCollapse()
                        },
                        textWrapper = textWrapper
                )
            }
        }
    }
}

@JvmOverloads
fun TextView.setTextWithAnimator(
        content: CharSequence,
        transition: Transition = AutoTransition(),
        sceneRoot: ViewGroup = this.parent as ViewGroup
) {
    val originText = this.text
    this.text = content
    val layout = this.layout
    if (layout != null) {
        val targetHeight = layout.height + paddingTop + paddingBottom
        text = originText

        layoutParams.height = targetHeight
        layoutParams = layoutParams
        transition.addListener(object : TransitionListenerAdapter() {
            override fun onTransitionEnd(transition: Transition) {
                transition.removeListener(this)
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                layoutParams = layoutParams
                text = content
            }

            override fun onTransitionCancel(transition: Transition) {
                transition.removeListener(this)
            }

        })
    }
    TransitionManager.beginDelayedTransition(sceneRoot, transition)
}

/**
 * collapse TextView
 * @param mainContent text
 * @param suffix suffix
 * @param targetLineCount targetLineCount
 * @param transition for animation. If is null, there will be no animation and it will take effect immediately.
 * @param onSuccess There may be asynchronous operations here, You can listen to the end event via onSuccess.
 * @param textWrapper You can wrap the text, such as changing the color through a SpannableString.
 */
fun TextView.collapse(
        mainContent: CharSequence,
        suffix: CharSequence,
        targetLineCount: Int,
        transition: Transition? = AutoTransition(),
        sceneRoot: ViewGroup = this.parent as ViewGroup,
        onSuccess: ((text: CharSequence) -> Unit)? = null,
        onFailed: ((text: CharSequence) -> Unit)? = null,
        textWrapper: ((text: String, suffix: CharSequence, suffixIndex: Int) -> CharSequence)?
) {
    val originText = text
    setTextWithSuffix(
            mainContent = mainContent, suffix = suffix, targetLineCount = targetLineCount,
            onSuccess = { result ->
                if (transition == null) {
                    onSuccess?.invoke(result)
                } else {
                    val targetText = text
                    val targetHeight = layout.height + paddingTop + paddingBottom
                    text = originText

                    layoutParams.height = targetHeight
                    layoutParams = layoutParams
                    transition.addListener(object : TransitionListenerAdapter() {
                        override fun onTransitionEnd(transition: Transition) {
                            transition.removeListener(this)
                            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                            layoutParams = layoutParams
                            text = targetText
                            onSuccess?.invoke(result)
                        }

                        override fun onTransitionCancel(transition: Transition) {
                            transition.removeListener(this)
                        }

                    })
                    TransitionManager.beginDelayedTransition(sceneRoot, transition)
                }
            },
            onFailed = {
                this.text = mainContent
                this.maxLines = targetLineCount
                onFailed?.invoke(mainContent)
            },
            textWrapper = textWrapper
    )

}

/**
 * expand TextView
 * @param mainContent text
 * @param transition for animation. If is null, there will be no animation and it will take effect immediately.
 */
fun TextView.expand(
        mainContent: CharSequence, transition: Transition? = AutoTransition(),
        sceneRoot: ViewGroup = this.parent as ViewGroup
) {
    this.maxLines = Int.MAX_VALUE
    this.text = mainContent
    transition?.also {
        TransitionManager.beginDelayedTransition(sceneRoot, it)
    }
}

fun TextView.setTextWithSuffix(
        mainContent: CharSequence,
        suffix: CharSequence,
        targetLineCount: Int,
        onSuccess: (text: CharSequence) -> Unit = {},
        onFailed: (text: CharSequence) -> Unit = { text ->
            this.text = text
            maxLines = targetLineCount
            ellipsize = TextUtils.TruncateAt.END
        },
        textWrapper: ((text: String, suffix: CharSequence, suffixIndex: Int) -> CharSequence)?
) {

    val originText = text

    fun autoSet(index: Int) {
        if (index < 0) {
            onFailed(originText)
        } else {
            text = if (index >= mainContent.length) {
                mainContent
            } else {
                val msg = "${mainContent.substring(0, index)}$suffix"
                textWrapper?.invoke(msg, suffix, index) ?: msg
            }
            onSuccess(text)
        }
    }

    if (layout == null) {
        val listener = object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                    v: View?,
                    left: Int, top: Int, right: Int, bottom: Int,
                    oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int
            ) {
                removeOnLayoutChangeListener(this)
                if (layout == null) {
                    onFailed(text)
                } else {
                    post {
                        val start = System.currentTimeMillis()
                        autoSet(binarySearch(mainContent, suffix, targetLineCount, textWrapper))
                        val end = System.currentTimeMillis()
                        log(">>>>>performance: ${end - start}ms")
                    }
                }
            }
        }
        addOnLayoutChangeListener(listener)
        requestLayout()
    } else {
        val start = System.currentTimeMillis()
        autoSet(binarySearch(mainContent, suffix, targetLineCount, textWrapper))
        val end = System.currentTimeMillis()
        log(">>>>>performance: ${end - start}ms")
    }
}

/**
 * Note: binary search will change the text, so be sure to remember the case where the search failed.
 */
private fun TextView.binarySearch(
        mainContent: CharSequence,
        suffix: CharSequence,
        targetLineCount: Int,
        textWrapper: ((text: String, suffix: CharSequence, suffixIndex: Int) -> CharSequence)?
): Int {
    var verifyCount = 0

    val verifyCache = mutableMapOf<Int, Int>()

    fun verify(start: Int, end: Int): Int {
        val key = (start shl 16) or end
        val hit = verifyCache[key]
        if (hit != null) {
            log("verify: $end cached")
            return hit
        }
        log("verify: $end")
        verifyCount++
        val tmp = mainContent.substring(start, end)
        val context = "$tmp$suffix"
        text = textWrapper?.invoke(context, suffix, end) ?: context
        val lineCount = this.lineCount
        verifyCache[key] = lineCount
        return lineCount
    }
    if (layout == null) {
        log("layout is null")
        return -1
    }
    val verify = verify(0, mainContent.length)
    if (verify <= targetLineCount) {
        log("verify <= targetLineCount, verify = $verify, targetLineCount = $targetLineCount")
        text = mainContent
        return mainContent.length
    }

    var left = 0
    var right = mainContent.length
    while (left <= right) {
        val mid = (left + right) / 2

        val pLineCount = verify(0, mid)
        var s = "binarySearch: ($left, $mid, $right), pLineCount = $pLineCount"
        if (pLineCount < targetLineCount) {
            s += ", targetLineCount = $targetLineCount, pLineCount < targetLineCount"
            left = mid + 1
        } else if (pLineCount == targetLineCount) {
            val nLineCount = verify(0, mid + 1)
            s += ", nLineCount = $nLineCount"
            if (nLineCount < targetLineCount + 1) {
                left = mid + 1
            } else if (nLineCount == targetLineCount + 1) {
                log("success = $mid, verifyCount = $verifyCount")
                return mid
            } else {
                // error impossible
                log("impossible")
                break
            }
        } else {
            right = mid - 1
        }

        log("$s, text = ${mainContent.substring(0, mid) + suffix}")
    }
    log("failed, verifyCount = $verifyCount")
    return -1
}

