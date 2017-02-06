/*
 *    Copyright Aesean
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

import android.app.Application;

import com.aesean.activitystack.utils.LifecycleUtils;
import com.aesean.activitystack.utils.shake.ShakeManager;

/**
 * AppApplication
 *
 * @author xl
 * @version V1.0
 * @since 19/01/2017
 */
public class AppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            registerDebugService();
        }
    }

    private void registerDebugService() {
        // 下面这个写法是如果开启了minifyEnabled true，则编译时候下面代码会自动优化。
        // 因为BuildConfig.DEBUG编译期会自动被修改为
        // public static final boolean DEBUG = false;
        // 也就是说if后面的代码永远都不会被执行到，编译器会自动删除正常情况下跑不到的代码。
        // 同时如果所有调用LifecycleUtils和ShakeManager的地方都做了类似处理，
        // 那么LifecycleUtils和ShakeManager则永远不会访问到，也会导致这两个类被自动删除。
        // 下面写法实际效果就是，debug时候下面代码全部存在，LifecycleUtils和ShakeManager类也存在，
        // 但是在release的时候，下面代码，包括LifecycleUtils和ShakeManager类都会被删除。
        // 建议自己反编译release和debug生成的apk，实际对比看下效果。
        if (!BuildConfig.DEBUG) {
            return;
        }
        new LifecycleUtils(this).register();
        new ShakeManager(this).registerShakeDetector();
        BlockUtils.getInstance().install();
    }
}
