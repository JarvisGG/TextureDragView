package com.jarvis.textureviewdemo.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import com.jarvis.textureviewdemo.R;
import com.jarvis.textureviewdemo.widgets.CameraDevicesStateCallbackImpl;
import com.jarvis.textureviewdemo.widgets.ViewDragHelpImpl;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

/**
 * @author Jarvis
 * @version 1.0
 * @title TextureViewDemo
 * @description 该类主要功能描述
 * @company 北京奔流网络技术有限公司
 * @create 17/3/10 下午3:49
 * @changeRecord [修改记录] <br/>
 */

public class DragTextureView extends FrameLayout {

    private TextureView cameraView;
    private TextureView videoView;

    private Context context;
    private CameraManager mCameraManager;
    private CameraDevicesStateCallbackImpl mCameraDeviceStateCallBack;
    private MediaPlayer mMediaPlayer;

    private ViewDragHelpImpl mDragHelper;
    private OnTouchListener mViewTouchListener;

    private int mVideoViewMinWidth;
    private int mVideoViewMaxWidth;
    private int mVideoViewHeight;
    private int mVideoViewHorizontalRange;
    private int mVideoViewVertialRange;

    private int mCameraViewMinWidth;
    private int mCameraViewMaxWidth;
    private int mCameraViewHeight;
    private int mCameraViewHorizontalRange;
    private int mCameraViewVertialRange;

    private boolean isParent = true;
    /**
     * 第一次调用onMeasure时调用
     */
    private boolean mIsFinishInit = false;
    /**
     * 当前拖动的方向
     */
    public static final int NONE = 1;
    public static final int HORIZONTAL = 1 << 1;
    public static final int VERTICAL = 1 << 2;

    /**
     * 最终组件滑向的方向
     */
    public static final int SLIDE_RESTORE_ORIGINAL = 1;
    public static final int SLIDE_TO_LEFT = 1 << 1;
    public static final int SLIDE_TO_RIGHT = 1 << 2;

    /**
     * 当前拖动的方向
     */
    private int mDragDirect = NONE;

    /**
     * 触发ACTION_DOWN时的坐标
     */
    private int mDownX;
    private int mDownY;
    /**
     * 垂直拖动时的偏移量
     * (mTop - mMinTop) / mVerticalRange
     */
    private float mVerticalOffset = 1f;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragTextureView(Context context) {
        super(context);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DragTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initView(Context context) {
        this.context = context;

        initDragUtils();
        initCameraView();
        initVideoView();

        LayoutParams cameraLayoutParams = new FrameLayout.LayoutParams(480, 270);
        cameraLayoutParams.gravity = Gravity.LEFT;
        this.addView(cameraView, cameraLayoutParams);

        LayoutParams videoLayoutParams = new FrameLayout.LayoutParams(480, 270);
        videoLayoutParams.gravity = Gravity.RIGHT;
        this.addView(videoView, videoLayoutParams);
    }

    private void onLayoutLightly(View targetView, int left, int top, int dx, int dy) {
        targetView.layout(left, top, left + targetView.getMeasuredWidth(), targetView.getMeasuredHeight());
    }

    private void requestLayoutLightly(View targetView, int left, int top, int dx, int dy) {
        onLayoutLightly(targetView, left, top, dx, dy);
        ViewCompat.postInvalidateOnAnimation(this);//进行重绘
    }

    private void initDragUtils() {
        mDragHelper = new ViewDragHelpImpl.Builder()
                .create(this, 1.0f, new ViewDragHelper.Callback() {
                    @Override
                    public boolean tryCaptureView(View child, int pointerId) {
                        boolean a = (videoView == child || cameraView == child);
                        return (videoView == child || cameraView == child);
                    }

                    @Override
                    public void onViewDragStateChanged(int state) {
                        if (state == ViewDragHelper.STATE_IDLE) {

                        }
                        mDragDirect = NONE;
                    }

                    @Override
                    public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) { //view在拖动过程坐标发生变化时会调用此方法，包括两个时间段：手动拖动和自动滚动
//                        requestLayoutLightly(changedView, left, top, dx, dy);
                    }

                    @Override
                    public int clampViewPositionVertical(View child, int top, int dy) {
                        final int topBound =getPaddingTop();
                        final int bottomBound = getHeight() - child.getHeight();
                        return Math.min(Math.max(top, topBound), bottomBound);
                    }

                    @Override
                    public int clampViewPositionHorizontal(View child, int left, int dx) {
                        final int leftBound =getPaddingLeft();
                        final int rightBound = getWidth() - child.getWidth();
                        return Math.min(Math.max(left, leftBound), rightBound);
                    }

                    @Override
                    public void onViewReleased(View releasedChild, float xvel, float yvel) {
                        super.onViewReleased(releasedChild, xvel, yvel);
                    }
                })
                .builder();

        mViewTouchListener = (v, event) -> {

            boolean isHit = (mDragHelper.viewDragHelper.isViewUnder(videoView, (int) event.getX(), (int) event.getY())
                    ||  mDragHelper.viewDragHelper.isViewUnder(cameraView, (int) event.getX(), (int) event.getY()));

            switch (MotionEventCompat.getActionMasked(event)) {
                case ACTION_DOWN:
                    mDownX = (int) event.getX();
                    mDownY = (int) event.getY();
                    break;
                case ACTION_MOVE:
                    int dx = Math.abs(mDownX - (int) event.getX());
                    int dy = Math.abs(mDownY - (int) event.getY());
                    int slop = mDragHelper.viewDragHelper.getTouchSlop();

                    if (Math.sqrt(dx * dx + dy * dy) >= slop) {//判断是水平方向拖拽，还是垂直方向上拖拽
                        if (dy >= dx)
                            mDragDirect = VERTICAL;
                        else
                            mDragDirect = HORIZONTAL;
                    }
                    break;

                case ACTION_UP:
                    if (mDragDirect == NONE) {
                        dx = Math.abs(mDownX - (int) event.getX());
                        dy = Math.abs(mDownY - (int) event.getY());
                        slop = mDragHelper.viewDragHelper.getTouchSlop();

                        if (Math.sqrt(dx * dx + dy * dy) < slop) {
                            mDragDirect = VERTICAL;
//                            if (mIsMinimum)
//                                maximize();
//                            else
//                                minimize();
                        }
                    }
                    break;
                default:
                    break;

            }

            mDragHelper.viewDragHelper.processTouchEvent(event);
            return isHit;
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCameraView() {
        cameraView = new TextureView(context);
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        cameraView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }

                try {
                    mCameraDeviceStateCallBack = new CameraDevicesStateCallbackImpl(context, cameraView.getSurfaceTexture());
                    mCameraManager.openCamera("0", mCameraDeviceStateCallBack, null);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

//        cameraView.setOnTouchListener(mViewTouchListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.viewDragHelper.processTouchEvent(event);
        return true;
    }

    private void initVideoView() {

        mMediaPlayer = MediaPlayer.create(context, R.raw.test_4);
        mMediaPlayer.setOnPreparedListener(mp -> {
            mMediaPlayer.setLooping(true);
        });

        videoView = new TextureView(context);
        videoView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mMediaPlayer.setSurface(new Surface(surface));
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

//        videoView.setOnTouchListener(mViewTouchListener);


    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        mMediaPlayer.release();
        super.onDetachedFromWindow();
    }


    public void startVideo() {
        if (mMediaPlayer.isPlaying())
            return;
        try {
            mMediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer.start();
    }
}
