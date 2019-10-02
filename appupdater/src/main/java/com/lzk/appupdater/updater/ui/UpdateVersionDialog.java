package com.lzk.appupdater.updater.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.lzk.appupdater.MainActivity;
import com.lzk.appupdater.R;
import com.lzk.appupdater.updater.AppUpdater;
import com.lzk.appupdater.updater.Bean.DownloadBean;
import com.lzk.appupdater.updater.net.INetDownloadCallback;
import com.lzk.appupdater.updater.utils.AppUtil;

import java.io.File;
import java.io.Serializable;

public class UpdateVersionDialog extends DialogFragment {

    private static final String KEY_DOWNLOAD_BEAN = "key_download_bean";
    private static final String TAG = "UpdateVersionDialog";

    private DownloadBean downloadBean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null){
            downloadBean = (DownloadBean) getArguments().getSerializable(KEY_DOWNLOAD_BEAN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_update_version_dialog,container,false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initViews(view);
    }

    private void initViews(View view) {
        TextView titleTv = view.findViewById(R.id.title_tv);
        TextView contentTv = view.findViewById(R.id.content_tv);
        final TextView updateTv = view.findViewById(R.id.update_tv);
        titleTv.setText(downloadBean.getTitle());
        contentTv.setText(downloadBean.getContent());

        updateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTv.setEnabled(false);
                File file = new File(getActivity().getCacheDir(),"target.apk");
                AppUpdater.getInstance().getINetManager().download(downloadBean.getUrl(), file,
                        new INetDownloadCallback() {
                            @Override
                            public void success(File apkFile) {
                                updateTv.setEnabled(true);
                                Log.d("shiZi","file path="+apkFile.getAbsolutePath());
                                AppUtil.installApk(getActivity(),apkFile);
                                dismiss();
                            }

                            @Override
                            public void progress(int progress) {
                                Log.d("shiZi","progress:"+progress);
                                updateTv.setText(progress+"%");
                            }

                            @Override
                            public void failed(Throwable throwable) {
                                updateTv.setEnabled(true);
                                Toast.makeText(getActivity(),"下载接口错误",Toast.LENGTH_SHORT).show();
                            }
                        },UpdateVersionDialog.this);
            }
        });

    }

    public static void show(FragmentActivity fragmentActivity, DownloadBean bean){
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_DOWNLOAD_BEAN,bean);
        UpdateVersionDialog dialog = new UpdateVersionDialog();
        dialog.setArguments(bundle);
        dialog.show(fragmentActivity.getSupportFragmentManager(),TAG);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        AppUpdater.getInstance().getINetManager().cancel(this);
    }
}
