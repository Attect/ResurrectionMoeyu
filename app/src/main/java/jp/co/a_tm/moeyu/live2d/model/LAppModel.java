package jp.co.a_tm.moeyu.live2d.model;

import java.io.InputStream;
import javax.microedition.khronos.opengles.GL10;
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

    public LAppModel(LAppLive2DManager mgr) {
        this.accel = null;
        this.live2DModel = null;
        this.modelInitialized = false;
        this.accel = null;
        this.live2dAnimation = null;
        this.live2DManager = mgr;
    }

    public void setupModel(LAppLive2DManager mgr, GL10 gl) throws Exception {
        if (mgr != null) {
            if (this.live2DModel == null) {
                String[] tex = new String[]{"moeyu.1024/texture_00.png", "moeyu.1024/texture_01.png", "moeyu.1024/texture_02.png", "moeyu.1024/texture_03.png"};
                InputStream in = this.live2DManager.getFileManager().open_resource("model/" + "moeyu" + ".moc");
                this.live2DModel = Live2DModelAndroid.loadModel(in);
                in.close();
                for (int j = 0; j < tex.length; j++) {
                    this.live2DModel.setTexture(j, UtOpenGL.loadTexture(gl, this.live2DManager.getFileManager().open_resource("model/" + tex[j]), true));
                }
            }
            if (this.live2dAnimation != null) {
                this.live2dAnimation.initParam(this.live2DModel);
            }
            this.modelInitialized = true;
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

    public LAppAnimation getAnimation() {
        return this.live2dAnimation;
    }

    public ALive2DModel getModel() {
        return this.live2DModel;
    }
}
