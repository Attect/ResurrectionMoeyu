package jp.co.a_tm.moeyu.live2d;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import javax.microedition.khronos.opengles.GL10;
import jp.co.a_tm.moeyu.live2d.model.LAppModel;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;
import jp.co.a_tm.moeyu.live2d.util.AccelHelper;
import jp.co.a_tm.moeyu.live2d.util.AccelHelper.AccelListener;
import jp.co.a_tm.moeyu.live2d.view.LAppGLView;
import jp.live2d.Live2D;
import jp.live2d.util.UtDebug;

public class LAppLive2DManager implements LAppDefine {
    private AccelHelper accelHelper;
    volatile boolean dirtyFlag = true;
    private FileManager fileManager;
    /* access modifiers changed from: private */
    public LAppGLView glView = null;
    private final Context mContext;
    private FinishListener mFinishListener;
    private volatile boolean modelUpdating = false;
    private LAppModel myModel = null;
    private static final int MAX_MODEL_SETUP_RETRY = 3;
    private int modelSetupRetryCount = 0;
    private volatile boolean modelSetupFailed = false;
    private String partsCacheDir = null;
    private int textureSize = 512;

    public interface FinishListener {
        void onFinishSetupModel();
    }

    public LAppLive2DManager(Context androidContext) {
        Live2D.init();
        this.mContext = androidContext;
        this.fileManager = new FileManager(androidContext);
        if (!Live2D.L2D_RANGE_CHECK_POINT) {
            UtDebug.error("未开启 RANGE_CHECK_POINT 时模型可能会出现变形", new Object[0]);
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void setFinishListener(FinishListener listener) {
        this.mFinishListener = listener;
    }

    public LAppGLView createView(Activity a, Rect rect) {
        if (this.glView == null) {
            this.glView = new LAppGLView(this, a);
        }
        if (this.accelHelper == null) {
            this.accelHelper = new AccelHelper(a);
            this.accelHelper.setAccelListener(new AccelListener() {
                public void accelUpdated(float a1, float a2, float a3) {
                    try {
                        LAppLive2DManager.this.glView.getRenderer().setCurAccel(a1, a2, a3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return this.glView;
    }

    public void releaseView() {
        if (this.glView != null) {
            this.glView.onPause();
        }
        releaseModel();
        this.glView = null;
        if (this.accelHelper != null) {
            this.accelHelper.stop();
            this.accelHelper = null;
        }
    }

    public void startAnimation() {
        if (this.glView != null) {
            this.glView.startAnimation();
            if (this.accelHelper != null) {
                this.accelHelper.start();
            }
        }
    }

    public void stopAnimation() {
        if (this.glView != null) {
            this.glView.stopAnimation();
            if (this.accelHelper != null) {
                this.accelHelper.stop();
            }
            System.out.print("stop animation\n");
        }
    }

    public LAppModel getModel(GL10 gl) {
        if (this.modelSetupFailed) {
            return null;
        }
        if (this.dirtyFlag) {
            setupModel_later(gl);
        }
        return this.myModel;
    }

    public LAppAnimation getAnimation() {
        if (this.myModel == null) {
            return null;
        }
        return this.myModel.getAnimation();
    }

    public boolean isModelUpdating() {
        return this.modelUpdating;
    }

    public int getTextureSize() {
        return this.textureSize;
    }

    public void setTextureSize(int size) {
        this.textureSize = size;
    }

    public boolean setupModel() {
        // [DEBUG] 预初始化入口（UI线程调用）
        Log.d("LIVE2D_DEBUG", "setupModel (pre-init): called from UI thread");
        try {
            setupModel_exe();
            return true;
        } catch (Exception e) {
            Log.e("LIVE2D_DEBUG", "setupModel (pre-init): FAILED", e);
            e.printStackTrace();
            return false;
        }
    }

    public void setupModel_exe() throws Exception {
        this.dirtyFlag = true;
        Log.d("LIVE2D_DEBUG", "setupModel_exe: dirtyFlag=true, myModel=" + (this.myModel != null ? "exists" : "NULL"));
        if (this.myModel == null) {
            this.myModel = new LAppModel(this);
            this.myModel.setupAnimation(this);
            Log.d("LIVE2D_DEBUG", "setupModel_exe: created model and setup animation");
        }
    }

    public void setupModel_later(GL10 gl) {
        UtDebug.start("LAppLive2DManager#setupModel()");
        Log.d("LIVE2D_DEBUG", "setupModel_later: modelUpdating=" + this.modelUpdating
            + " dirtyFlag=" + this.dirtyFlag + " myModel=" + (this.myModel != null ? "exists" : "NULL")
            + " retry=" + this.modelSetupRetryCount + "/" + MAX_MODEL_SETUP_RETRY);
        if (!this.modelUpdating) {
            this.modelUpdating = true;
            try {
                if (this.dirtyFlag) {
                    this.dirtyFlag = false;
                    if (this.myModel == null) {
                        this.myModel = new LAppModel(this);
                        Log.d("LIVE2D_DEBUG", "setupModel_later: created new LAppModel instance");
                    }
                    this.myModel.setupModel(this, gl);
                    Log.d("LIVE2D_DEBUG", "setupModel_later: setupModel completed, modelInitialized="
                            + (this.myModel != null && this.myModel.isModelInitialized()));
                    UtDebug.dump("LAppLive2DManager#setupModel()");
                    if (this.mFinishListener != null) {
                        Log.d("LIVE2D_DEBUG", "setupModel_later: calling onFinishSetupModel()");
                        this.mFinishListener.onFinishSetupModel();
                    } else {
                        Log.w("LIVE2D_DEBUG", "setupModel_later: mFinishListener is NULL!");
                    }
                } else {
                    Log.d("LIVE2D_DEBUG", "setupModel_later: dirtyFlag=false, skipping setup");
                }
            } catch (Exception e) {
                Log.e("LIVE2D_DEBUG", "setupModel_later: setupModel FAILED, will retry", e);
                this.dirtyFlag = true;
                this.modelSetupRetryCount++;
                if (this.modelSetupRetryCount >= MAX_MODEL_SETUP_RETRY) {
                    Log.e("LIVE2D_DEBUG", "setupModel_later: MAX RETRIES (" + MAX_MODEL_SETUP_RETRY + ") REACHED, giving up");
                    this.dirtyFlag = false;
                    this.modelSetupFailed = true;
                }
            } finally {
                this.modelUpdating = false;
            }
        } else {
            Log.w("LIVE2D_DEBUG", "setupModel_later: modelUpdating=true, skipping (concurrent update in progress?)");
        }
    }

    public boolean isModelSetupFailed() {
        return this.modelSetupFailed;
    }

    public void releaseModel() {
        if (this.myModel != null && this.glView != null) {
            final LAppModel modelToRelease = this.myModel;
            this.glView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    GL10 gl = LAppLive2DManager.this.glView != null
                            ? LAppLive2DManager.this.glView.getRenderer().getGL()
                            : null;
                    if (gl != null) {
                        modelToRelease.releaseModelTextures(gl);
                    }
                }
            });
        }
        this.myModel = null;
    }

    public void setPartsCacheDirectory(String path) {
        this.partsCacheDir = path;
    }

    public String getPartsCacheDirectory() {
        return this.partsCacheDir;
    }

    /**
     * EGL 上下文重建时调用（onSurfaceCreated 中触发）
     * 释放已失效的模型纹理并标记需要重新加载
     */
    public void onGLContextCreated(GL10 gl) {
        Log.d("LIVE2D_DEBUG", "onGLContextCreated: myModel=" + (this.myModel != null ? "exists" : "NULL")
                + " dirtyFlag=" + this.dirtyFlag + " modelInitialized="
                + (this.myModel != null && this.myModel.isModelInitialized()));
        if (this.myModel != null) {
            // EGL 上下文已重建，旧纹理 ID 全部失效，必须重新加载
            this.myModel.releaseModelTextures(gl);
            this.myModel.invalidateGpuResources();
            this.dirtyFlag = true;
            this.modelSetupRetryCount = 0;
            this.modelSetupFailed = false;
            Log.d("LIVE2D_DEBUG", "onGLContextCreated: released textures, dirtyFlag=true, ready for reload");
        }
    }

    public boolean setBackgroundImage(String filepath) {
        return setBackgroundImage(filepath, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    public boolean setBackgroundImage(String filepath, float sx, float sy, float sw, float sh, float dx, float dy, float dw, float dh) {
        return this.glView.getRenderer().setBackgroundImage(filepath, sx, sy, sw, sh, dx, dy, dw, dh);
    }

    public FileManager getFileManager() {
        return this.fileManager;
    }
}
