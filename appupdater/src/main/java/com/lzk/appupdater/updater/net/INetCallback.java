package com.lzk.appupdater.updater.net;

public interface INetCallback {
    void success(String response);
    void failed(Throwable throwable);
}
