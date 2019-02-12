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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

/**
 * LaunchActivityService
 *
 * @author xl
 * @version V1.0
 * @since 21/02/2017
 */
public class LaunchActivityService extends IntentService {
    private static final String KEY_TARGET_INTENT = "KEY_TARGET_INTENT";
    private static final String KEY_DELAY_MILLS = "KEY_DELAY_MILLS";

    public LaunchActivityService() {
        super("launch_activity_thread");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            throw new NullPointerException("intent不能为null，请使用" +
                    "LaunchActivityService.create方法创建LaunchActivityService");
        }
        long delay = intent.getLongExtra(KEY_DELAY_MILLS, 0);
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Intent target = intent.getParcelableExtra(KEY_TARGET_INTENT);
        if (target == null) {
            throw new NullPointerException("intent不能为null，请使用" +
                    "LaunchActivityService.create方法创建LaunchActivityService");
        }
        startActivity(target);
    }

    /**
     * 构建一个LaunchActivityService对象
     *
     * @param context    当前App的上下文
     * @param target     需要打开的目标Activity
     * @param delayMills 延迟启动，单位毫秒，只有大于0才会生效。
     * @return LaunchActivityService的Intent对象
     */
    public static Intent create(Context context, Intent target, long delayMills) {
        if (target == null) {
            throw new NullPointerException("intent不能为null");
        }
        Intent intent = new Intent(context, LaunchActivityService.class);
        intent.putExtra(KEY_TARGET_INTENT, target);
        intent.putExtra(KEY_DELAY_MILLS, delayMills);
        return intent;
    }
}
