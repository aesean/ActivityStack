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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aesean.activitystack.utils.ApplicationUtils;
import com.aesean.activitystack.utils.shake.IRegisterShakeDetector;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements IRegisterShakeDetector {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void registerShakeDetector(Map<String, Object> map) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        // release的时候，后面的代码会被自动删除
        map.put("打开SecondActivity", SecondActivity.class);
        map.put("自动填充姓名", new Runnable() {
            @Override
            public void run() {
                EditText editText = (EditText) findViewById(R.id.user_name);
                editText.setText("自动填充的姓名");
            }
        });
        map.put("自动登录", new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "模拟自动登录，可以安全放心的把账号密码写到这里，" +
                        "发布release的时候，这里的代码会被自动删除。", Toast.LENGTH_LONG).show();
            }
        });
        map.put("重启App（Alarm）", new Runnable() {
            @Override
            public void run() {
                ((AppApplication) getApplication()).restart();
            }
        });
        map.put("重启App（Service）", new Runnable() {
            @Override
            public void run() {
                restartApplicationByService(MainActivity.this, 500);
            }
        });
        map.put("TopActivity", new Runnable() {
            @Override
            public void run() {
                Activity topActivity = ApplicationUtils.getTopActivity();
                String msg = null;
                if (topActivity != null) {
                    msg = topActivity.getClass().getName();
                }
                Toast.makeText(MainActivity.this, "TopActivity is " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private static void restartApplicationByService(Context context, long delay) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Intent service = LaunchActivityService.create(context, intent, delay);
        context.startService(service);
        Process.killProcess(Process.myPid());
    }

    public void block_200ms(View view) {
        block(200);
        showBlockTips();
    }

    public void block_400ms(View view) {
        block(400);
        showBlockTips();
    }

    public void block_800ms(View view) {
        block(800);
        showBlockTips();
    }

    public void block_1200ms(View view) {
        block(1200);
        showBlockTips();
    }

    public void block_2000ms(View view) {
        block(1200);
        block(400);
        block(400);
        showBlockTips();
    }

    public void block_5000ms(View view) {
        block(2000);
        block(2000);
        block(800);
        block(200);
        showBlockTips();
    }

    private void showBlockTips() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (Debug.isDebuggerConnected()) {
            return;
        }
        String msg = "打开Logcat，设置过滤级别为：DEBUG，过滤字符串为：BlockUtils，" +
                "可以看到BlockUtils检测到的卡顿代码位置。";
        Log.w(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void block(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
