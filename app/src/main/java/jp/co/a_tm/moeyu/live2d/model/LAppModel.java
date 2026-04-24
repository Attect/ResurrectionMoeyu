package jp.co.a_tm.moeyu.live2d.model;

import java.io.InputStream;
import javax.microedition.khronos.opengles.GL10;
import android.util.Log;
import jp.co.a_tm.moeyu.live2d.LAppLive2DManager;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;
import jp.live2d.ALive2DModel;
import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;

public class LAppModel {
    transient float[] accel;
    transient LAppLive2DManager live2DManager;
    Live2DModelAndroid live2DModel;
    LAppAnimation live2dAnimation;
    transient boolean modelInitialized;
    /** 模型纹理 ID，用于显式释放 */
    private final int[] modelTextureIds = new int[4];

    public LAppModel(LAppLive2DManager mgr) {
        this.accel = null;
        this.live2DModel = null;
        this.modelInitialized = false;
        this.accel = null;
        this.live2dAnimation = null;
        this.live2DManager = mgr;
    }

    public void setupModel(LAppLive2DManager mgr, GL10 gl) throws Exception {
        // [DEBUG] 模型加载开始
        Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: mgr=" + (mgr != null ? "OK" : "NULL")
            + " live2DModel=" + (this.live2DModel != null ? "already loaded" : "NULL"));
        if (mgr != null) {
            if (this.live2DModel == null) {
                String[] tex = new String[]{"moeyu.1024/texture_00.png", "moeyu.1024/texture_01.png", "moeyu.1024/texture_02.png", "moeyu.1024/texture_03.png"};
                // [DEBUG] 加载moc文件
                Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: loading moeyu.moc ...");
                InputStream in = this.live2DManager.getFileManager().open_resource("model/" + "moeyu" + ".moc");
                try {
                    this.live2DModel = Live2DModelAndroid.loadModel(in);
                } finally {
                    in.close();
                }
                Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: moc loaded OK, live2DModel=" + (this.live2DModel != null ? "OK" : "FAIL"));
                // 释放旧模型纹理（如果存在）
                releaseModelTextures(gl);
                // [DEBUG] 加载纹理
                for (int j = 0; j < tex.length; j++) {
                    Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: loading texture[" + j + "]=" + tex[j] + " ...");
                    int texId = UtOpenGL.loadTexture(gl, this.live2DManager.getFileManager().open_resource("model/" + tex[j]), true);
                    this.modelTextureIds[j] = texId;
                    this.live2DModel.setTexture(j, texId);
                    Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: texture[" + j + "] loaded, texId=" + texId);
                }
            } else {
                Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: live2DModel already exists, reusing");
            }
            if (this.live2dAnimation != null) {
                this.live2dAnimation.initParam(this.live2DModel);
                Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: animation params initialized");
            } else {
                Log.w("LIVE2D_DEBUG", "LAppModel.setupModel: live2dAnimation is NULL!");
            }
            this.modelInitialized = true;
            Log.d("LIVE2D_DEBUG", "LAppModel.setupModel: COMPLETE, modelInitialized=true");
        } else {
            Log.e("LIVE2D_DEBUG", "LAppModel.setupModel: mgr is NULL, cannot setup!");
        }
    }

    public void setupAnimation(LAppLive2DManager ka) {
        this.live2dAnimation = new LAppAnimation(ka);
        if (this.live2DModel != null) {
            this.live2dAnimation.initParam(this.live2DModel);
        }
    }

    public void drawModel(GL10 gl) throws Exception {
        gl.glPushMatrix();
        gl.glTranslatef(-250.0f, -50.0f, 0.0f);
        gl.glScalef(2.5f, 2.5f, 1.0f);
        drawModel_core(gl);
        gl.glPopMatrix();
    }

    public void drawModel_core(GL10 gl) throws Exception {
        if (this.live2DModel != null) {
            if (this.live2dAnimation != null) {
                this.live2dAnimation.updateParam(this.live2DModel);
            }
            if (this.accel != null) {
                this.live2DModel.addToParamFloat("PARAM_ANGLE_X", (1.5f * 60.0f) * this.accel[0], 0.5f);
                this.live2DModel.addToParamFloat("PARAM_ANGLE_Y", (1.5f * 60.0f) * this.accel[1], 0.5f);
                this.live2DModel.addToParamFloat("PARAM_BODY_ANGLE_X", (20.0f * 1.5f) * this.accel[0], 0.5f);
                this.live2DModel.addToParamFloat("PARAM_BASE_X", -200.0f * this.accel[0], 0.5f);
                this.live2DModel.addToParamFloat("PARAM_BASE_Y", -100.0f * this.accel[1], 0.5f);
            }
            this.live2DModel.setupPartsOpacityGroup_alphaImpl(new String[]{"VISIBLE:PARTS_01L", "VISIBLE:PARTS_01_BACKGROUND", "VISIBLE:PARTS_01_BASIC", "VISIBLE:PARTS_01_BASIC_R", "VISIBLE:PARTS_01_BODY", "VISIBLE:PARTS_01_BROW_001", "VISIBLE:PARTS_01_EAR_001", "VISIBLE:PARTS_01_EYE_001", "VISIBLE:PARTS_01_EYE_BALL_001", "VISIBLE:PARTS_01_FACE_001", "VISIBLE:PARTS_01_FACE_STRETCH", "VISIBLE:PARTS_01_HAIR_BACK_001", "VISIBLE:PARTS_01_HAIR_FRONT_001", "VISIBLE:PARTS_01_HAIR_SIDE_001", "VISIBLE:PARTS_01_HAIR_SIDE_STRETCH", "VISIBLE:PARTS_01_MOUTH_001", "VISIBLE:PARTS_01_NOSE_001", "VISIBLE:PARTS_01_SKETCH", "VISIBLE:PARTS_01_STRETCH", "VISIBLE:PARTS_01_WASH01", "VISIBLE:PARTS_01_WASH01_BOBBLE", "VISIBLE:PARTS_01_WASH02", "VISIBLE:PARTS_01_WASH02_BOBBLE", "VISIBLE:PARTS_CHEEK", "VISIBLE:PARTS_CHEEK_STRETCH", "VISIBLE:ROUGH"}, new String[]{"PARTS_01L", "PARTS_01_BACKGROUND", "PARTS_01_BASIC", "PARTS_01_BASIC_R", "PARTS_01_BODY", "PARTS_01_BROW_001", "PARTS_01_EAR_001", "PARTS_01_EYE_001", "PARTS_01_EYE_BALL_001", "PARTS_01_FACE_001", "PARTS_01_FACE_STRETCH", "PARTS_01_HAIR_BACK_001", "PARTS_01_HAIR_FRONT_001", "PARTS_01_HAIR_SIDE_001", "PARTS_01_HAIR_SIDE_STRETCH", "PARTS_01_MOUTH_001", "PARTS_01_NOSE_001", "PARTS_01_SKETCH", "PARTS_01_STRETCH", "PARTS_01_WASH01", "PARTS_01_WASH01_BOBBLE", "PARTS_01_WASH02", "PARTS_01_WASH02_BOBBLE", "PARTS_CHEEK", "PARTS_CHEEK_STRETCH", "ROUGH"}, 0.0f, 0.5f);
            this.live2DModel.setGL(gl);
            this.live2DModel.update();
            this.live2DModel.draw();
        }
    }

    public boolean isModelInitialized() {
        return this.modelInitialized;
    }

    public void setAccelarationValue(float[] accel) {
        this.accel = accel;
    }

    /**
     * 释放模型纹理
     */
    public void releaseModelTextures(GL10 gl) {
        if (gl == null) {
            return;
        }
        for (int texId : this.modelTextureIds) {
            if (texId != 0) {
                gl.glDeleteTextures(1, new int[]{texId}, 0);
            }
        }
        java.util.Arrays.fill(this.modelTextureIds, 0);
        Log.d("LIVE2D_DEBUG", "LAppModel.releaseModelTextures: released");
    }

    public LAppAnimation getAnimation() {
        return this.live2dAnimation;
    }

    public ALive2DModel getModel() {
        return this.live2DModel;
    }
}
