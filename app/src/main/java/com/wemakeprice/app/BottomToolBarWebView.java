package com.wemakeprice.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

public class BottomToolBarWebView extends WebView implements ViewSizeInterface, View.OnTouchListener {
    private static final int TRANSLATE_Y_DURATION = 200;
    private static final int DRAG_THRESHOLD = 2;
    private static final int RELEASE_THRESHOLD = 16;

    private Position mPosition;
    private OnWebViewScrollChangeListener mOnWebViewScrollChangeListener;

    private int mPrevAction = MotionEvent.ACTION_DOWN;

    private float mWebViewDeltaY;
    private float mPrevY = -1.0F;

    private boolean mPaging = false;
    private boolean mScrollableState = false;

    public BottomToolBarWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public void setPosition(Position position) {
        mPosition = position;
    }

    public void setWebViewScrollChangeListener(OnWebViewScrollChangeListener listener) {
        mOnWebViewScrollChangeListener = listener;
    }

    public void moveToBottom() {
        moveWebViewAnimation(getHeight() * -1.0F);
    }

    public void moveToTop() {
        moveWebViewAnimation(0);
    }

    private void moveWebViewAnimation(float deltaY) {
        ObjectAnimator translateTopY = ObjectAnimator.ofFloat(this, "translationY", deltaY);
        translateTopY.setDuration(TRANSLATE_Y_DURATION);
        translateTopY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                getTranslationYRatio();
            }
        });
        translateTopY.addUpdateListener(animation -> getTranslationYRatio());
        translateTopY.start();
    }

    private void getTranslationYRatio() {
        if (mPosition == Position.TOP && mOnWebViewScrollChangeListener != null) {
            mOnWebViewScrollChangeListener.getTranslationYRatio(getMoveRatio());
        }
    }

    private float removeMalocclusionTranslation(float toMoveValue) {
        if (toMoveValue > 0)
            return 0;
        else if (toMoveValue < getHeight() * -1.0F)
            return getHeight() * -1.0F;
        else
            return toMoveValue;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight(getContext()));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Scroll scrollState = mPrevY > event.getY() ? Scroll.DOWN : Scroll.UP;
        mPrevY = event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mWebViewDeltaY = event.getRawY() - getTranslationY();
            mScrollableState = getScrollableState();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mPaging) {
                setWebViewPosition();
                return true;
            }
        }

        boolean prevPagingState = mPaging;

        mPaging = getPagingState(scrollState);

        if (event.getAction() == MotionEvent.ACTION_MOVE && mPrevAction == MotionEvent.ACTION_MOVE && prevPagingState) {
            mPaging = true;
        }

        mPrevAction = event.getAction();

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mPaging && mScrollableState)
                moveWebViewByTouch(event.getRawY());
            return mPaging && mScrollableState;
        } else {
            return false;
        }
    }

    private boolean getPagingState(Scroll scrollState) {
        if (mPosition == Position.TOP) {
            return scrollState == Scroll.DOWN && !canScrollVertically(1);
        } else {
            return scrollState == Scroll.UP && !canScrollVertically(-1);
        }
    }

    private boolean getScrollableState() {
        if (mPosition == Position.TOP) {
            return !canScrollVertically(1);
        } else {
            return !canScrollVertically(-1);
        }
    }

    private void setWebViewPosition() {
        float deltaY = getHeight() / RELEASE_THRESHOLD;

        if (mOnWebViewScrollChangeListener != null) {
            if (mPosition == Position.TOP) {
                if (Math.abs(getTranslationY()) > deltaY) {
                    mOnWebViewScrollChangeListener.setPageToBottom();
                } else {
                    mOnWebViewScrollChangeListener.setPageToTop();
                }
            } else {
                if (getHeight() + getTranslationY() < deltaY) {
                    mOnWebViewScrollChangeListener.setPageToBottom();
                } else {
                    mOnWebViewScrollChangeListener.setPageToTop();
                }
            }
        }
    }

    private void moveWebViewByTouch(float rawY) {
        if (canMoveTopOrBottom()) {
            float toMoveValue = rawY - mWebViewDeltaY;

            float newToMoveValue = removeMalocclusionTranslation(toMoveValue);
            float deltaY;

            if (mPosition == Position.TOP) {
                deltaY = newToMoveValue / DRAG_THRESHOLD;
            } else {
                deltaY = -getHeight() - (((getTranslationY() - newToMoveValue)) / DRAG_THRESHOLD);
            }

            setTranslationY(deltaY);

            if (mOnWebViewScrollChangeListener != null) {
                mOnWebViewScrollChangeListener.setTranslationY(deltaY);
                mOnWebViewScrollChangeListener.getTranslationYRatio(getMoveRatio());
            }
        }
    }

    private float getMoveRatio() {
        float ratio = getTranslationY() / getHeight();

        return Math.abs(ratio);
    }

    private boolean canMoveTopOrBottom() {
        return !(getTranslationY() > 0) && !(getTranslationY() < getHeight() * -1.0F);
    }

    private enum Scroll {
        DOWN, UP
    }
}
