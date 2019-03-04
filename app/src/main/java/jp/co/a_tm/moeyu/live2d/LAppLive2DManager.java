package jp.co.a_tm.moeyu.live2d;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
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
    boolean dirtyFlag = true;
    private FileManager fileManager;
    /* access modifiers changed from: private */
    public LAppGLView glView = null;
    private final Context mContext;
    private FinishListener mFinishListener;
    private boolean modelUpdating = false;
    private LAppModel myModel = null;
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
            UtDebug.error("RANGE_CHECK_POINTをオンにしない場合 モデルは崩れる場合があります", new Object[0]);
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
        this.glView = null;
        this.myModel = null;
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

    public LAppModel getModel(GL10 gl) throws Exception {
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
        try {
            setupModel_exe();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setupModel_exe() throws Exception {
        this.dirtyFlag = true;
        if (this.myModel == null) {
            this.myModel = new LAppModel(this);
            this.myModel.setupAnimation(this);
        }
    }

    public void setupModel_later(GL10 gl) throws Exception {
        UtDebug.start("LAppLive2DManager#setupModel()");
        if (!this.modelUpdating) {
            this.modelUpdating = true;
            if (this.dirtyFlag) {
                this.dirtyFlag = false;
                if (this.myModel == null) {
                    this.myModel = new LAppModel(this);
                }
                this.myModel.setupModel(this, gl);
                this.modelUpdating = false;
                UtDebug.dump("LAppLive2DManager#setupModel()");
                if (this.mFinishListener != null) {
                    this.mFinishListener.onFinishSetupModel();
                }
            }
        }
    }

    public void releaseModel() {
        this.myModel = null;
    }

    public void setPartsCacheDirectory(String path) {
        this.partsCacheDir = path;
    }

    public String getPartsCacheDirectory() {
        return this.partsCacheDir;
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
