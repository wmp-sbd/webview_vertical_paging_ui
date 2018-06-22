package com.wemakeprice.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * @author ByunghooLim
 *
 * Touch 이벤트를 통해 웹뷰내의 contents를 스트롤과 웹뷰 자체 tranlation Y를 수행하고,
 * OnWebViewScrollChangeListener 구현을 통해 이벤트 콜백 처리를 수행한다.
 */
public class BottomToolBarWebView extends WebView implements ViewSizeInterface, View.OnTouchListener {
    /**
     * tranlation Y 애니메이션 duration.
     * 값이 클수록 전환이 느려진다.
     */
    private static final int TRANSLATE_Y_DURATION = 200;
    /**
     * Touch로 translationY를 할때 sticky한 감을 주기 위한 값.
     * 값이 클수록 sticky 강도가 강해짐.
     */
    private static final int DRAG_THRESHOLD = 2;
    /**
     * Touch로 translationY를 할때 얼마만큼 페이징 해야 페이지 이동을 할지를 판단 하는 값
     * 값이 클수록 조금 만 translationY 해도 페이징 함.
     */
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

    /**
     * 웹뷰의 포지션 설정
     *
     * @see Position
     */
    public void setPosition(Position position) {
        mPosition = position;
    }

    /**
     * OnWebViewScrollChangeListener 리스너 설정
     *
     * @see OnWebViewScrollChangeListener
     */
    public void setWebViewScrollChangeListener(OnWebViewScrollChangeListener listener) {
        mOnWebViewScrollChangeListener = listener;
    }

    /**
     * 웹뷰를 TRANSLATE_Y_DURATION 동안 animation으로 Bottom으로 이동
     */
    public void moveToBottom() {
        moveWebViewAnimation(getHeight() * -1.0F);
    }

    /**
     * 웹뷰를 TRANSLATE_Y_DURATION 동안 animation으로 Top으로 이동
     */
    public void moveToTop() {
        moveWebViewAnimation(0);
    }

    /**
     * 웹뷰를 TRANSLATE_Y_DURATION 동안 animation으로 deltaY만큼 이동
     *
     * @param deltaY 이동 할 Y값
     */
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

    /**
     * 웹뷰가 이동한 거리를 0.0F ~ 1.0F사이로 계산해서 OnWebViewScrollChangeListener를 통해 callback으로 호출해 준다.
     * 초기값 0.0F, 완전히 이동했을때 1.0F
     */
    private void getTranslationYRatio() {
        if (mPosition == Position.TOP && mOnWebViewScrollChangeListener != null) {
            mOnWebViewScrollChangeListener.getTranslationYRatio(getMoveRatio());
        }
    }

    /**
     * @return 웹뷰 이동한 거리가 0을 넘어섰거나, 웹뷰의 높이에 -1.0F을 곱한값 보다 작아 졌을 경우 값을 보정해 준다.
     */
    private float removeMalocclusionTranslation(float toMoveValue) {
        if (toMoveValue > 0)
            return 0;
        else if (toMoveValue < getHeight() * -1.0F)
            return getHeight() * -1.0F;
        else
            return toMoveValue;
    }

    /**
     * 최초 웹뷰 크기가 설정 될 때, 하단 툴바의 영역을 고려하여 웹뷰의 높이를 재설정 한다
     *
     * @see ViewSizeInterface getMeasuredHeight(Context context)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight(getContext()));
    }

    /**
     * onTouch 이벤트를 override 하여
     * MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP, MotionEvent,ACTION_MOVE event에 따른 이벤트 처리를 해준다.
     * <p>
     * getPagingState(Scroll scrollState)에 따라 웹뷰 content가 스크롤 되야 하는지, 웹뷰 자체가 페이징 되어야 하는지를 판단하여
     * mPaging에 저장한 후 그에 따라 동작을 처리 한다.
     */
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

    /**
     * @return 현재 포지션에 따라 웹뷰 contents를 스크롤 할지 , 웹뷰 자체를 페이징 할지를 반환 한다.
     */
    private boolean getPagingState(Scroll scrollState) {
        if (mPosition == Position.TOP) {
            return scrollState == Scroll.DOWN && !canScrollVertically(1);
        } else {
            return scrollState == Scroll.UP && !canScrollVertically(-1);
        }
    }

    /**
     * @return 현재 포지션에 따라 웹뷰 contents가 최대로 스크롤 되었는지 여부를 반환 한다.
     */
    private boolean getScrollableState() {
        if (mPosition == Position.TOP) {
            return !canScrollVertically(1);
        } else {
            return !canScrollVertically(-1);
        }
    }


    /**
     * 웹뷰가 paging 되어 MotionEvent.ACTION_UP 이벤트 발생시 RELEASE_THRESHOLD 값에 따라 페이지 이동을 할지,
     * 아니면 원래 자리로 돌아 올지 판단하여 이동을 수행 한다.
     */
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

    /**
     * 웹뷰가 Top이나 Bottom으로 이동가능 한지 여부에 따라, rawY에 따라 웹뷰를 이동 시킨다.
     * @param rawY event rawY값
     */
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

    /**
     * @return 웹뷰의 높이에 따른 이동한 거리의 비율을 반환 (범위 : 0.0F ~ 1.0F)
     */
    private float getMoveRatio() {
        float ratio = getTranslationY() / getHeight();

        return Math.abs(ratio);
    }

    /**
     * @return  웹뷰가 Top이나 Bottom으로 추가로 translationY 할 수 있는지 반환
     */
    private boolean canMoveTopOrBottom() {
        return !(getTranslationY() > 0) && !(getTranslationY() < getHeight() * -1.0F);
    }

    /**
     * 웹뷰 스크롤 방향 enum
     */
    private enum Scroll {
        DOWN, UP
    }
}
