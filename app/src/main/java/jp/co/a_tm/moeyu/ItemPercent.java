package jp.co.a_tm.moeyu;

import android.content.Context;
import android.util.Log;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;

public class ItemPercent {
    private final int[] BRONZE = new int[]{0, 1, 2, 3, 4, 2004, 4004, 6004, 8004, 10004, 14004, 18004, 22004, 26004, 30004, 34004, 38004, 42004, 46004, 50004, 80000, 110000, 140000, 170000, 200000};
    private final int[] GOLD = new int[]{6, 26, 48, 70, 110, 150, LAppAnimation.FLIP_START_FACE_Y, 250, LAppAnimation.FLIP_START_BODY_X, 350, 400, 450, 500, 550, 600, 650, 700, 750, 800, 850, 880, 910, 940, 970, 1000};
    private final int[] PLATINUM = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25};
    private Context mContext;

    public ItemPercent(Context context) {
        this.mContext = context;
    }

    public int[] getArray(String type) {
        if (type.equals(CoinController.PLATINUM)) {
            ItemTableController controller = new ItemTableController(this.mContext);
            if (controller.countOpened() >= this.PLATINUM.length) {
                return this.PLATINUM;
            }
            boolean[] platinumOpened = controller.isOpened(1, this.PLATINUM.length);
            int[] iArr = new int[this.PLATINUM.length];
            int count = 0;
            for (int id = 0; id < iArr.length; id++) {
                if (platinumOpened[id]) {
                    iArr[id] = count;
                } else {
                    count++;
                    iArr[id] = count;
                }
            }
            return iArr;
        } else if (type.equals(CoinController.GOLD)) {
            return this.GOLD;
        } else {
            if (type.equals(CoinController.BRONZE)) {
                return this.BRONZE;
            }
            Log.e("ItemGetArrat", "invalid type");
            return null;
        }
    }
}
