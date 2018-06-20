package com.wemakeprice.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class WebViewVerticalPagerActivity extends Activity implements OnWebViewScrollChangeListener, ViewSizeInterface {
    private BottomToolBarWebView mTopWebView;
    private BottomToolBarWebView mBottomWebView;

    private LinearLayout mToolBar;

    private Position mCurrentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_vertical_pager);

        initViews();
        initViewSize();
        loadUrl();
    }

    private void initViews() {
        mTopWebView = findViewById(R.id.top_web_view);
        mTopWebView.setPosition(Position.TOP);
        mTopWebView.setWebViewScrollChangeListener(this);

        mBottomWebView = findViewById(R.id.bottom_web_view);
        mBottomWebView.setPosition(Position.BOTTOM);
        mBottomWebView.setWebViewScrollChangeListener(this);

        mToolBar = findViewById(R.id.tool_bar);
        mToolBar.setBackgroundColor(Color.BLUE);
        mToolBar.setOnClickListener(v -> {
            if (mCurrentPosition == Position.TOP)
                setPageToBottom();
            else
                setPageToTop();
        });

        mCurrentPosition = Position.TOP;
    }

    private void initViewSize() {
        ViewGroup.LayoutParams layoutParams = mToolBar.getLayoutParams();
        layoutParams.height = getToolbarHeightPx(this);
        mToolBar.setLayoutParams(layoutParams);
    }

    private void loadUrl() {
        mTopWebView.loadUrl("https://m.zum.com/news/home?ra=#!/home");
    }

    @Override
    public void setTranslationY(float y) {
        if (mCurrentPosition == Position.TOP) {
            if (TextUtils.isEmpty(mBottomWebView.getUrl()))
                mBottomWebView.loadUrl("https://m.naver.com/");

            mBottomWebView.setTranslationY(y);
        } else {
            mTopWebView.setTranslationY(y);
        }
    }

    @Override
    public void getTranslationYRatio(float ratio) {
        float deltaY;

        if (ratio <= 0.5F) {
            deltaY = mToolBar.getHeight() * ratio * 2;
        } else {
            deltaY = mToolBar.getHeight() * (1.0F - ratio) * 2;
        }

        mToolBar.setTranslationY(deltaY);
    }

    @Override
    public void setPageToBottom() {
        mTopWebView.moveToBottom();
        mBottomWebView.moveToBottom();

        mCurrentPosition = Position.BOTTOM;

        mToolBar.setBackgroundColor(Color.GREEN);
    }

    @Override
    public void setPageToTop() {
        mTopWebView.moveToTop();
        mBottomWebView.moveToTop();

        mCurrentPosition = Position.TOP;

        mToolBar.setBackgroundColor(Color.BLUE);
    }
}
