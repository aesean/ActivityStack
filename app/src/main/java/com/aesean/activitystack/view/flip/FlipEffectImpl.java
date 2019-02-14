package com.aesean.activitystack.view.flip;

import android.view.View;

import androidx.annotation.NonNull;

public class FlipEffectImpl implements FlipEffect {

    @Override
    public void onUpdate(float degrees, @NonNull View view) {
        final int width = view.getWidth();
        final int height = view.getHeight();
        degrees = (degrees % 360f + 360f) % 360f;
        if (degrees >= 0 && degrees < 180) {
            final float progress = degrees / 180f;
            view.setPivotX(0);
            view.setPivotY(height >> 1);
            view.setRotationY(degrees);
            view.setTranslationX(progress * width);
        } else {
            final float progress = (degrees - 180) / 180f;
            view.setPivotX(width);
            view.setPivotY(height >> 1);
            view.setRotationY(degrees);
            view.setTranslationX(-(1 - progress) * width);
        }
    }

}
