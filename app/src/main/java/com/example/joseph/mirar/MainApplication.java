package com.example.joseph.mirar;

import android.app.Application;
import android.content.Context;

import org.artoolkit.ar.base.assets.AssetHelper;

/**
 * Created by Joseph on 2016-08-29.
 */
public class MainApplication extends Application {

    private static Application sInstance;

    public static Application getInstance() {
        return sInstance;
    }

    private static Context mContext;

    public Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        mContext = getContext();
        ((MainApplication) sInstance).initializeInstance();
    }

    protected void initializeInstance() {
        AssetHelper assetHelper = new AssetHelper(getAssets());
        assetHelper.cacheAssetFolder(getInstance(), "Data");
    }
}
