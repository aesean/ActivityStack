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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.aesean.activitystack.BuildConfig;
import com.aesean.activitystack.utils.ApplicationUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ShakeManager
 *
 * @author xl
 * @version V1.0
 * @since 23/12/2016
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ShakeManager implements ShakeDetector.ShakeListener {
    private static final String TAG = "ShakeManager";

    private ShakeDetector mShakeDetector;
    private final Context mContext;
    private final IGetActivity mGetActivity;

    private boolean mShowing = false;
    private long mLastShakeTime = -1;
    private static final long SHAKE_DELAY = 1000;

    public ShakeManager(final Context context) {
        this(context, new IGetActivity() {
            @Override
            public Activity getActivity() {
                return ApplicationUtils.getTopActivity();
            }
        });
    }

    public ShakeManager(Context context, IGetActivity getActivity) {
        if (context == null) {
            throw new NullPointerException("Context can not be null");
        }
        if (getActivity == null) {
            throw new NullPointerException("IGetActivity can not be null");
        }
        mContext = context.getApplicationContext();
        mGetActivity = getActivity;
    }

    public interface IGetActivity {
        Activity getActivity();
    }

    public void unregisterShakeDetector() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (mShakeDetector != null) {
            mShakeDetector.stop();
        }
    }

    public void registerShakeDetector() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (mShakeDetector == null) {
            mShakeDetector = new ShakeDetector(this);
        }
        mShakeDetector.start((SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE));
    }

    @Override
    public void onShake() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (mShowing) {
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (mLastShakeTime != -1 && currentTimeMillis - mLastShakeTime < SHAKE_DELAY) {
            return;
        }
        mLastShakeTime = currentTimeMillis;
        final Activity activity = mGetActivity.getActivity();
        if (activity == null) {
            new Exception("Shake fail, getActivity is null").printStackTrace();
            return;
        }
        final Map<String, Object> map = new LinkedHashMap<>();
        if (activity instanceof IRegisterShakeDetector) {
            ((IRegisterShakeDetector) activity).registerShakeDetector(map);
        }
        if (activity instanceof FragmentActivity) {
            @SuppressWarnings("RestrictedApi")
            List<Fragment> fragments = ((FragmentActivity) activity).getSupportFragmentManager().getFragments();
            if (fragments != null) {
                for (Fragment fragment : fragments) {
                    if (fragment instanceof IRegisterShakeDetector) {
                        ((IRegisterShakeDetector) fragment).registerShakeDetector(map);
                    }
                }
            }
        }
        if (map.size() < 1) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("开发者菜单");
        final String[] array = map.keySet().toArray(new String[map.size()]);
        builder.setItems(array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Object obj = map.get(array[which]);
                if (obj instanceof Class) {
                    try {
                        // 检查是否是Activity的子类
                        if (Activity.class.isAssignableFrom((Class) obj)) {
                            Intent intent = new Intent(activity, (Class) obj);
                            activity.startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (obj instanceof DialogInterface.OnClickListener) {
                    ((DialogInterface.OnClickListener) obj).onClick(dialog, which);
                }
                if (obj instanceof Runnable) {
                    ((Runnable) obj).run();
                }
            }
        });
        AlertDialog dialog = builder.create();
        mShowing = true;
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mShowing = false;
            }
        });
        dialog.show();
    }
}
