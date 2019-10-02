package com.lzk.appupdater.updater;

import com.lzk.appupdater.updater.net.INetManager;
import com.lzk.appupdater.updater.net.OkHttpNetManager;

public class AppUpdater {

    private static AppUpdater sAppUpdater = new AppUpdater();

    public static AppUpdater getInstance(){
        return sAppUpdater;
    }

    private INetManager mINetManager = new OkHttpNetManager();

    public INetManager getINetManager(){
        return mINetManager;
    }

    public void setNetManager(INetManager iNetManager){
        mINetManager = iNetManager;
    }

}
