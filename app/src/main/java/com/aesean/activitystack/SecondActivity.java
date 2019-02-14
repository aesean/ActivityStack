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
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.aesean.activitystack.utils.ApplicationUtils;
import com.aesean.activitystack.utils.shake.IRegisterShakeDetector;
import com.aesean.activitystack.view.flip.FlipLayout;

import java.util.Map;

public class SecondActivity extends AppCompatActivity implements IRegisterShakeDetector {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }

    @Override
    public void registerShakeDetector(Map<String, Object> map) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        map.put("TopActivity", new Runnable() {
            @Override
            public void run() {
                Activity topActivity = ApplicationUtils.getTopActivity();
                String msg = null;
                if (topActivity != null) {
                    msg = topActivity.getClass().getName();
                }
                Toast.makeText(SecondActivity.this, "TopActivity is " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
