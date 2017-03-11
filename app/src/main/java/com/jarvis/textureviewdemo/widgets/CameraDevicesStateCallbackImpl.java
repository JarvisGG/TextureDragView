package com.jarvis.textureviewdemo.widgets;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.TextureView;

import java.util.Arrays;

/**
 * @author Jarvis
 * @version 1.0
 * @title TextureViewDemo
 * @description 该类主要功能描述
 * @company 北京奔流网络技术有限公司
 * @create 17/3/10 下午9:28
 * @changeRecord [修改记录] <br/>
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraDevicesStateCallbackImpl extends CameraDevice.StateCallback {

    private Activity mActivity;
    private SurfaceTexture mSurfaceTextureView;
    private CameraDevice mCameraDevice;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraCaptureSessionStateCallBackImpl mCameraCaptureSessionStateCallBack;

    public CameraDevicesStateCallbackImpl(Context activity, SurfaceTexture surfaceTexture) {
        this.mActivity = (Activity) activity;
        this.mSurfaceTextureView = surfaceTexture;
    }
    @Override
    public void onOpened(CameraDevice camera) {
        mCameraDevice = camera;
        mSurfaceTextureView.setDefaultBufferSize(480, 270);
        mImageReader = ImageReader.newInstance(480, 270, ImageFormat.JPEG, /* Max Images */ 2);
        Surface surface = new Surface(mSurfaceTextureView);

        try {

            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(surface);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            mCameraCaptureSessionStateCallBack = new CameraCaptureSessionStateCallBackImpl(mCaptureRequestBuilder.build());
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), mCameraCaptureSessionStateCallBack, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected(CameraDevice camera) {
        mCameraDevice.close();
    }

    @Override
    public void onError(CameraDevice camera, int error) {
        mCameraDevice.close();
        mActivity.finish();
    }
}
