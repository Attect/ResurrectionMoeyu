package jp.co.a_tm.moeyu.live2d.motion;

import android.text.TextUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import jp.co.a_tm.moeyu.Scene;
import jp.co.a_tm.moeyu.live2d.LAppDefine;
import jp.co.a_tm.moeyu.live2d.LAppLive2DManager;
import jp.co.a_tm.moeyu.util.Logger;
import jp.live2d.ALive2DModel;
import jp.live2d.motion.AMotion;
import jp.live2d.motion.EyeBlinkMotion;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;
import jp.live2d.util.UtFile;
import jp.live2d.util.UtSystem;

public class LAppAnimation {
    public static final int EYE_INTERVAL_NORMAL = 4000;
    public static final int EYE_INTERVAL_TOUCHING = 1500;
    public static final int FLIP_LENGTH = 500;
    public static final int FLIP_START_BODY_H = 600;
    public static final int FLIP_START_BODY_W = 600;
    public static final int FLIP_START_BODY_X = 300;
    public static final int FLIP_START_BODY_Y = 600;
    public static final int FLIP_START_FACE_H = 400;
    public static final int FLIP_START_FACE_W = 400;
    public static final int FLIP_START_FACE_X = 400;
    public static final int FLIP_START_FACE_Y = 200;
    public static final float MOUSE_TO_FACE_TARGET_SCALE = 1.5f;
    private static Random rand = new Random();
    boolean _flipAvailable;
    float _flipStartX;
    float _flipStartY;
    float _lastX;
    float _lastY;
    float _totalD;
    boolean _touchSingle;
    MotionQueueManager expressionMgr = new MotionQueueManager();
    HashMap<String, LAppExpressionMotion> expressions;
    EyeBlinkMotion eyeMotion;
    float faceTargetX = 0.0f;
    float faceTargetY = 0.0f;
    float faceVX = 0.0f;
    float faceVY = 0.0f;
    float faceX = 0.0f;
    float faceY = 0.0f;
    long lastTimeSec = 0;
    LAppLive2DManager live2DManager;
    private Scene mScene = Scene.bath_a;
    MotionQueueManager mainMotionMgr = new MotionQueueManager();
    final List<Live2DMotion[]> motionIdle = new ArrayList();
    final Map<String, Live2DMotion> motionTouchMap = new HashMap();
    float mouseX = 0.0f;
    float mouseY = 0.0f;
    float mouthOpen;
    long startTimeMSec;
    double timeDoubleSec;
    long timeMSec;
    boolean touching;

    public void setScene(Scene scene) {
        this.mScene = scene;
        startMainMotion();
    }

    private void startMainMotion() {
        this.mainMotionMgr.startMotion(((Live2DMotion[]) this.motionIdle.get(this.mScene.ordinal()))[rand() % ((Live2DMotion[]) this.motionIdle.get(this.mScene.ordinal())).length], false);
    }

    public LAppAnimation(LAppLive2DManager k) {
        this.live2DManager = k;
        this.touching = false;
        this.eyeMotion = new EyeBlinkMotion();
        this.eyeMotion.setInterval(EYE_INTERVAL_NORMAL);
        this.expressions = LAppExpressionMotion.loadExpressionJson(this.live2DManager, LAppDefine.MOTION_DIR, "expression.json");
        this.startTimeMSec = UtSystem.getUserTimeMSec();
        this.timeDoubleSec = 0.0d;
        this.timeMSec = 0;
        initMotionTouchMap();
        String mdir = String.format("%s/%s", LAppDefine.MOTION_DIR, "idle");
        this.motionIdle.add(new Live2DMotion[]{loadMotion(mdir, "normal_01"), loadMotion(mdir, "normal_02"), loadMotion(mdir, "normal_03")});
        this.motionIdle.add(new Live2DMotion[]{loadMotion(mdir, "wash_01"), loadMotion(mdir, "wash_02")});
        this.motionIdle.add(new Live2DMotion[]{loadMotion(mdir, "wash_body_01")});
        this.motionIdle.add(new Live2DMotion[]{loadMotion(mdir, "normal_01"), loadMotion(mdir, "normal_02"), loadMotion(mdir, "normal_03")});
        for (int i = 0; i < this.motionIdle.size(); i++) {
            for (int j = 0; j < ((Live2DMotion[]) this.motionIdle.get(i)).length; j++) {
                ((Live2DMotion[]) this.motionIdle.get(i))[j].setFadeIn(EYE_INTERVAL_NORMAL);
                ((Live2DMotion[]) this.motionIdle.get(i))[j].setFadeOut(EYE_INTERVAL_NORMAL);
            }
        }
    }

    private void initMotionTouchMap() {
        String dir = LAppDefine.MOTION_DIR + File.separator + "touch";
        try {
            for (String fileName : this.live2DManager.getContext().getAssets().list(LAppDefine.MOTION_DIR + File.separator + "touch")) {
                String voiceName = TextUtils.substring(fileName, 0, fileName.indexOf("."));
                this.motionTouchMap.put(voiceName, loadMotion(dir, voiceName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: 0000 */
    public Live2DMotion loadMotion(String dir, String filename) {
        try {
            return Live2DMotion.loadMotion(UtFile.load(this.live2DManager.getFileManager().open_resource(dir + "/" + filename + ".mtn")));
        } catch (Exception e) {
            System.err.printf("failed to load motion :: %s\n", new Object[]{filename});
            return new Live2DMotion();
        }
    }

    public void setMainMotion(Live2DMotion motion) {
        motion.reinit();
        this.mainMotionMgr.startMotion(motion, false);
    }

    public void setMouthOpen(float v01) {
        this.mouthOpen = v01;
    }

    public void setExpression(String expressionID) {
        AMotion motion = (AMotion) this.expressions.get(expressionID);
        if (motion != null) {
            this.expressionMgr.startMotion(motion, false);
            return;
        }
        System.err.printf("expression[%s] is null  \t\t\t\t\t@@LAppAnimation\n", expressionID);
    }

    public void startTouchMotion(String motionName) {
        Logger.d(motionName);
        this.mainMotionMgr.startMotion((AMotion) this.motionTouchMap.get(motionName), false);
    }

    public void tapEvent(int tapCount, float x, float y) {
    }

    public void touchesBegan(float logicalX, float logicalY, int touchNum) {
        this.touching = true;
        this.mouseX = ((logicalX - 640.0f) * 2.0f) / 1280.0f;
        this.mouseY = ((logicalY - 640.0f) * 2.0f) / 1280.0f;
        this.faceTargetX = range(this.mouseX * 1.5f, -1.0f, 1.0f);
        this.faceTargetY = range(this.mouseY * 1.5f, -1.0f, 1.0f);
        this.eyeMotion.setInterval(EYE_INTERVAL_TOUCHING);
        this._flipStartX = logicalX;
        this._flipStartY = logicalY;
        this._lastX = logicalX;
        this._lastY = logicalY;
        this._totalD = 0.0f;
        this._touchSingle = touchNum == 1;
        this._flipAvailable = true;
        Logger.d("faceTargetX : faceTargetY = " + this.faceTargetX + " : " + this.faceTargetY);
        Logger.d("MouseX : MouseY = " + this.mouseX + " : " + this.mouseY);
    }

    private static boolean contains(float tx, float ty, float x, float y, float w, float h) {
        return x <= tx && tx <= x + w && y <= ty && ty <= y + h;
    }

    public void touchesMoved(float logicalX, float logicalY, int touchNum) {
        this.mouseX = (2.0f * (logicalX - 640.0f)) / 1280.0f;
        this.mouseY = (2.0f * (logicalY - 640.0f)) / 1280.0f;
        this.faceTargetX = range(this.mouseX * 1.5f, -1.0f, 1.0f);
        this.faceTargetY = range(this.mouseY * 1.5f, -1.0f, 1.0f);
        this._totalD = (float) (((double) this._totalD) + Math.sqrt((double) (((logicalX - this._lastX) * (logicalX - this._lastX)) + ((logicalY - this._lastY) * (logicalY - this._lastY)))));
        this._lastX = logicalX;
        this._lastY = logicalY;
        boolean z = this._touchSingle && touchNum == 1;
        this._touchSingle = z;
        if (this._touchSingle && this._totalD > 500.0f && this._flipAvailable) {
            if (contains(this._flipStartX, this._flipStartY, 400.0f, 200.0f, 400.0f, 400.0f)) {
                Logger.d("flip head!!");
            } else if (contains(this._flipStartX, this._flipStartY, 300.0f, 600.0f, 600.0f, 600.0f)) {
                Logger.d("flip body!!");
            }
            this._flipAvailable = false;
        }
    }

    public void touchesEnded() {
        this.touching = false;
        this.faceTargetX = 0.0f;
        this.faceTargetY = 0.0f;
        this.eyeMotion.setInterval(EYE_INTERVAL_NORMAL);
    }

    public void shakeEvent() {
    }

    public void initParam(ALive2DModel model) {
        model.setParamFloat("PARAM_BODY_DIR", 15.0f);
        model.setParamFloat("PARAM_ANGLE_X", -15.0f);
        model.setParamFloat("PARAM_ANGLE_Y", -3.0f);
        model.setParamFloat("PARAM_ANGLE_Z", -15.0f);
        model.setParamFloat("PARAM_EYE_BALL_X", 0.0f);
        model.setParamFloat("PARAM_EYE_BALL_Y", -0.0f);
        model.setParamFloat("PARAM_EYE_L_OPEN", 1.0f);
        model.setParamFloat("PARAM_EYE_R_OPEN", 1.0f);
        model.setParamFloat("PARAM_SMILE", 0.5f);
        model.setParamFloat("PARAM_TERE", 0.2f);
        model.setParamFloat("PARAM_MOUTH_FORM", 0.5f);
        model.setParamFloat("PARAM_MOUTH_OPEN_Y", 0.0f);
        model.setParamFloat("PARAM_BROW_L_Y", 0.5f);
        model.setParamFloat("PARAM_BROW_R_Y", 0.5f);
    }

    public void updateParam(ALive2DModel model) {
        if (model != null) {
            this.timeMSec = UtSystem.getUserTimeMSec() - this.startTimeMSec;
            this.timeDoubleSec = ((double) this.timeMSec) / 1000.0d;
            double t = (this.timeDoubleSec * 2.0d) * 3.141592653589793d;
            model.loadParam();
            if (this.mainMotionMgr.isFinished()) {
                startMainMotion();
            } else if (!this.mainMotionMgr.updateParam(model)) {
                this.eyeMotion.setParam(model);
            }
            model.saveParam();
            this.expressionMgr.updateParam(model);
            updateDragMotion(model);
            model.setParamFloat("PARAM_BREATH", (float) ((Math.sin(t / 3.2345d) * 0.5d) + 0.5d));
            model.addToParamFloat("PARAM_ALL_X", (float) (0.10000000149011612d * Math.sin(t / 9.5345d)), 0.5f);
            model.addToParamFloat("PARAM_BODY_ANGLE_X", (float) (Math.sin(t / 15.5345d) * 4.0d), 0.5f);
            model.addToParamFloat("PARAM_BODY_ANGLE_Y", (float) (Math.sin(t / 12.5345d) * 4.0d), 0.5f);
            model.addToParamFloat("PARAM_ANGLE_X", (float) (15.0d * Math.sin(t / 6.5345d)), 0.5f);
            model.addToParamFloat("PARAM_ANGLE_Y", (float) (8.0d * Math.sin(t / 3.5345d)), 0.5f);
            model.addToParamFloat("PARAM_ANGLE_Z", (float) (10.0d * Math.sin(t / 5.5345d)), 0.5f);
            updateAuto(model);
        }
    }

    private int rand() {
        return rand.nextInt(Integer.MAX_VALUE);
    }

    private void updateDragMotion(ALive2DModel model) {
        if (this.lastTimeSec == 0) {
            this.lastTimeSec = UtSystem.getUserTimeMSec();
            return;
        }
        long curTimeSec = UtSystem.getUserTimeMSec();
        float deltaTimeWeight = (((float) (curTimeSec - this.lastTimeSec)) * 30.0f) / 1000.0f;
        this.lastTimeSec = curTimeSec;
        float MAX_A = (0.17777778f * deltaTimeWeight) / 4.5f;
        float dx = this.faceTargetX - this.faceX;
        float dy = this.faceTargetY - this.faceY;
        if (dx != 0.0f || dy != 0.0f) {
            float d = (float) Math.sqrt((double) ((dx * dx) + (dy * dy)));
            float ax = ((0.17777778f * dx) / d) - this.faceVX;
            float ay = ((0.17777778f * dy) / d) - this.faceVY;
            float a = (float) Math.sqrt((double) ((ax * ax) + (ay * ay)));
            if (a < (-MAX_A) || a > MAX_A) {
                ax *= MAX_A / a;
                ay *= MAX_A / a;
                a = MAX_A;
            }
            this.faceVX += ax;
            this.faceVY += ay;
            float max_v = 0.5f * (((float) Math.sqrt((double) (((MAX_A * MAX_A) + ((16.0f * MAX_A) * d)) - ((8.0f * MAX_A) * d)))) - MAX_A);
            float cur_v = (float) Math.sqrt((double) ((this.faceVX * this.faceVX) + (this.faceVY * this.faceVY)));
            if (cur_v > max_v) {
                this.faceVX *= max_v / cur_v;
                this.faceVY *= max_v / cur_v;
            }
            this.faceX += this.faceVX;
            this.faceY += this.faceVY;
            float zzz = this.faceX * this.faceY;
            model.addToParamFloat("PARAM_ANGLE_X", range(this.faceX * 30.0f, -30.0f, 30.0f), 1.0f);
            model.addToParamFloat("PARAM_ANGLE_Y", range((-this.faceY) * 30.0f, -30.0f, 30.0f), 1.0f);
            model.addToParamFloat("PARAM_ANGLE_Z", range(30.0f * zzz, -30.0f, 30.0f), 1.0f);
            model.addToParamFloat("PARAM_BODY_ANGLE_X", range(this.faceX * 5.0f, -10.0f, 10.0f), 1.0f);
            model.addToParamFloat("PARAM_EYE_BALL_X", range(this.faceX, -1.0f, 1.0f), 1.0f);
            model.addToParamFloat("PARAM_EYE_BALL_Y", range(-this.faceY, -1.0f, 1.0f), 1.0f);
            if (this.faceY < -0.5f) {
                model.addToParamFloat("PARAM_BASE_Y", ((-this.faceY) - 0.5f) * 20.0f, 1.0f);
            }
        }
    }

    private void updateAuto(ALive2DModel model) {
        float loop4x;
        float loop4y;
        int t4 = (int) ((this.timeMSec / 2) % 4000);
        if (t4 < 1000) {
            loop4x = ((float) t4) / 1000.0f;
            loop4y = 0.0f;
        } else if (t4 < 2000) {
            loop4x = 1.0f;
            loop4y = ((float) (t4 - 1000)) / 1000.0f;
        } else if (t4 < 3000) {
            loop4x = 1.0f - (((float) (t4 - 2000)) / 1000.0f);
            loop4y = 1.0f;
        } else {
            loop4x = 0.0f;
            loop4y = 1.0f - (((float) (t4 - 3000)) / 1000.0f);
        }
        model.setParamFloat("PARAM_LOOP_4_SEC_X", loop4x);
        model.setParamFloat("PARAM_LOOP_4_SEC_Y", loop4y);
    }

    private float range(float v, float min, float max) {
        if (v < min) {
            v = min;
        }
        if (v > max) {
            return max;
        }
        return v;
    }
}
