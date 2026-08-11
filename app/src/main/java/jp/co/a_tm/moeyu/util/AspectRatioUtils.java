package jp.co.a_tm.moeyu.util;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * 屏幕宽高比适配工具
 *
 * 本应用按 9:16 竖屏设计。在比 16:9 更高（如 18:9、20:9）或更宽
 * （平板、折叠屏、分屏）的屏幕上，将内容视图限制为 9:16 并在父容器内居中，
 * 多余区域以黑边填充，避免画面被拉伸变形。
 *
 * 在标准 16:9 屏幕上计算结果与父容器尺寸一致，不产生任何视觉变化。
 *
 * @author Attect
 */
public final class AspectRatioUtils {

    /** 设计宽高比（宽/高 = 9/16） */
    private static final float DESIGN_ASPECT = 9.0f / 16.0f;

    private AspectRatioUtils() {
    }

    /**
     * 将 Activity 的内容根视图限制为 9:16 并居中，黑边填充其余区域。
     * 必须在 setContentView() 之后调用。
     *
     * @param activity 目标 Activity
     */
    public static void applyToContent(Activity activity) {
        FrameLayout content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        // 黑边：内容根视图之外的区域显示为黑色
        content.setBackgroundColor(Color.BLACK);
        View root = content.getChildAt(0);
        if (root != null) {
            fitWithinParent(root);
        }
    }

    /**
     * 将指定视图在其父容器内限制为 9:16 并居中。
     * 通过监听父容器布局变化自动重算，父容器尺寸改变（如分屏调整）时也会保持正确。
     *
     * @param view 需要限制比例的视图（其父容器应为 FrameLayout 以支持居中）
     */
    public static void fitWithinParent(final View view) {
        ViewParent parent = view.getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        final ViewGroup parentView = (ViewGroup) parent;
        parentView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                apply(view, parentView);
            }
        });
        // 父容器已完成布局时立即应用
        if (parentView.getWidth() > 0 && parentView.getHeight() > 0) {
            apply(view, parentView);
        }
    }

    /**
     * 计算 9:16 内容盒尺寸并应用到视图。
     * 屏幕过高时限制高度（上下黑边），过宽时限制宽度（左右黑边）。
     */
    private static void apply(View view, ViewGroup parent) {
        int parentW = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int parentH = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
        if (parentW <= 0 || parentH <= 0) {
            return;
        }
        int width = parentW;
        int height = parentH;
        if (parentH > (int) (parentW / DESIGN_ASPECT)) {
            // 比 16:9 细长的屏幕：限制高度，上下留黑边
            height = (int) (parentW / DESIGN_ASPECT);
        } else if (parentW > (int) (parentH * DESIGN_ASPECT)) {
            // 比 16:9 更宽的屏幕（平板/折叠屏/横屏）：限制宽度，左右留黑边
            width = (int) (parentH * DESIGN_ASPECT);
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new FrameLayout.LayoutParams(width, height, Gravity.CENTER);
        } else {
            // 尺寸未变化时不重复设置，避免触发布局循环
            if (lp.width == width && lp.height == height) {
                return;
            }
            lp.width = width;
            lp.height = height;
            if (lp instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) lp).gravity = Gravity.CENTER;
            }
        }
        view.setLayoutParams(lp);
    }
}
