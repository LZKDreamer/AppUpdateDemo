package com.lzk.appupdater.updater.net;

import android.os.Handler;
import android.os.Looper;

import com.lzk.appupdater.updater.net.INetCallback;
import com.lzk.appupdater.updater.net.INetDownloadCallback;
import com.lzk.appupdater.updater.net.INetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpNetManager implements INetManager {

    private static OkHttpClient sOkHttpClient;

    static {
        OkHttpClient.Builder builder =new OkHttpClient.Builder();
        builder.connectTimeout(15,TimeUnit.SECONDS);
        sOkHttpClient = builder.build();
    }

    private static Handler sHandler = new Handler(Looper.getMainLooper());


    @Override
    public void get(String url, final INetCallback callback,Object tag) {
        Request.Builder builder = new Request.Builder();
        final Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.failed(e);
                    }
                });
            }

            @Override
            public void onResponse( Call call, Response response) throws IOException {

                try {
                    final String s = response.body().string();
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.success(s);
                        }
                    });
                }catch (Throwable e){
                    e.printStackTrace();
                    callback.failed(e);
                }

            }
        });
    }

    @Override
    public void download(String url, final File targetFile, final INetDownloadCallback iNetDownload
            ,Object tag) {
        if (!targetFile.exists()){
            targetFile.getParentFile().mkdirs();
        }
        Request.Builder builder = new Request.Builder();
        final Request request = builder.url(url).get().tag(tag).build();
        Call call = sOkHttpClient.newCall(request);
        call.enqueue(new Callback() {

            @Override
            public void onFailure(Call call, final IOException e) {
                sHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        iNetDownload.failed(e);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                OutputStream os = null;
                try{
                    final long totalLen = response.body().contentLength();
                    is = response.body().byteStream();
                    os = new FileOutputStream(targetFile);
                    byte[] buffer = new byte[8*1024];
                    long curLen = 0;
                    int bufferLen = 0;

                    while (!call.isCanceled() && (bufferLen = is.read(buffer)) != -1){
                        os.write(buffer,0,bufferLen);
                        os.flush();
                        curLen+=bufferLen;
                        final long finalCurLen = curLen;
                        sHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                iNetDownload.progress((int) (finalCurLen*1.0f/totalLen*100));
                            }
                        });
                    }

                    if (call.isCanceled()){
                        return;
                    }

                    try {
                        targetFile.setExecutable(true,false);
                        targetFile.setReadable(true,false);
                        targetFile.setWritable(true,false);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iNetDownload.success(targetFile);
                        }
                    });
                }catch (final Exception e){
                    e.printStackTrace();
                    if (call.isCanceled()){
                        return;
                    }
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            iNetDownload.failed(e);
                        }
                    });
                }finally {
                    if (is != null){
                        is.close();
                    }
                    if (os != null){
                        os.close();
                    }
                }
            }
        });
    }

    @Override
    public void cancel(Object tag) {
        List<Call> queuedCalls = sOkHttpClient.dispatcher().queuedCalls();
        if (queuedCalls != null){
            for (Call call : queuedCalls){
                if (tag.equals(call.request().tag())){
                    call.cancel();
                }
            }
        }

        List<Call> runningCalls = sOkHttpClient.dispatcher().runningCalls();
        if (runningCalls != null){
            for (Call call : runningCalls){
                if (tag.equals(call.request().tag())){
                    call.cancel();
                }
            }
        }
    }
}
