package jp.co.a_tm.moeyu;

import java.util.ArrayList;
import java.util.List;
import jp.co.a_tm.moeyu.model.UserData;

public class SpecialEvent {
    public static List<String> get(UserData userData, int item, Scene scene, Region region) {
        if (20 == item && Scene.bath_b == scene && Region.belly == region) {
            return variableArray2List("227", "228", "229", "230");
        } else if (1 == item && Scene.head == scene && Region.head == region) {
            return variableArray2List("143", "222", "223", "201");
        } else if (13 == item && Scene.bath_a == scene && Region.face == region) {
            return variableArray2List("157", "186", "187", "188");
        } else if (16 != item || Scene.bath_b != scene || Region.face != region) {
            return variableArray2List(new String[0]);
        } else {
            return variableArray2List("206", "207", "208");
        }
    }

    private static List<String> variableArray2List(String... array) {
        List<String> list = new ArrayList();
        for (String element : array) {
            list.add(element);
        }
        return list;
    }
}
