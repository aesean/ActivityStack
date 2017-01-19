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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.aesean.activitystack.utils.shake.IRegisterShakeDetector;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements IRegisterShakeDetector {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }
}
