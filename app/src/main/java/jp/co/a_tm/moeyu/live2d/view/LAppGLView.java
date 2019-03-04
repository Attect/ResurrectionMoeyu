package jp.co.a_tm.moeyu.live2d.view;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import jp.co.a_tm.moeyu.PreferenceActivity;
import jp.co.a_tm.moeyu.live2d.LAppLive2DManager;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;
import jp.co.a_tm.moeyu.util.Logger;
import jp.live2d.type.LDPointF;

public class LAppGLView extends GLSurfaceView {
    GestureDetector gestureDetector;
    private float lastD = -1.0f;
    private float last_p1x;
    private float last_p1y;
    private float last_p2x;
    private float last_p2y;
    /* access modifiers changed from: private */
    public float lastx;
    /* access modifiers changed from: private */
    public float lasty;
    LAppLive2DManager live2DMgr;
    LAppRenderer renderer;
    private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
        public boolean onDoubleTap(MotionEvent event) {
            return tapEvent(2) | super.onDoubleTap(event);
        }

        public boolean onDown(MotionEvent event) {
            super.onDown(event);
            return true;
        }

        public boolean onSingleTapConfirmed(MotionEvent event) {
            return tapEvent(1) | super.onSingleTapUp(event);
        }

        public boolean onSingleTapUp(MotionEvent event) {
            return super.onSingleTapUp(event);
        }

        private boolean tapEvent(int tapCount) {
            float logicalX = LAppGLView.this.renderer.viewToLogicalX(LAppGLView.this.lastx);
            float logicalY = LAppGLView.this.renderer.viewToLogicalY(LAppGLView.this.lasty);
            LAppAnimation kanim = LAppGLView.this.getMyAnimation();
            if (kanim == null) {
                return false;
            }
            kanim.tapEvent(tapCount, logicalX, logicalY);
            return true;
        }
    };

    public LAppGLView(LAppLive2DManager sampleLive2D, Activity activity) {
        super(activity);
        boolean isAr = PreferenceActivity.isEnableCamera(activity);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(-3);
        setZOrderOnTop(isAr);
        setFocusable(true);
        this.live2DMgr = sampleLive2D;
        this.renderer = new LAppRenderer(sampleLive2D, this, isAr);
        setRenderer(this.renderer);
        this.gestureDetector = new GestureDetector(getContext(), this.simpleOnGestureListener);
    }

    public void startAnimation() {
    }

    public void stopAnimation() {
    }

    public LAppRenderer getRenderer() {
        return this.renderer;
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = false;
        switch (event.getAction()) {
            case 0:
                ret = true;
                touchesBegan(event);
                break;
            case 1:
                touchesEnded(event);
                break;
            case 2:
                touchesMoved(event);
                break;
        }
        return ret | this.gestureDetector.onTouchEvent(event);
    }

    /* access modifiers changed from: 0000 */
    public void touchesBegan(MotionEvent event) {
        int touchNum = event.getPointerCount();
        if (touchNum == 1) {
            this.lastx = event.getX();
            this.lasty = event.getY();
            this.lastD = -1.0f;
        } else if (touchNum >= 2) {
            this.last_p1x = event.getX(0);
            this.last_p1y = event.getY(0);
            this.last_p2x = event.getX(1);
            this.last_p2y = event.getY(1);
            float dist = (float) Math.sqrt((double) (((this.last_p1x - this.last_p2x) * (this.last_p1x - this.last_p2x)) + ((this.last_p1y - this.last_p2y) * (this.last_p1y - this.last_p2y))));
            float cy = (this.last_p1y + this.last_p2y) * 0.5f;
            this.lastx = (this.last_p1x + this.last_p2x) * 0.5f;
            this.lasty = cy;
            this.lastD = dist;
        }
        LAppAnimation anim = getMyAnimation();
        if (anim != null) {
            float logicalX = this.renderer.viewToLogicalX(this.lastx);
            float logicalY = this.renderer.viewToLogicalY(this.lasty);
            anim.touchesBegan(logicalX, logicalY, touchNum);
            Logger.d("logicalX : logicalY = " + logicalX + " : " + logicalY);
        }
    }

    /* access modifiers changed from: 0000 */
    public float calcShift(float v1, float v2) {
        Object obj = 1;
        Object obj2 = v1 > 0.0f ? 1 : null;
        if (v2 <= 0.0f) {
            obj = null;
        }
        if (obj2 != obj) {
            return 0.0f;
        }
        float fugou = v1 > 0.0f ? 1.0f : -1.0f;
        float a1 = Math.abs(v1);
        float a2 = Math.abs(v2);
        if (a1 >= a2) {
            a1 = a2;
        }
        return fugou * a1;
    }

    /* access modifiers changed from: 0000 */
    public void touchesMoved(MotionEvent event) {
        int touchNum = event.getPointerCount();
        LDPointF lDPointF;
        if (touchNum == 1) {
            lDPointF = new LDPointF(event.getX(), event.getY());
            this.lastx = lDPointF.a;
            this.lasty = lDPointF.b;
            this.lastD = -1.0f;
        } else if (touchNum >= 2) {
            int index1 = 0;
            int index2 = 0;
            int minDist2 = 999999999;
            for (int i1 = 0; i1 < touchNum; i1++) {
                LDPointF pp1 = new LDPointF(event.getX(i1), event.getY(i1));
                for (int i2 = 0; i2 < touchNum; i2++) {
                    if (i1 != i2) {
                        lDPointF = new LDPointF(event.getX(i2), event.getY(i2));
                        int distTotal = (int) ((((this.last_p1x - pp1.a) * (this.last_p1x - pp1.a)) + ((this.last_p1y - pp1.b) * (this.last_p1y - pp1.b))) + (((this.last_p2x - lDPointF.a) * (this.last_p2x - lDPointF.a)) + ((this.last_p2y - lDPointF.b) * (this.last_p2y - lDPointF.b))));
                        if (distTotal < minDist2) {
                            minDist2 = distTotal;
                            index1 = i1;
                            index2 = i2;
                        }
                    }
                }
            }
            if (minDist2 <= 9800 || touchNum <= 2) {
                lDPointF = new LDPointF(event.getX(index1), event.getY(index1));
                lDPointF = new LDPointF(event.getX(index2), event.getY(index2));
                float dist = (float) Math.sqrt((double) (((lDPointF.a - lDPointF.a) * (lDPointF.a - lDPointF.a)) + ((lDPointF.b - lDPointF.b) * (lDPointF.b - lDPointF.b))));
                float cx = (lDPointF.a + lDPointF.a) * 0.5f;
                float cy = (lDPointF.b + lDPointF.b) * 0.5f;
                if (this.lastD > 0.0f) {
                }
                this.lastx = cx;
                this.lasty = cy;
                this.last_p1x = lDPointF.a;
                this.last_p1y = lDPointF.b;
                this.last_p2x = lDPointF.a;
                this.last_p2y = lDPointF.b;
                this.lastD = dist;
            } else {
                return;
            }
        }
        LAppAnimation anim = getMyAnimation();
        if (anim != null) {
            anim.touchesMoved(this.renderer.viewToLogicalX(this.lastx), this.renderer.viewToLogicalY(this.lasty), touchNum);
        }
    }

    /* access modifiers changed from: 0000 */
    public void touchesEnded(MotionEvent event) {
        LAppAnimation kanim = getMyAnimation();
        if (kanim != null) {
            kanim.touchesEnded();
        }
    }

    /* access modifiers changed from: 0000 */
    public LAppAnimation getMyAnimation() {
        if (this.live2DMgr == null) {
            return null;
        }
        return this.live2DMgr.getAnimation();
    }
}
