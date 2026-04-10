package jp.co.a_tm.moeyu;

import java.util.ArrayList;
import java.util.List;
import jp.co.a_tm.moeyu.model.UserData;

/**
 * 特殊事件配置类
 * 定义特定物品在特定场景和区域的组合触发的事件语音链
 */
public class SpecialEvent {
    /** 物品ID: 沐浴露 (shampoo) */
    private static final int ITEM_ID_SHAMPOO = 20;
    /** 物品ID: 毛巾 */
    private static final int ITEM_ID_TOWEL = 1;
    /** 物品ID: 梳子 */
    private static final int ITEM_ID_BRUSH = 13;
    /** 物品ID: 镜子 */
    private static final int ITEM_ID_MIRROR = 16;

    /** 沐浴露事件语音链（bath_b场景, belly区域） */
    private static final String[] VOICE_CHAIN_SHAMPOO = {"227", "228", "229", "230"};
    /** 毛巾事件语音链（head场景, head区域） */
    private static final String[] VOICE_CHAIN_TOWEL = {"143", "222", "223", "201"};
    /** 梳子事件语音链（bath_a场景, face区域） */
    private static final String[] VOICE_CHAIN_BRUSH = {"157", "186", "187", "188"};
    /** 镜子事件语音链（bath_b场景, face区域） */
    private static final String[] VOICE_CHAIN_MIRROR = {"206", "207", "208"};

    public static List<String> get(UserData userData, int item, Scene scene, Region region) {
        if (ITEM_ID_SHAMPOO == item && Scene.bath_b == scene && Region.belly == region) {
            return toStringList(VOICE_CHAIN_SHAMPOO);
        } else if (ITEM_ID_TOWEL == item && Scene.head == scene && Region.head == region) {
            return toStringList(VOICE_CHAIN_TOWEL);
        } else if (ITEM_ID_BRUSH == item && Scene.bath_a == scene && Region.face == region) {
            return toStringList(VOICE_CHAIN_BRUSH);
        } else if (ITEM_ID_MIRROR == item && Scene.bath_b == scene && Region.face == region) {
            return toStringList(VOICE_CHAIN_MIRROR);
        } else {
            return toStringList(new String[0]);
        }
    }

    /**
     * 将字符串数组转换为列表
     *
     * @param array 字符串数组
     * @return 字符串列表
     */
    private static List<String> toStringList(String... array) {
        List<String> list = new ArrayList<>();
        for (String element : array) {
            list.add(element);
        }
        return list;
    }
}
