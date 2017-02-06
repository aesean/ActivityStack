/*
 *    Copyright (C) 2017.  Aesean
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.aesean.activitystack;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Printer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * BlockUtils
 * 原理大致就是:通过{@link Looper#setMessageLogging(Printer)}方法,监测Handler打印消息之间间隔的时间,
 * 来监控获取主线程执行持续时间,然后通过一个HandlerThread子线程来打印主线程堆栈信息。
 * 由于大部分耗时操作都是通过子线程处理的，基本可以忽略当前监控服务对于主线程性能的影响。
 * {@link #receiveDispatchingMessage()}方法会给WatchHandler发送一个开启监听的消息。
 * WatchHandler会每隔{@link #DUMP_STACK_DELAY_MILLIS}dump一次堆栈数据，
 * 此时如果{@link #receiveFinishedMessage()}检测到没有超时，会通知WatchHandler取消继续dump
 * （这里会有较小概率发生线程安全问题，明明正常finishMessage，WatchHandler还是打印了堆栈，但是不会无限打印）。
 * 如果{@link #receiveFinishedMessage()}没有收到消息，或者收到超时消息，则忽略消息，WatchHandler
 * 会自己处理超时打印（这里之所以这么做主要是避免线程同步）。
 *
 * @author xl
 * @version V1.3
 * @since 16/8/15
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BlockUtils {
    private static final String TAG = "BlockUtils";
    private static final String HANDLER_THREAD_TAG = "watch_handler_thread";
    /**
     * 用于分割字符串,LogCat对打印的字符串长度有限制
     */
    private static final String PART_SEPARATOR = "3664113077962208511";

    /**
     * 卡顿,单位毫秒,这个参数因为子线程也要用,为了避免需要线程同步,所以就static final了,自定义请直接修改这个值.
     */
    private static final long BLOCK_DELAY_MILLIS = 600;

    /**
     * 最大处理次数，-1会无限打印，只有收到finish消息的时候才会终止打印。
     */
    private static final int MAX_POST_TIMES = -1;
    /**
     * Dump堆栈数据时间间隔,单位毫秒
     */
    private static final long DUMP_STACK_DELAY_MILLIS = 160;
    private static final long START_DUMP_STACK_DELAY_MILLIS = 80;
    private static final long SYNC_DELAY = 0;

    private Looper mWatchLooper;

    /**
     * 堆栈内容相同的情况下是否打印
     */
    private static final boolean PRINT_SAME_STACK = false;

    /**
     * 起一个子线程,用来打印主线程的堆栈信息.因为是要监控主线程是否有卡顿的,所以主线程现在是无法打印堆栈的,
     * 所以需要起一个子线程来打印主线程的堆栈信息.
     */
    private HandlerThread mWatchThread;
    private Handler mWatchHandler;
    private PrintStaceInfoRunnable mPrintStaceInfoRunnable;

    private long mStartTime;

    private BlockUtils() {
        this(Looper.getMainLooper());
    }

    public BlockUtils(Looper looper) {
        mWatchLooper = looper;
    }

    private Printer createBlockPrinter() {
        return new Printer() {
            /**
             * 纪录当前Printer回调的状态,注意这里初始状态必须是true.
             */
            private boolean mPrinterStart = true;

            @Override
            public void println(String s) {
                // 默认不修正，如果实际出现错乱，可以调用下面方法进行修正。
                // checkMessage(s);
                // 这里因为Looper.loop方法内会在Handler开始和结束调用这个方法,所以这里也对应两个状态,start和finish
                if (mPrinterStart) {
                    receiveDispatchingMessage();
                } else {
                    receiveFinishedMessage();
                }
                mPrinterStart = !mPrinterStart;
            }

            /**
             * 校验，这里默认收到消息是一次start，下一次finish，如果实际出现错乱，
             * 可以根据{@link Looper#loop()}中logging.println打印的Message格式，进行一次修正。
             */
            private void checkMessage(String s) {
                if (s.startsWith(">>>>> Dispatching to")) {
                    mPrinterStart = true;
                }
                if (s.startsWith("<<<<< Finished to")) {
                    mPrinterStart = false;
                }
            }

        };
    }

    /**
     * dump堆栈数据，会添加时间标签
     *
     * @param dumpThread 需要处理的线程
     * @return 堆栈数据
     */
    public static String dumpStackWithTimeHead(Thread dumpThread) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.getDefault());
        String head = "\n" + dateFormat.format(new Date(System.currentTimeMillis())) + "时堆栈状态\n";
        String dumpStack = dumpStack(dumpThread);
        return head + dumpStack;
    }

    /**
     * dump堆栈数据
     *
     * @param dumpThread 需要处理的线程
     * @return 堆栈数据
     */
    public static String dumpStack(Thread dumpThread) {
        StackTraceElement[] stackTraceElements = dumpThread.getStackTrace();
        // 注意这里仅仅是打印当前堆栈信息而已,实际代码不一定就是卡这里了.
        // 比如此次Handler一共要处理三个方法
        // method0(); 需要100ms
        // method1(); 需要200ms
        // method2(); 需要300ms
        // 其实最佳方案是这三个方法全部打印,但从代码层面很难知道是这三个方法时候打印
        // 只能每隔一段时间（比如：100ms）dump一次主线程堆栈信息,但是因为线程同步问题，可能第一个method0dump不到
        String temp = "";
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            String stack = stackTraceElement.toString() + "\n";
            temp += stack;
        }
        if (TextUtils.isEmpty(temp)) {
            temp = "null";
        }
        return temp;
    }

    private void receiveDispatchingMessage() {
        mStartTime = System.currentTimeMillis();
        // 如果有出现没有正确收到finishMessage的情况，这里可以每次start强制再处理一次。
        // if (mPrintStaceInfoRunnable != null) {
        //     mPrintStaceInfoRunnable.cancel();
        //     mWatchHandler.removeCallbacks(mPrintStaceInfoRunnable);
        // }
        // 注意当前类所有代码,除了这个方法里的代码,其他全部是在主线程执行.
        mPrintStaceInfoRunnable = new PrintStaceInfoRunnable();
        mWatchHandler.postDelayed(mPrintStaceInfoRunnable, START_DUMP_STACK_DELAY_MILLIS);
    }

    private void receiveFinishedMessage() {
        long end = System.currentTimeMillis();
        long delay = end - mStartTime;
        if (delay >= BLOCK_DELAY_MILLIS) {
            Log.w(TAG, "(" + mPrintStaceInfoRunnable.getTag() + ") 检测到超时，App执行本次Handler消息消耗了:" + delay + "ms\n");
        }
        // 这里是主线程，设置cancel后，没有做线程同步，子线程同步数据可能会有延迟。
        mPrintStaceInfoRunnable.cancel();
        mWatchHandler.removeCallbacks(mPrintStaceInfoRunnable);
        mPrintStaceInfoRunnable = null;
    }

    /**
     * 使用{@link #install()}代替
     */
    @Deprecated
    public void start() {
        install();
    }

    public void install() {
        if (mWatchThread != null || mWatchHandler != null) {
            throw new RuntimeException("请勿重复install。如果需要释放资源，请调用release方法。");
        }
        mWatchThread = new HandlerThread(HANDLER_THREAD_TAG);
        mWatchThread.start();
        mWatchHandler = new Handler(mWatchThread.getLooper());
        mWatchLooper.setMessageLogging(getInstance().createBlockPrinter());
    }

    /**
     * 使用{@link #release()}代替
     */
    @Deprecated
    public void stop() {
        release();
    }

    public void release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWatchThread.quitSafely();
        } else {
            mWatchThread.quit();
        }
        mWatchThread = null;
        mWatchHandler = null;
        mWatchLooper.setMessageLogging(null);
        mWatchLooper = null;
    }

    public static BlockUtils getInstance() {
        return InstanceHolder.sInstance;
    }

    private static class InstanceHolder {
        private static final BlockUtils sInstance = new BlockUtils();
    }

    private class PrintStaceInfoRunnable implements Runnable {

        private String mTag = String.valueOf(this.hashCode());
        private long mStartMillis = System.currentTimeMillis();
        private int mPostTimes = 0;
        private String mLastDumpStack;
        private int mTimeoutTimes = 0;
        private boolean mCancel = false;
        private StringBuilder mStackInfo = new StringBuilder();

        private Thread mDumpThread = mWatchLooper.getThread();

        private boolean isTimeOut() {
            return System.currentTimeMillis() - mStartMillis > BLOCK_DELAY_MILLIS + SYNC_DELAY;
        }

        private String getTag() {
            return mTag;
        }

        private void recordTimes() {
            mPostTimes++;
            if (isTimeOut()) {
                mTimeoutTimes++;
            }
        }

        private void cancel() {
            mCancel = true;
        }

        private boolean isFirstTimeout() {
            return mTimeoutTimes == 1;
        }

        private String newTimeHead() {
            return "\n(" + getTag() + ") " + (System.currentTimeMillis() - mStartMillis) + "ms时堆栈状态\n";
        }

        private boolean equalsLastDump(String stack) {
            //noinspection StringEquality
            return (mLastDumpStack == stack) || (mLastDumpStack != null && mLastDumpStack.equals(stack));
        }

        private String dumpStack() {
            return BlockUtils.dumpStack(getDumpThread());
        }

        @Override
        public void run() {
            if (mCancel) {
                return;
            }
            recordTimes();
            //noinspection ConstantConditions
            if (mPostTimes < MAX_POST_TIMES || MAX_POST_TIMES == -1) {
                mWatchHandler.postDelayed(this, DUMP_STACK_DELAY_MILLIS);
            }

            String timeHead = newTimeHead();
            final String dumpStack = dumpStack();
            final boolean equalsLastDump = equalsLastDump(dumpStack);
            if (isTimeOut()) {
                String stack = "";
                // 超时打印
                if (isFirstTimeout()) {
                    // 取出前面的数据
                    stack = mStackInfo.toString();
                    mStackInfo.delete(0, mStackInfo.length());
                    // 前面数据也可能为空
                    if (!TextUtils.isEmpty(stack)) {
                        stack += PART_SEPARATOR;
                    }
                    stack += timeHead + dumpStack;
                } else {
                    if (!equalsLastDump) {
                        stack += timeHead + dumpStack;
                    }
                }
                // 非空打印
                if (!TextUtils.isEmpty(stack)) {
                    printStackTraceInfo(stack);
                }
            } else {
                if (!equalsLastDump) {
                    mStackInfo.append(PART_SEPARATOR).append(timeHead).append(dumpStack);
                }
            }

            mLastDumpStack = dumpStack;
        }

        @NonNull
        private Thread getDumpThread() {
            return mDumpThread;
        }

        private void printStackTraceInfo(String info) {
            String[] split = info.split(PART_SEPARATOR);
            for (String s : split) {
                if (!TextUtils.isEmpty(s)) {
                    Log.d(TAG, s + "\n");
                }
            }
        }
    }
}