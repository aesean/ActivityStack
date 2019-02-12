package com.aesean.activitystack.utils;

import android.os.Build;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Printer;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public final class LooperProxy {
    private Looper mLooper;
    private List<Printer> mPrinterList = new LinkedList<>();

    public LooperProxy(Looper looper) {
        mLooper = looper;
        mLooper.setMessageLogging(new Printer() {
            @Override
            public void println(String x) {
                for (Printer printer : mPrinterList) {
                    printer.println(x);
                }
            }
        });
    }

    public Looper getProxyLooper() {
        return mLooper;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean isCurrentThread() {
        return mLooper.isCurrentThread();
    }

    public void removeMessageLogging(@NonNull Printer printer) {
        mPrinterList.remove(printer);
    }

    public void addMessageLogging(@NonNull Printer printer) {
        mPrinterList.add(printer);
    }

    public void quit() {
        mLooper.quit();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void quitSafely() {
        mLooper.quitSafely();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public MessageQueue getQueue() {
        return mLooper.getQueue();
    }

    @NonNull
    public Thread getThread() {
        return mLooper.getThread();
    }

    public void dump(@NonNull Printer pw, @NonNull String prefix) {
        mLooper.dump(pw, prefix);
    }
}
