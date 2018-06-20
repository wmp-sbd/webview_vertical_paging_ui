package com.wemakeprice.app;

interface OnWebViewScrollChangeListener {
    void setTranslationY(float y);

    void getTranslationYRatio(float ratio);

    void setPageToBottom();

    void setPageToTop();
}
