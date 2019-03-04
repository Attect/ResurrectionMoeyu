package jp.co.a_tm.moeyu;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements Callback {
    private static final String CAMERA_PARAM_LANDSCAPE = "landscape";
    private static final String CAMERA_PARAM_ORIENTATION = "orientation";
    private static final String CAMERA_PARAM_PORTRAIT = "portrait";
    private static boolean DEBUGGING = false;
    private static final String LOG_TAG = "CameraPreviewSample";
    protected Activity mActivity;
    protected Camera mCamera;
    private int mCenterPosX = -1;
    private int mCenterPosY;
    private SurfaceHolder mHolder;
    protected Size mPictureSize;
    protected List<Size> mPictureSizeList;
    PreviewReadyCallback mPreviewReadyCallback = null;
    protected Size mPreviewSize;
    protected List<Size> mPreviewSizeList;
    private int mSurfaceChangedCallDepth = 0;
    protected boolean mSurfaceConfiguring = false;

    public interface PreviewReadyCallback {
        void onPreviewReady();
    }

    public CameraPreview(Activity activity) {
        super(activity);
        this.mActivity = activity;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        if (VERSION.SDK_INT >= 9) {
            this.mCamera = Camera.open(0);
        } else {
            this.mCamera = Camera.open();
        }
        Parameters cameraParams = this.mCamera.getParameters();
        this.mPreviewSizeList = cameraParams.getSupportedPreviewSizes();
        this.mPictureSizeList = cameraParams.getSupportedPictureSizes();
        try {
            this.mCamera.setPreviewDisplay(this.mHolder);
        } catch (IOException e) {
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        this.mSurfaceChangedCallDepth++;
        doSurfaceChanged(width, height);
        this.mSurfaceChangedCallDepth--;
    }

    private void doSurfaceChanged(int width, int height) {
        this.mCamera.stopPreview();
        Parameters cameraParams = this.mCamera.getParameters();
        boolean portrait = isPortrait();
        if (!this.mSurfaceConfiguring) {
            Size previewSize = determinePreviewSize(portrait, width, height);
            Size pictureSize = determinePictureSize(previewSize);
            if (DEBUGGING) {
                Log.v(LOG_TAG, "Desired Preview Size - w: " + width + ", h: " + height);
            }
            this.mPreviewSize = previewSize;
            this.mPictureSize = pictureSize;
            this.mSurfaceConfiguring = adjustSurfaceLayoutSize(previewSize, portrait, width, height);
            if (this.mSurfaceConfiguring && this.mSurfaceChangedCallDepth <= 1) {
                return;
            }
        }
        configureCameraParameters(cameraParams, portrait);
        this.mSurfaceConfiguring = false;
        try {
            this.mCamera.startPreview();
        } catch (Exception e) {
            Log.w(LOG_TAG, "Failed to start preview: " + e.getMessage());
            this.mPreviewSizeList.remove(this.mPreviewSize);
            this.mPreviewSize = null;
            if (this.mPreviewSizeList.size() > 0) {
                surfaceChanged(null, 0, width, height);
            } else {
                Toast.makeText(this.mActivity, "Can't start preview", Toast.LENGTH_LONG).show();
                Log.w(LOG_TAG, "Gave up starting preview");
            }
        }
        if (this.mPreviewReadyCallback != null) {
            this.mPreviewReadyCallback.onPreviewReady();
        }
    }

    /* access modifiers changed from: protected */
    public Size determinePreviewSize(boolean portrait, int reqWidth, int reqHeight) {
        int reqPreviewWidth;
        int reqPreviewHeight;
        if (portrait) {
            reqPreviewWidth = reqHeight;
            reqPreviewHeight = reqWidth;
        } else {
            reqPreviewWidth = reqWidth;
            reqPreviewHeight = reqHeight;
        }
        if (DEBUGGING) {
            Log.v(LOG_TAG, "Listing all supported preview sizes");
            for (Size size : this.mPreviewSizeList) {
                Log.v(LOG_TAG, "  w: " + size.width + ", h: " + size.height);
            }
            Log.v(LOG_TAG, "Listing all supported picture sizes");
            for (Size size2 : this.mPictureSizeList) {
                Log.v(LOG_TAG, "  w: " + size2.width + ", h: " + size2.height);
            }
        }
        float reqRatio = ((float) reqPreviewWidth) / ((float) reqPreviewHeight);
        float deltaRatioMin = Float.MAX_VALUE;
        Size retSize = null;
        for (Size size22 : this.mPreviewSizeList) {
            float deltaRatio = Math.abs(reqRatio - (((float) size22.width) / ((float) size22.height)));
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size22;
            }
        }
        return retSize;
    }

    /* access modifiers changed from: protected */
    public Size determinePictureSize(Size previewSize) {
        Size retSize = null;
        for (Size size : this.mPictureSizeList) {
            if (size.equals(previewSize)) {
                return size;
            }
        }
        if (DEBUGGING) {
            Log.v(LOG_TAG, "Same picture size not found.");
        }
        float reqRatio = ((float) previewSize.width) / ((float) previewSize.height);
        float deltaRatioMin = Float.MAX_VALUE;
        for (Size size2 : this.mPictureSizeList) {
            float deltaRatio = Math.abs(reqRatio - (((float) size2.width) / ((float) size2.height)));
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size2;
            }
        }
        return retSize;
    }

    /* access modifiers changed from: protected */
    public boolean adjustSurfaceLayoutSize(Size previewSize, boolean portrait, int availableWidth, int availableHeight) {
        float tmpLayoutHeight;
        float tmpLayoutWidth;
        float fact;
        if (portrait) {
            tmpLayoutHeight = (float) previewSize.width;
            tmpLayoutWidth = (float) previewSize.height;
        } else {
            tmpLayoutHeight = (float) previewSize.height;
            tmpLayoutWidth = (float) previewSize.width;
        }
        float factH = ((float) availableHeight) / tmpLayoutHeight;
        float factW = ((float) availableWidth) / tmpLayoutWidth;
        if (factH < factW) {
            fact = factH;
        } else {
            fact = factW;
        }
        LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        int layoutHeight = (int) (tmpLayoutHeight * fact);
        int layoutWidth = (int) (tmpLayoutWidth * fact);
        if (DEBUGGING) {
            Log.v(LOG_TAG, "Preview Layout Size - w: " + layoutWidth + ", h: " + layoutHeight);
            Log.v(LOG_TAG, "Scale factor: " + fact);
        }
        if (layoutWidth == getWidth() && layoutHeight == getHeight()) {
            return false;
        }
        layoutParams.height = layoutHeight;
        layoutParams.width = layoutWidth;
        if (this.mCenterPosX >= 0) {
            layoutParams.topMargin = this.mCenterPosY - (layoutHeight / 2);
            layoutParams.leftMargin = this.mCenterPosX - (layoutWidth / 2);
        }
        setLayoutParams(layoutParams);
        return true;
    }

    public void setCenterPosition(int x, int y) {
        this.mCenterPosX = x;
        this.mCenterPosY = y;
    }

    /* access modifiers changed from: protected */
    public void configureCameraParameters(Parameters cameraParams, boolean portrait) {
        if (VERSION.SDK_INT >= 8) {
            int angle;
            switch (this.mActivity.getWindowManager().getDefaultDisplay().getRotation()) {
                case 0:
                    angle = 90;
                    break;
                case 1:
                    angle = 0;
                    break;
                case 2:
                    angle = 270;
                    break;
                case 3:
                    angle = 180;
                    break;
                default:
                    angle = 90;
                    break;
            }
            Log.v(LOG_TAG, "angle: " + angle);
            this.mCamera.setDisplayOrientation(angle);
        } else if (portrait) {
            cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_PORTRAIT);
        } else {
            cameraParams.set(CAMERA_PARAM_ORIENTATION, CAMERA_PARAM_LANDSCAPE);
        }
        cameraParams.setPreviewSize(this.mPreviewSize.width, this.mPreviewSize.height);
        cameraParams.setPictureSize(this.mPictureSize.width, this.mPictureSize.height);
        if (DEBUGGING) {
            Log.v(LOG_TAG, "Preview Actual Size - w: " + this.mPreviewSize.width + ", h: " + this.mPreviewSize.height);
            Log.v(LOG_TAG, "Picture Actual Size - w: " + this.mPictureSize.width + ", h: " + this.mPictureSize.height);
        }
        this.mCamera.setParameters(cameraParams);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        stop();
    }

    public void stop() {
        if (this.mCamera != null) {
            this.mCamera.stopPreview();
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    public boolean isPortrait() {
        return this.mActivity.getResources().getConfiguration().orientation == 1;
    }

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        if (this.mCamera != null) {
            this.mCamera.setOneShotPreviewCallback(callback);
        }
    }

    public void setPreviewCallback(PreviewCallback callback) {
        if (this.mCamera != null) {
            this.mCamera.setPreviewCallback(callback);
        }
    }

    public Size getPreviewSize() {
        return this.mPreviewSize;
    }

    public void setOnPreviewReady(PreviewReadyCallback cb) {
        this.mPreviewReadyCallback = cb;
    }
}
