package com.wemakeprice.app;

import android.content.Context;
import android.util.TypedValue;

interface ViewSizeInterface {
    int TOOLBAR_HEIGHT_DP = 50;

    default int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }

    default int getMeasuredHeight(Context context) {
        int displayHeight = context.getResources().getDisplayMetrics().heightPixels;
        int statusBarHeight = getStatusBarHeight(context);

        return displayHeight - statusBarHeight - getToolbarHeightPx(context);
    }

    default int getToolbarHeightPx(Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TOOLBAR_HEIGHT_DP, context.getResources().getDisplayMetrics());
    }
}
