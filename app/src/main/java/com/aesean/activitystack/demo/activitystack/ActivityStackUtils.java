package com.aesean.activitystack.demo.activitystack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ActivityStackUtils {

    private static final String TAG = "ActivityStackDetails";

    private ActivityStackUtils() {
    }

    public static boolean isResumed(@NonNull Activity activity) {
        try {
            Class<FragmentActivity> clazz = FragmentActivity.class;
            @SuppressWarnings("JavaReflectionMemberAccess")
            Field mResumedField = clazz.getField("mResumed");
            mResumedField.setAccessible(true);
            return (boolean) mResumedField.get(activity);
        } catch (@NonNull Exception e) {
            logE(e.getMessage());
            return false;
        }
    }

    @NonNull
    private static String getObjectInfo(@Nullable Object object) {
        if (object == null) {
            return "null";
        } else {
            return object.getClass().getSimpleName() + "@" + object.hashCode();
        }
    }

    @NonNull
    public static String getActivityStackDetails() {
        String s = getActivityStackDetails(null);
        for (String log : s.split("\n")) {
            log(log);
        }
        return s;
    }

    @NonNull
    public static String getActivityStackDetails(@Nullable String topActivityName) {
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder topActivityDetails = new StringBuilder();
        Map<Activity, ActivityStackUtils.Data> map = ActivityStackUtils.getAllActivityInfo();

        boolean checkTopOfStack = TextUtils.isEmpty(topActivityName);
        for (Map.Entry<Activity, ActivityStackUtils.Data> entry : map.entrySet()) {
            Activity activity = entry.getKey();
            ActivityStackUtils.Data data = entry.getValue();

            if (checkTopOfStack) {
                if (ActivityStackUtils.isResumed(activity)) {
                    topActivityDetails.append(data.toString());
                } else {
                    stringBuilder.append(data.toString());
                }
            } else {
                if (activity.getClass().getName().equals(topActivityName)) {
                    topActivityDetails.append(data.toString());
                } else {
                    stringBuilder.append(data.toString());
                }
            }
        }
        return topActivityDetails + stringBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static Map<Activity, Data> getAllActivityInfo() {
        Map<Activity, Data> map = new HashMap<>();
        try {
            @SuppressLint("PrivateApi")
            Class clazz = Class.forName("android.app.ActivityThread");
            @SuppressLint("DiscouragedPrivateApi")
            java.lang.reflect.Method method = clazz.getDeclaredMethod("currentActivityThread");
            method.setAccessible(true);
            Object activityThread = method.invoke(null);
            java.lang.reflect.Field filed = clazz.getDeclaredField("mActivities");
            filed.setAccessible(true);
            Object activities = filed.get(activityThread);
            Map<Object, Object> arrayMap = (Map<Object, Object>) activities;

            if (arrayMap == null) {
                return map;
            }
            for (Map.Entry<Object, Object> entry : arrayMap.entrySet()) {
                Object value = entry.getValue();
                Class<?> activityClientRecordClass = value.getClass();
                java.lang.reflect.Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(value);
                if (activity == null) {
                    continue;
                }

                Data data = new Data();
                data.activity = activity;

                android.app.FragmentManager fragmentManager = activity.getFragmentManager();
                java.lang.reflect.Field mAddedField = fragmentManager.getClass().getDeclaredField("mAdded");
                mAddedField.setAccessible(true);
                data.fragmentList = getFragments(fragmentManager);
                if (activity instanceof androidx.fragment.app.FragmentActivity) {
                    FragmentActivity fragmentActivity = (FragmentActivity) activity;
                    data.xFragmentList = fragmentActivity.getSupportFragmentManager().getFragments();
                }
                map.put(activity, data);
            }
        } catch (Exception e) {
            logW(e.getMessage());
        }
        return map;
    }

    @NonNull
    private static List<android.app.Fragment> getFragments(@NonNull FragmentManager fragmentManager) {
        try {
            java.lang.reflect.Field mAddedField = fragmentManager.getClass().getDeclaredField("mAdded");
            mAddedField.setAccessible(true);

            Object o = mAddedField.get(fragmentManager);
            if (o == null) {
                return new LinkedList<>();
            }
            //noinspection unchecked
            return new ArrayList<>((List<android.app.Fragment>) o);
        } catch (Exception e) {
            logW(e.getMessage());
        }
        return new LinkedList<>();
    }

    private static void logE(@Nullable String s) {
        Log.e(TAG, s == null ? "null" : s);
    }

    private static void logW(@Nullable String s) {
        Log.w(TAG, s == null ? "null" : s);
    }

    private static void log(@Nullable String s) {
        Log.d(TAG, s == null ? "null" : s);
    }

    public static class Data {

        @Nullable
        public Activity activity;
        @Nullable
        public List<Fragment> xFragmentList;
        @Nullable
        public List<android.app.Fragment> fragmentList;

        @NonNull
        public String format() {
            StringBuilder stringBuilder = new StringBuilder();
            final String activityInfo = getObjectInfo(activity);
            stringBuilder.append("\tActivity = ").append(activityInfo).append('\n');
            String prefix = "\t\t\t";
            stringBuilder.append(prefix).append("--------androidxFragment--------\n");
            if (xFragmentList == null || xFragmentList.isEmpty()) {
                stringBuilder.append(prefix).append("null\n");
            } else {
                stringBuilder.append(formatXFragments(prefix, xFragmentList));
            }

            stringBuilder.append(prefix).append("--------Fragment--------\n");
            if (fragmentList == null || fragmentList.isEmpty()) {
                stringBuilder.append(prefix).append("null\n");
            } else {
                stringBuilder.append(formatFragments(prefix, fragmentList));
            }
            return stringBuilder.toString();
        }

        @NonNull
        private String formatFragments(@NonNull String prefix, @NonNull List<android.app.Fragment> fragments) {
            StringBuilder stringBuilder = new StringBuilder();
            int index = -1;
            for (android.app.Fragment fragment : fragments) {
                stringBuilder.append(prefix).append(" (").append(++index).append(") \t").append(getObjectInfo(fragment)).append('\n');
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    List<android.app.Fragment> childFragments = getFragments(fragment.getChildFragmentManager());
                    stringBuilder.append(formatFragments(prefix + "\t\t", childFragments));
                }
            }
            return stringBuilder.toString();
        }

        @NonNull
        private String formatXFragments(@NonNull String prefix, @NonNull List<Fragment> fragments) {
            StringBuilder stringBuilder = new StringBuilder();
            int index = -1;
            for (Fragment fragment : fragments) {
                stringBuilder.append(prefix).append(" (").append(++index).append(") \t").append(getObjectInfo(fragment)).append('\n');
                List<Fragment> childFragments = fragment.getChildFragmentManager().getFragments();
                stringBuilder.append(formatXFragments(prefix + "\t\t", childFragments));
            }
            return stringBuilder.toString();
        }

        @NonNull
        @Override
        public String toString() {
            return format();
        }
    }
}
