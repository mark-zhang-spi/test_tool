package com.digilock.pivot_test_util.utils;

import android.content.Context;
import android.content.res.Configuration;

public class Helper {
    public static boolean isLargeTablet(Context context){
        Configuration config = context.getResources().getConfiguration();
        return config.smallestScreenWidthDp >= 800;
    }
}