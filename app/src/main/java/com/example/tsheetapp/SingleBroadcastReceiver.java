package com.example.tsheetapp;

import android.content.Context;
import android.content.Intent;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

public class SingleBroadcastReceiver extends UploadServiceBroadcastReceiver {
    public interface Delegate {
        void onProgress(int progress);
        void onProgress(long uploadedBytes, long totalBytes);
        void onError(Exception exception);
        void onCompleted(int serverResponseCode, byte[] serverResponseBody);
        void onCancelled();
    }

    private String mUploadID;
    private Delegate mDelegate;

    public void setUploadID(String uploadID) {
        mUploadID = uploadID;
    }

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void register(Context context) {
        super.register(context);
    }

    @Override
    public void unregister(Context context) {
        super.unregister(context);
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        super.onProgress(context, uploadInfo);
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, Exception exception) {
        super.onError(context, uploadInfo, exception);
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        super.onCompleted(context, uploadInfo, serverResponse);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        super.onCancelled(context, uploadInfo);
    }
}
