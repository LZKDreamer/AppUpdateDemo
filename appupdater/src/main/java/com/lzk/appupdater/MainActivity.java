package com.lzk.appupdater;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lzk.appupdater.updater.Bean.DownloadBean;
import com.lzk.appupdater.updater.AppUpdater;
import com.lzk.appupdater.updater.net.INetCallback;
import com.lzk.appupdater.updater.net.INetDownloadCallback;
import com.lzk.appupdater.updater.ui.UpdateVersionDialog;
import com.lzk.appupdater.updater.utils.AppUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private Button mCheckVersionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCheckVersionBtn = findViewById(R.id.main_check_version_btn);
        mCheckVersionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppUpdater.getInstance().getINetManager().get("http://59.110.162.30/app_updater_version.json",
                        new INetCallback() {
                            @Override
                            public void success(String response) {
                                DownloadBean bean = DownloadBean.parse(response);
                                if (bean == null){
                                    Toast.makeText(MainActivity.this,"接口返回数据异常",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                try {
                                    long versionCode = Long.parseLong(bean.getVersionCode());
                                    if (versionCode <= AppUtil.getVersionCode(MainActivity.this)){
                                        Toast.makeText(MainActivity.this,"已经是最新版本",Toast.LENGTH_SHORT).show();
                                    }else {
                                        UpdateVersionDialog.show(MainActivity.this,bean);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void failed(Throwable throwable) {
                                Toast.makeText(MainActivity.this,"版本更新接口错误",Toast.LENGTH_SHORT).show();
                            }
                        },this);
            }
        });
    }

    @Override
    protected void onDestroy() {
        AppUpdater.getInstance().getINetManager().cancel(this);
    }
}
