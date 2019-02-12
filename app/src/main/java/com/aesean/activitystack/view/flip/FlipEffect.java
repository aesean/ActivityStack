package com.aesean.activitystack.view.flip;

import android.view.View;

import androidx.annotation.NonNull;

public interface FlipEffect {

    void onUpdate(float degrees, @NonNull View view);

}
