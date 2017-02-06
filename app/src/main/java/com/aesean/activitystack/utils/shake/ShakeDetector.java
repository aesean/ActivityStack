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

package com.aesean.activitystack.utils.shake;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Listens for the user shaking their phone. Allocation-less once it starts listening.
 * <p>
 * 注意：This Class is copied from Facebook ReactNative(package com.facebook.react.common.ShakeDetector)
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ShakeDetector implements SensorEventListener {
    private static final String TAG = "ShakeDetector";
    private static final int MAX_SAMPLES = 25;
    private static final int MIN_TIME_BETWEEN_SAMPLES_MS = 20;
    private static final int VISIBLE_TIME_RANGE_MS = 500;
    private static final int MAGNITUDE_THRESHOLD = 25;
    private static final int PERCENT_OVER_THRESHOLD_FOR_SHAKE = 66;

    public interface ShakeListener {
        void onShake();
    }

    private final ShakeListener mShakeListener;

    private SensorManager mSensorManager;
    private long mLastTimestamp;
    private int mCurrentIndex;
    private double[] mMagnitudes;
    private long[] mTimestamps;

    public ShakeDetector(ShakeListener listener) {
        mShakeListener = listener;
    }

    /**
     * Start listening for shakes.
     */
    public void start(SensorManager manager) {
        Sensor accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            mSensorManager = manager;
            mLastTimestamp = -1;
            mCurrentIndex = 0;
            mMagnitudes = new double[MAX_SAMPLES];
            mTimestamps = new long[MAX_SAMPLES];

            mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.e(TAG, "错误，当前手机无法获取：ACCELEROMETER传感器，摇一摇功能异常。");
        }
    }

    /**
     * Stop listening for shakes.
     */
    public void stop() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.timestamp - mLastTimestamp < MIN_TIME_BETWEEN_SAMPLES_MS) {
            return;
        }

        float ax = sensorEvent.values[0];
        float ay = sensorEvent.values[1];
        float az = sensorEvent.values[2];

        mLastTimestamp = sensorEvent.timestamp;
        mTimestamps[mCurrentIndex] = sensorEvent.timestamp;
        mMagnitudes[mCurrentIndex] = Math.sqrt(ax * ax + ay * ay + az * az);

        maybeDispatchShake(sensorEvent.timestamp);

        mCurrentIndex = (mCurrentIndex + 1) % MAX_SAMPLES;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private void maybeDispatchShake(long currentTimestamp) {

        int numOverThreshold = 0;
        int total = 0;
        for (int i = 0; i < MAX_SAMPLES; i++) {
            int index = (mCurrentIndex - i + MAX_SAMPLES) % MAX_SAMPLES;
            if (currentTimestamp - mTimestamps[index] < VISIBLE_TIME_RANGE_MS) {
                total++;
                if (mMagnitudes[index] >= MAGNITUDE_THRESHOLD) {
                    numOverThreshold++;
                }
            }
        }

        if (((double) numOverThreshold) / total > PERCENT_OVER_THRESHOLD_FOR_SHAKE / 100.0) {
            mShakeListener.onShake();
        }
    }
}