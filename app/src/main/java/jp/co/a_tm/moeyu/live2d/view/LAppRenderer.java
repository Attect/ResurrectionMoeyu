package jp.co.a_tm.moeyu.live2d.view;

import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import jp.co.a_tm.moeyu.Scene;
import jp.co.a_tm.moeyu.live2d.LAppDefine;
import jp.co.a_tm.moeyu.live2d.LAppLive2DManager;
import jp.co.a_tm.moeyu.live2d.model.LAppModel;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;
import jp.live2d.android.Live2DModelAndroid;
import jp.live2d.android.UtOpenGL;
import jp.live2d.type.LDRect;
import jp.live2d.type.LDRectF;
import jp.live2d.util.UtSystem;

public class LAppRenderer implements Renderer, LAppDefine {
    static float acceleration_x = 0.0f;
    static float acceleration_y = 0.0f;
    static float acceleration_z = 0.0f;
    static float dst_acceleration_x = 0.0f;
    static float dst_acceleration_y = 0.0f;
    static float dst_acceleration_z = 0.0f;
    static float lastMove;
    static long lastTimeMSec = -1;
    static float last_dst_acceleration_x = 0.0f;
    static float last_dst_acceleration_y = 0.0f;
    static float last_dst_acceleration_z = 0.0f;
    private final float MAX_ACCEL_D;
    float[] accel;
    LDRectF backDstR;
    String backImagePath;
    boolean backImageUpdated;
    LDRectF backSrcR;
    int backingHeight;
    int backingWidth;
    GL10 gl;
    public boolean isAr;
    float[] lastAccel;
    LAppLive2DManager live2DMgr;
    float logicalH;
    float logicalW;
    private Scene mScene;
    private int[] mWallTextures;
    private int[] mWaterBacks;
    private int[] mWaterFronts;
    Live2DModelAndroid model;
    int renderCount;
    float targetX;
    float targetY;
    LAppGLView view;
    LDRect visibleRect;

    public void setScene(Scene scene) {
        this.mScene = scene;
        getAnimation().setScene(scene);
    }

    public LAppRenderer(LAppLive2DManager sampleLive2D, LAppGLView view) {
        this(sampleLive2D, view, false);
    }

    public LAppRenderer(LAppLive2DManager sampleLive2D, LAppGLView view, boolean isAr) {
        this.visibleRect = new LDRect();
        this.accel = new float[3];
        this.lastAccel = new float[3];
        this.backSrcR = new LDRectF(0.0f, 0.0f, 1.0f, 1.0f);
        this.backDstR = new LDRectF(0.0f, 0.0f, 1.0f, 1.0f);
        this.mScene = Scene.bath_a;
        this.mWallTextures = new int[2];
        this.mWaterBacks = new int[2];
        this.mWaterFronts = new int[2];
        this.isAr = false;
        this.MAX_ACCEL_D = 0.04f;
        this.live2DMgr = sampleLive2D;
        this.view = view;
        this.isAr = isAr;
        setBackgroundImage(null, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    /* access modifiers changed from: 0000 */
    public float fabs(float v) {
        return v > 0.0f ? v : -v;
    }

    private LAppAnimation getAnimation() {
        if (this.live2DMgr == null) {
            return null;
        }
        return this.live2DMgr.getAnimation();
    }

    public void setCurAccel(float a1, float a2, float a3) {
        dst_acceleration_x = a1;
        dst_acceleration_y = a2;
        dst_acceleration_z = a3;
        lastMove = (lastMove * 0.7f) + (0.3f * ((fabs(dst_acceleration_x - last_dst_acceleration_x) + fabs(dst_acceleration_y - last_dst_acceleration_y)) + fabs(dst_acceleration_z - last_dst_acceleration_z)));
        last_dst_acceleration_x = dst_acceleration_x;
        last_dst_acceleration_y = dst_acceleration_y;
        last_dst_acceleration_z = dst_acceleration_z;
        if (lastMove > 1.5f) {
            LAppAnimation ka = getAnimation();
            if (ka != null) {
                ka.shakeEvent();
            }
            lastMove = 0.0f;
        }
    }

    /* access modifiers changed from: 0000 */
    public void updateAccel() {
        float dx = dst_acceleration_x - acceleration_x;
        float dy = dst_acceleration_y - acceleration_y;
        float dz = dst_acceleration_z - acceleration_z;
        if (dx > 0.04f) {
            dx = 0.04f;
        }
        if (dx < -0.04f) {
            dx = -0.04f;
        }
        if (dy > 0.04f) {
            dy = 0.04f;
        }
        if (dy < -0.04f) {
            dy = -0.04f;
        }
        if (dz > 0.04f) {
            dz = 0.04f;
        }
        if (dz < -0.04f) {
            dz = -0.04f;
        }
        acceleration_x += dx;
        acceleration_y += dy;
        acceleration_z += dz;
        long time = UtSystem.getUserTimeMSec();
        long diff = time - lastTimeMSec;
        lastTimeMSec = time;
        float scale = ((0.2f * ((float) diff)) * 60.0f) / 1000.0f;
        if (scale > 0.5f) {
            scale = 0.5f;
        }
        this.accel[0] = (acceleration_x * scale) + (this.accel[0] * (1.0f - scale));
        this.accel[1] = (acceleration_y * scale) + (this.accel[1] * (1.0f - scale));
        this.accel[2] = (acceleration_z * scale) + (this.accel[2] * (1.0f - scale));
    }

    public GL10 getGL() {
        return this.gl;
    }

    public void onDrawFrame(GL10 gl) {
        if (this.logicalW > 0.0f && this.logicalH > 0.0f) {
            int i = this.renderCount;
            this.renderCount = i + 1;
            if (i < 10 || i % 60 == 0) {
                Log.d("LIVE2D_DEBUG", "onDrawFrame: renderCount=" + i
                    + " logicalW=" + this.logicalW + " logicalH=" + this.logicalH
                    + " backingW=" + this.backingWidth + " backingH=" + this.backingHeight
                    + " isAr=" + this.isAr + " scene=" + this.mScene);
            }
            if (i % 60 == 0) {
                // 全屏视口，填满整个屏幕
                gl.glViewport(0, 0, this.backingWidth, this.backingHeight);
                gl.glMatrixMode(GL10.GL_PROJECTION);
                gl.glLoadIdentity();
                gl.glOrthof(0.0f, this.logicalW, this.logicalH, 0.0f, 0.5f, -0.5f);
                gl.glMatrixMode(GL10.GL_MODELVIEW);
                gl.glLoadIdentity();
                this.visibleRect.a = (int) 190.0f;
                this.visibleRect.b = 0;
                this.visibleRect.c = (int) this.logicalW;
                this.visibleRect.d = (int) this.logicalH;
                gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
            }
            // 不透明清屏（EGL 已去掉 Alpha 通道，Alpha 值被忽略）
            gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            gl.glClear(0x00004000);
            renderMain(gl);
        } else {
            Log.w("LIVE2D_DEBUG", "onDrawFrame: SKIP rendering! logicalW=" + this.logicalW
                + " logicalH=" + this.logicalH + " (surface not ready?)");
        }
    }

    /* access modifiers changed from: 0000 */
    public void renderMain(GL10 gl) {
        gl.glEnable(3042);
        gl.glDisable(2929);
        gl.glBlendFunc(1, 771);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glDisable(2884);
        gl.glMatrixMode(5888);
        gl.glLoadIdentity();
        gl.glEnable(3553);
        // 显式设置纹理环境模式为 GL_MODULATE，防止被其他代码改为 GL_BLEND 等导致黑屏
        gl.glTexEnvf(8960, 8704, 8448.0f);
        // 禁用可能冲突的客户端状态数组，避免之前状态残留导致绘制失败
        gl.glDisableClientState(32885); // GL_NORMAL_ARRAY
        gl.glDisableClientState(32886); // GL_COLOR_ARRAY
        gl.glEnableClientState(32888);  // GL_TEXTURE_COORD_ARRAY
        gl.glEnableClientState(32884);  // GL_VERTEX_ARRAY
        updateAccel();
        float ACCEL_PIX = this.logicalW / 6.0f;
        if (!this.isAr) {
            gl.glPushMatrix();
            gl.glTranslatef((-0.3f * ACCEL_PIX) * this.accel[0], (0.060000002f * ACCEL_PIX) * this.accel[1], 0.0f);
            gl.glTranslatef(-80.0f, 0.0f, 0.0f);
            gl.glScalef(480.0f, 480.0f, 1.0f);
            int i = (this.mScene == Scene.bath_a || this.mScene == Scene.bath_b) ? 0 : 1;
            if (this.renderCount <= 3) {
                Log.d("LIVE2D_DEBUG", "renderMain: drawing wall texture index=" + i
                    + " wallTex[0]=" + this.mWallTextures[0] + " wallTex[1]=" + this.mWallTextures[1]);
            }
            drawImageDirect(gl, this.mWallTextures[i], this.backDstR.a, this.backDstR.b, this.backDstR.c, this.backDstR.d, this.backSrcR.a, this.backSrcR.b, this.backSrcR.c, this.backSrcR.d);
            gl.glPopMatrix();
            if (Scene.bath_a == this.mScene || Scene.bath_b == this.mScene) {
                gl.glPushMatrix();
                gl.glEnable(3042);
                gl.glTexEnvf(8960, 8704, 8448.0f);
                gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
                gl.glTranslatef(-80.0f, 0.0f, 0.0f);
                gl.glScalef(480.0f, 480.0f, 1.0f);
                drawImageDirect(gl, this.mWaterBacks[0], this.backDstR.a, this.backDstR.b, this.backDstR.c, this.backDstR.d, this.backSrcR.a, this.backSrcR.b, this.backSrcR.c, this.backSrcR.d);
                gl.glPopMatrix();
            }
        }
        gl.glPushMatrix();
        try {
            gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            gl.glTranslatef((-0.15f * ACCEL_PIX) * this.accel[0], (0.030000001f * ACCEL_PIX) * this.accel[1], 0.0f);
            gl.glTranslatef(-80.0f, -20.0f, 0.0f);
            gl.glScalef(0.13f, 0.12f, 1.0f);
            LAppModel kmodel = this.live2DMgr.getModel(gl);
            boolean modelNull = (kmodel == null);
            boolean modelUpdating = this.live2DMgr.isModelUpdating();
            boolean modelInitialized = kmodel != null && kmodel.isModelInitialized();
            // [DEBUG] 模型绘制条件（仅前10帧+每60帧打印一次）
            if (this.renderCount <= 10 || this.renderCount % 60 == 0) {
                Log.d("LIVE2D_DEBUG", "renderMain: model=" + (kmodel != null ? kmodel.getClass().getSimpleName() : "NULL")
                    + " isNull=" + modelNull + " isUpdating=" + modelUpdating
                    + " isInitialized=" + modelInitialized
                    + " willDraw=" + (!modelNull && !modelUpdating && modelInitialized));
            }
            if (!(kmodel == null || this.live2DMgr.isModelUpdating() || !kmodel.isModelInitialized())) {
                kmodel.setAccelarationValue(this.accel);
                try {
                    kmodel.drawModel(gl);
                } catch (Exception e) {
                    // [DEBUG] 模型绘制异常
                    Log.e("LIVE2D_DEBUG", "renderMain: drawModel EXCEPTION", e);
                    e.printStackTrace();
                }
            }
        } catch (Exception e2) {
            // [DEBUG] 模型获取异常
            Log.e("LIVE2D_DEBUG", "renderMain: getModel EXCEPTION", e2);
            e2.printStackTrace();
        }
        gl.glPopMatrix();
        if (!this.isAr) {
            if (Scene.bath_a == this.mScene || Scene.bath_b == this.mScene) {
                gl.glPushMatrix();
                gl.glEnable(3042);
                gl.glTexEnvf(8960, 8704, 8448.0f);
                gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
                gl.glTranslatef(-80.0f, 0.0f, 0.0f);
                gl.glScalef(480.0f, 480.0f, 1.0f);
                drawImageDirect(gl, this.mWaterFronts[0], this.backDstR.a, this.backDstR.b, this.backDstR.c, this.backDstR.d, this.backSrcR.a, this.backSrcR.b, this.backSrcR.c, this.backSrcR.d);
                gl.glPopMatrix();
            }
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.backingWidth = width;
        this.backingHeight = height;
        this.logicalW = 320.0f;
        this.logicalH = 480.0f;
        // 全屏视口，填满整个屏幕，UI 叠加在上方
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrthof(0.0f, this.logicalW, this.logicalH, 0.0f, 0.5f, -0.5f);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        this.visibleRect.a = (int) 190.0f;
        this.visibleRect.b = 0;
        this.visibleRect.c = (int) this.logicalW;
        this.visibleRect.d = (int) this.logicalH;
        Log.d("LIVE2D_DEBUG", "onSurfaceChanged: width=" + width + " height=" + height
            + " logicalW=" + this.logicalW + " logicalH=" + this.logicalH);
        System.out.printf("onSurfaceChanged( %d , %d ) \t\t@@LAppRenderer\n", new Object[]{Integer.valueOf(this.backingWidth), Integer.valueOf(this.backingHeight)});
        this.renderCount = 0;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig arg1) {
        // [DEBUG] 确认OpenGL表面创建
        Log.d("LIVE2D_DEBUG", "onSurfaceCreated: isAr=" + this.isAr + " gl=" + gl);
        System.out.printf("onSurfaceCreated() \t\t\t\t\t\t@@LAppRenderer\n", new Object[0]);
        this.gl = gl;
        if (!this.isAr) {
            // 先释放旧纹理，防止同一 GL 上下文内重复加载导致泄漏
            releaseBackgroundTextures(gl);
            this.mWaterBacks[0] = UtOpenGL.loadTexture(gl, this.view.getContext(), "water_back00.png", true);
            this.mWaterFronts[0] = UtOpenGL.loadTexture(gl, this.view.getContext(), "water_front00.png", true);
            this.mWallTextures[0] = UtOpenGL.loadTexture(gl, this.view.getContext(), "water_wall00.png", true);
            this.mWallTextures[1] = UtOpenGL.loadTexture(gl, this.view.getContext(), "water_wall01.png", true);
            // [DEBUG] 纹理加载结果
            Log.d("LIVE2D_DEBUG", "onSurfaceCreated: textures loaded -"
                + " waterBack[0]=" + this.mWaterBacks[0]
                + " waterFront[0]=" + this.mWaterFronts[0]
                + " wallTex[0]=" + this.mWallTextures[0]
                + " wallTex[1]=" + this.mWallTextures[1]);
        } else {
            Log.d("LIVE2D_DEBUG", "onSurfaceCreated: AR mode, skipping texture loading");
        }
        // EGL 上下文重建后，通知管理器释放已失效的模型纹理并标记重载
        if (this.live2DMgr != null) {
            this.live2DMgr.onGLContextCreated(gl);
        }
    }

    /**
     * 释放背景纹理，防止 GPU 内存泄漏
     */
    private void releaseBackgroundTextures(GL10 gl) {
        int[] textures = new int[] {
                this.mWaterBacks[0], this.mWaterFronts[0],
                this.mWallTextures[0], this.mWallTextures[1]
        };
        for (int texId : textures) {
            if (texId != 0) {
                gl.glDeleteTextures(1, new int[]{texId}, 0);
            }
        }
        this.mWaterBacks[0] = 0;
        this.mWaterFronts[0] = 0;
        this.mWallTextures[0] = 0;
        this.mWallTextures[1] = 0;
        Log.d("LIVE2D_DEBUG", "releaseBackgroundTextures: old textures released");
    }

    /**
     * 使用 direct buffer 绘制带纹理的四边形。
     * Android 14 (API 34) 起，glVertexPointer/glTexCoordPointer 等方法
     * 强制要求使用 ByteBuffer.allocateDirect() 创建的 native order direct buffer，
     * 否则绘制会被静默忽略（黑屏）。Live2D SDK 的 UtOpenGL.drawImage 使用旧式
     * heap buffer，在 Android 14 上失效，因此需要自行实现。
     */
    private void drawImageDirect(GL10 gl, int texture, float x1, float y1, float x2, float y2, float u1, float v1, float u2, float v2) {
        gl.glBindTexture(GL10.GL_TEXTURE_2D, texture);
        // 显式设置纹理过滤和环绕参数，防止 loadTexture 未设置或设置错误导致黑屏
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

        float[] vertices = {x1, y1, x2, y1, x1, y2, x2, y2};
        float[] texCoords = {u1, v1, u2, v1, u1, v2, u2, v2};

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        FloatBuffer vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4);
        tbb.order(ByteOrder.nativeOrder());
        FloatBuffer texCoordBuffer = tbb.asFloatBuffer();
        texCoordBuffer.put(texCoords);
        texCoordBuffer.position(0);

        gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer);
        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texCoordBuffer);
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
    }

    public boolean setBackgroundImage(String filepath, float sx, float sy, float sw, float sh, float dx, float dy, float dw, float dh) {
        this.backSrcR.a = sx;
        this.backSrcR.b = sy;
        this.backSrcR.c = sw;
        this.backSrcR.d = sh;
        this.backDstR.a = sx;
        this.backDstR.b = sy;
        this.backDstR.c = sw;
        this.backDstR.d = sh;
        if (filepath == null) {
            filepath = "water_wall00.png";
        }
        this.backImagePath = filepath;
        this.backImageUpdated = true;
        return true;
    }

    public float viewToLogicalX(float x) {
        return ((float) this.visibleRect.a) + (((float) this.visibleRect.c) * (x / this.logicalW));
    }

    public float viewToLogicalY(float y) {
        return ((float) this.visibleRect.b) + (((float) this.visibleRect.d) * (y / this.logicalH));
    }
}
