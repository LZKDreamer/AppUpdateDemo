package com.lzk.appupdater.updater.net;

import java.io.File;

public interface INetManager {
    void get(String url,INetCallback callback,Object tag);
    void download(String url, File targetFile, INetDownloadCallback iNetDownload,Object tag);
    void cancel(Object tag);
}
