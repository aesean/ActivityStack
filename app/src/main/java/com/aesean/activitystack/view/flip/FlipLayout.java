package com.aesean.activitystack.view.flip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class FlipLayout extends FrameLayout {

    private static final long DEFAULT_DURATION = 600;

    private int mCurrentShowViewIndex = -1;
    @NonNull
    private FlipEffect mFlipEffect = new FlipEffectImpl();
    @NonNull
    private AnimatorSet mAnimatorSet;
    @NonNull
    private ValueAnimator mFlipOutAnimator;
    @NonNull
    private ValueAnimator mFlipInAnimator;
    @Nullable
    private FlipListener mFlipListener;

    {
        mFlipOutAnimator = ValueAnimator.ofFloat(0, 90);
        mFlipOutAnimator.setDuration(DEFAULT_DURATION);
        mFlipOutAnimator.setInterpolator(new LinearInterpolator());
        mFlipOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mFlipEffect.onUpdate(value, getChildAt(mCurrentShowViewIndex));
                if (mFlipListener != null) {
                    mFlipListener.onFlipping(mCurrentShowViewIndex, value);
                }
            }
        });
        mFlipOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                for (int i = 0; i < getChildCount(); i++) {
                    if (i != mCurrentShowViewIndex) {
                        View view = getChildAt(i);
                        mFlipEffect.onUpdate(270, view);
                    }
                }
                if (mFlipListener != null) {
                    mFlipListener.onFlipOutStart(mCurrentShowViewIndex);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mFlipListener != null) {
                    mFlipListener.onFlipOutEnd(mCurrentShowViewIndex);
                }
                mCurrentShowViewIndex = nextFlipIndex();
            }
        });

        mFlipInAnimator = ValueAnimator.ofFloat(270, 360);
        mFlipInAnimator.setDuration(DEFAULT_DURATION);
        mFlipInAnimator.setInterpolator(new LinearInterpolator());
        mFlipInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mFlipEffect.onUpdate(value, getChildAt(mCurrentShowViewIndex));
                if (mFlipListener != null) {
                    mFlipListener.onFlipping(mCurrentShowViewIndex, value);
                }
            }
        });
        mFlipInAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (mFlipListener != null) {
                    mFlipListener.onFlipInStart(mCurrentShowViewIndex);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mFlipListener != null) {
                    mFlipListener.onFlipInEnd(mCurrentShowViewIndex);
                }
            }
        });

        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(mFlipOutAnimator, mFlipInAnimator);
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {

            int from;
            int to;

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                from = mCurrentShowViewIndex;
                to = nextFlipIndex();
                if (mFlipListener != null) {
                    mFlipListener.onFlipStart(from, to);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mFlipListener != null) {
                    mFlipListener.onFlipEnd(from, to);
                }
            }
        });
    }

    public FlipLayout(@NonNull Context context) {
        super(context);
    }

    public FlipLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FlipLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FlipLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private int nextFlipIndex() {
        return (mCurrentShowViewIndex + 1) % getChildCount();
    }

    public FlipEffect getFlipEffect() {
        return mFlipEffect;
    }

    public void setFlipEffect(@NonNull FlipEffect flipEffect) {
        mFlipEffect = flipEffect;
    }

    public long getFlipOutDuration() {
        return mFlipOutAnimator.getDuration();
    }

    public void setFlipOutDuration(long duration) {
        mFlipOutAnimator.setDuration(duration);
    }

    public long getFlipInDuration() {
        return mFlipInAnimator.getDuration();
    }

    public void setFlipInDuration(long duration) {
        mFlipInAnimator.setDuration(duration);
    }

    private void checkChildViewCount() {
        final int childCount = getChildCount();
        if (childCount < 2) {
            throw new IllegalStateException("childCount should be >= 2, childCount = " + childCount);
        }
    }

    public int getCurrentShowViewIndex() {
        return mCurrentShowViewIndex;
    }

    @NonNull
    public View getCurrentShowView() {
        return getChildAt(mCurrentShowViewIndex);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void pause() {
        mAnimatorSet.pause();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void resume() {
        mAnimatorSet.resume();
    }

    public void flip() {
        if (mAnimatorSet.isRunning()) {
            return;
        }
        checkChildViewCount();
        if (mCurrentShowViewIndex == -1) {
            mCurrentShowViewIndex = getChildCount() - 1;
        }
        mAnimatorSet.start();
    }

    public void setFlipListener(@Nullable FlipListener flipListener) {
        mFlipListener = flipListener;
    }

    public interface FlipListener {
        void onFlipStart(int from, int to);

        void onFlipInStart(int index);

        void onFlipOutStart(int index);

        void onFlipping(int index, float degree);

        void onFlipInEnd(int index);

        void onFlipOutEnd(int index);

        void onFlipEnd(int from, int to);
    }

    public static abstract class FlipListenerAdapter implements FlipListener {

        public FlipListenerAdapter() {
        }

        @Override
        public void onFlipStart(int from, int to) {

        }

        @Override
        public void onFlipInStart(int index) {

        }

        @Override
        public void onFlipOutStart(int index) {

        }

        @Override
        public void onFlipping(int index, float degree) {

        }

        @Override
        public void onFlipInEnd(int index) {

        }

        @Override
        public void onFlipOutEnd(int index) {

        }

        @Override
        public void onFlipEnd(int from, int to) {

        }
    }
}
