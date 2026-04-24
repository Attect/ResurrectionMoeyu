package jp.co.a_tm.moeyu;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.util.Collections;

/**
 * 相机预览视图（Camera2 API 实现）
 * 作为 AR 模式背景，显示在最底层
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private final Activity mActivity;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Size mPreviewSize;
    private boolean mSurfaceReady = false;
    private boolean mCameraOpening = false;

    public CameraPreview(Activity activity) {
        super(activity);
        this.mActivity = activity;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceReady = true;
        startBackgroundThread();
        openCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Camera2 的会话会在配置时自动适配 Surface 尺寸
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceReady = false;
        stop();
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                Log.e(TAG, "stopBackgroundThread interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private void openCamera() {
        if (mCameraOpening || !mSurfaceReady) return;
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length == 0) {
                Toast.makeText(mActivity, "未检测到相机", Toast.LENGTH_SHORT).show();
                return;
            }
            String cameraId = cameraIds[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) {
                Toast.makeText(mActivity, "无法获取相机配置", Toast.LENGTH_SHORT).show();
                return;
            }
            Size[] sizes = map.getOutputSizes(SurfaceHolder.class);
            mPreviewSize = chooseOptimalSize(sizes, getWidth(), getHeight());
            adjustSurfaceLayoutSize(mPreviewSize);
            mCameraOpening = true;
            manager.openCamera(cameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera failed", e);
            Toast.makeText(mActivity, "相机访问失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 选择最优预览尺寸：优先 16:9，其次覆盖 surface 尺寸
     */
    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        if (choices == null || choices.length == 0) {
            return new Size(640, 480);
        }
        float targetRatio = (float) Math.max(width, height) / Math.min(width, height);
        Size optimalSize = choices[0];
        float minDelta = Float.MAX_VALUE;
        for (Size size : choices) {
            float ratio = (float) Math.max(size.getWidth(), size.getHeight())
                    / Math.min(size.getWidth(), size.getHeight());
            float delta = Math.abs(ratio - targetRatio);
            if (delta < minDelta) {
                minDelta = delta;
                optimalSize = size;
            }
        }
        return optimalSize;
    }

    /**
     * 根据预览尺寸调整 SurfaceView 布局，保持比例并居中填充
     */
    private void adjustSurfaceLayoutSize(Size previewSize) {
        int availableWidth = getWidth();
        int availableHeight = getHeight();
        if (availableWidth == 0 || availableHeight == 0) return;

        float previewRatio = (float) previewSize.getWidth() / previewSize.getHeight();
        float screenRatio = (float) availableWidth / availableHeight;

        int layoutWidth, layoutHeight;
        if (screenRatio > previewRatio) {
            layoutHeight = availableHeight;
            layoutWidth = (int) (availableHeight * previewRatio);
        } else {
            layoutWidth = availableWidth;
            layoutHeight = (int) (availableWidth / previewRatio);
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(layoutWidth, layoutHeight);
        } else {
            params.width = layoutWidth;
            params.height = layoutHeight;
        }
        setLayoutParams(params);
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraOpening = false;
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpening = false;
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpening = false;
            cameraDevice.close();
            mCameraDevice = null;
            Log.e(TAG, "Camera error: " + error);
            Toast.makeText(mActivity, "相机错误: " + error, Toast.LENGTH_SHORT).show();
        }
    };

    private void createCameraPreviewSession() {
        try {
            Surface surface = getHolder().getSurface();
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if (mCameraDevice == null) return;
                            mCaptureSession = session;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mCaptureSession.setRepeatingRequest(
                                        mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                Log.e(TAG, "setRepeatingRequest failed", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(mActivity, "相机配置失败", Toast.LENGTH_SHORT).show();
                        }
                    },
                    mBackgroundHandler
            );
        } catch (CameraAccessException e) {
            Log.e(TAG, "createCameraPreviewSession failed", e);
        }
    }

    /**
     * 停止预览并释放相机资源
     */
    public void stop() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        stopBackgroundThread();
        mCameraOpening = false;
    }
}
