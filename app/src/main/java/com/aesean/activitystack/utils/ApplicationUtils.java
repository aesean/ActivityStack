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

package com.aesean.activitystack.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ApplicationUtils
 * 当前类的代码用了大量反射，不保证一定是安全有效的。
 * 不建议在release状态调用这个类的任何功能，如果确实需要用到，请处理好调用异常。
 *
 * @author xl
 * @version V1.0
 * @since 06/01/2017
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public final class ApplicationUtils {

    public static List<Activity> getActivitiesByApplication(Application application) throws Exception {
        Object activities = getActivities(application);
        if (activities != null) {
            return toActivityList(activities);
        }
        return null;
    }

    public static Activity getTopActivity(Application application) throws Exception {
        ActivityManager activityManager = (ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE);
        String topActivity = null;
        String packageName = application.getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<ActivityManager.AppTask> appTasks = activityManager.getAppTasks();
            for (int i = 0, size = appTasks.size(); i < size; i++) {
                ActivityManager.RecentTaskInfo taskInfo = appTasks.get(size - 1 - i).getTaskInfo();
                if (packageName.equals(taskInfo.baseActivity.getPackageName())) {
                    topActivity = taskInfo.topActivity.getClassName();
                }
            }
        }
        if (TextUtils.isEmpty(topActivity)) {
            @SuppressWarnings("deprecation")
            List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(10);
            for (ActivityManager.RunningTaskInfo runningTask : runningTasks) {
                if (packageName.equals(runningTask.topActivity.getPackageName())) {
                    topActivity = runningTask.topActivity.getClassName();
                    break;
                }
            }
        }
        if (TextUtils.isEmpty(topActivity)) return null;
        List<Activity> activities = getActivitiesByApplication(application);
        if (activities != null) {
            for (Activity activity : activities) {
                if (topActivity.equals(activity.getClass().getName())) {
                    return activity;
                }
            }
        }
        return null;
    }

    public static Object getActivities(Application application) throws Exception {
        return ReflectUtils.reflect(application, "mLoadedApk.mActivityThread.mActivities");
//            Class<Application> applicationClass = Application.class;
//            Field mLoadedApkField = applicationClass.getDeclaredField("mLoadedApk");
//            mLoadedApkField.setAccessible(true);
//            Object mLoadedApk = mLoadedApkField.get(application);
//            Class<?> mLoadedApkClass = mLoadedApk.getClass();
//            Field mActivityThreadField = mLoadedApkClass.getDeclaredField("mActivityThread");
//            mActivityThreadField.setAccessible(true);
//            Object mActivityThread = mActivityThreadField.get(mLoadedApk);
//            Class<?> mActivityThreadClass = mActivityThread.getClass();
//            Field mActivitiesField = mActivityThreadClass.getDeclaredField("mActivities");
//            mActivitiesField.setAccessible(true);
//            return mActivitiesField.get(mActivityThread);
    }

    public static List<Object> getActivitiesKeyList(Application application) throws Exception {
        Object activities = getActivities(application);
        return toKeyList(activities);
    }

    private static List<Object> toKeyList(Object mActivities) {
        if (mActivities == null) {
            throw new NullPointerException("mActivities can't be null");
        }
        List<Object> list = new ArrayList<>();
        if (mActivities instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> arrayMap = (Map<Object, Object>) mActivities;
            for (Map.Entry<Object, Object> entry : arrayMap.entrySet()) {
                Object key = entry.getKey();
                list.add(key);
            }
        }
        return list;
    }

    public static List<Object> getActivitiesValueList(Application application) throws Exception {
        Object activities = getActivities(application);
        return toValueList(activities);
    }

    private static List<Object> toValueList(Object mActivities) throws Exception {
        if (mActivities == null) {
            throw new NullPointerException("mActivities can't be null");
        }
        List<Object> list = new ArrayList<>();
        if (mActivities instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> arrayMap = (Map<Object, Object>) mActivities;
            for (Map.Entry<Object, Object> entry : arrayMap.entrySet()) {
                Object value = entry.getValue();
                list.add(value);
            }
        }
        return list;
    }

    private static List<Activity> toActivityList(Object mActivities) throws Exception {
        if (mActivities == null) {
            return null;
        }
        List<Activity> list = new ArrayList<>();
        if (mActivities instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> arrayMap = (Map<Object, Object>) mActivities;
            for (Map.Entry<Object, Object> entry : arrayMap.entrySet()) {
                Object value = entry.getValue();
                Object o = ReflectUtils.reflect(value, "activity");
//                    Class<?> activityClientRecordClass = value.getClass();
//                    Field activityField = activityClientRecordClass.getDeclaredField("activity");
//                    activityField.setAccessible(true);
//                    Object o = activityField.get(value);
                list.add((Activity) o);
            }
        }
        return list;
    }
}
