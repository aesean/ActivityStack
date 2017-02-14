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
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * LifecycleUtils
 *
 * @author xl
 * @version V1.0
 * @since 29/12/2016
 */
@SuppressWarnings("unused")
public class LifecycleUtils {
    private String mTag;
    private Application mApplication;

    public static class Builder {
        private String tag = "LifecycleUtils";
        private Application application;

        public void setTag(String tag) {
            this.tag = tag;
        }

        public void setApplication(Application application) {
            this.application = application;
        }

        public LifecycleUtils build() {
            if (application == null) {
                try {
                    application = ApplicationUtils.getApplication();
                } catch (ReflectUtils.ReflectException e) {
                    throw new RuntimeException(e);
                }
            }
            return new LifecycleUtils(application, tag);
        }
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    private LifecycleUtils(Application application, String tag) {
        if (application == null) {
            throw new NullPointerException("application can not be null");
        }
        mApplication = application;
        mTag = tag;
    }

    private String toString(Object... objects) {
        return Arrays.toString(objects);
    }

    private void print(String msg, Object... objects) {
        Log.d(mTag, msg + ": " + toString(objects));
    }

    public void register() {
        mApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            private WeakReference<FragmentManager.FragmentLifecycleCallbacks> mWeakReference;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                print("onActivityCreated", activity, savedInstanceState);
                // 一定注意不要强引用Activity或者Fragment
                mWeakReference = new WeakReference<>(registerFragmentLifecycleCallback(activity));
            }

            @Override
            public void onActivityStarted(Activity activity) {
                print("onActivityStarted", activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                print("onActivityResumed", activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                print("onActivityPaused", activity);
            }

            @Override
            public void onActivityStopped(Activity activity) {
                print("onActivityStopped", activity);
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                print("onActivitySaveInstanceState", activity, outState);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                print("onActivityStarted", activity);
                if (mWeakReference != null) {
                    unregisterFragmentLifecycleCallback(activity, mWeakReference.get());
                    mWeakReference.clear();
                }
            }
        });
    }

    private void unregisterFragmentLifecycleCallback(Activity activity
            , FragmentManager.FragmentLifecycleCallbacks callbacks) {
        if (callbacks != null && activity instanceof FragmentActivity) {
            FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            supportFragmentManager.unregisterFragmentLifecycleCallbacks(callbacks);
        }
    }

    private FragmentManager.FragmentLifecycleCallbacks registerFragmentLifecycleCallback(Activity activity) {
        if (activity instanceof FragmentActivity) {
            FragmentManager supportFragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks
                    = supportFragmentManager.new FragmentLifecycleCallbacks() {

                @Override
                public void onFragmentPreAttached(FragmentManager fm, Fragment f, Context context) {
                    super.onFragmentPreAttached(fm, f, context);
                    print("onFragmentPreAttached", f, context);
                }

                @Override
                public void onFragmentAttached(FragmentManager fm, Fragment f, Context context) {
                    super.onFragmentAttached(fm, f, context);
                    print("onFragmentAttached", f, context);
                }

                @Override
                public void onFragmentCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                    super.onFragmentCreated(fm, f, savedInstanceState);
                    print("onFragmentCreated", f, savedInstanceState);
                }

                @Override
                public void onFragmentActivityCreated(FragmentManager fm, Fragment f, Bundle savedInstanceState) {
                    super.onFragmentActivityCreated(fm, f, savedInstanceState);
                    print("onFragmentActivityCreated", f, savedInstanceState);
                }

                // @Override
                // public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
                //     super.onFragmentViewCreated(fm, f, v, savedInstanceState);
                //     Log.d(mTag, "onFragmentViewCreated: " + LifecycleUtils.toString(f));
                // }

                @Override
                public void onFragmentStarted(FragmentManager fm, Fragment f) {
                    super.onFragmentStarted(fm, f);
                    print("onFragmentActivityCreated", f);
                }

                @Override
                public void onFragmentResumed(FragmentManager fm, Fragment f) {
                    super.onFragmentResumed(fm, f);
                    print("onFragmentResumed", f);
                }

                @Override
                public void onFragmentPaused(FragmentManager fm, Fragment f) {
                    super.onFragmentPaused(fm, f);
                    print("onFragmentPaused", f);
                }

                @Override
                public void onFragmentStopped(FragmentManager fm, Fragment f) {
                    super.onFragmentStopped(fm, f);
                    print("onFragmentStopped", f);
                }

                @Override
                public void onFragmentSaveInstanceState(FragmentManager fm, Fragment f, Bundle outState) {
                    super.onFragmentSaveInstanceState(fm, f, outState);
                    print("onFragmentSaveInstanceState", f, outState);
                }

                @Override
                public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
                    super.onFragmentViewDestroyed(fm, f);
                    print("onFragmentViewDestroyed", f);
                }

                @Override
                public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
                    super.onFragmentDestroyed(fm, f);
                    print("onFragmentDestroyed", f);
                }

                @Override
                public void onFragmentDetached(FragmentManager fm, Fragment f) {
                    super.onFragmentDetached(fm, f);
                    print("onFragmentDetached", f);
                }
            };
            supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);
            return fragmentLifecycleCallbacks;
        }
        return null;
    }
}

