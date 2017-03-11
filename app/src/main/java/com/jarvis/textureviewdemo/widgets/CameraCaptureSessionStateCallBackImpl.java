package com.jarvis.textureviewdemo.widgets;


import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * @author Jarvis
 * @version 1.0
 * @title TextureViewDemo
 * @description 该类主要功能描述
 * @company 北京奔流网络技术有限公司
 * @create 17/3/10 下午9:31
 * @changeRecord [修改记录] <br/>
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraCaptureSessionStateCallBackImpl extends CameraCaptureSession.StateCallback {

    private CaptureRequest mPreviewRequest;

    public CameraCaptureSessionStateCallBackImpl(CaptureRequest previewRequest) {
        this.mPreviewRequest = previewRequest;
    }

    @Override
    public void onConfigured(CameraCaptureSession session) {
        try {
            session.setRepeatingRequest(mPreviewRequest, null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigureFailed(CameraCaptureSession session) {

    }
}
