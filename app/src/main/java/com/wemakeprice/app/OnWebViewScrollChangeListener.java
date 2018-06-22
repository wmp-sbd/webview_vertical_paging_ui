package com.wemakeprice.app;

/**
 * @author ByunghooLim
 *
 * BottomToolBarWebView에서 발행하는 이벤트 전달 interface
 */
interface OnWebViewScrollChangeListener {
    /**
     * @param y BottomToolBarWebView의 TranslationY 값
     */
    void setTranslationY(float y);

    /**
     * @param ratio BottomToolBarWebView가 이동한 거리를에 따른 비율 값
     * 초기값 0.0F, 완전히 이동했을때 1.0F
     */
    void getTranslationYRatio(float ratio);

    /**
     * 페이지를 Bottom으로 설정
     */
    void setPageToBottom();

    /**
     * 페이지를 Top으로 설정
     */
    void setPageToTop();
}
