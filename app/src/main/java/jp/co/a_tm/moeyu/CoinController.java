package jp.co.a_tm.moeyu;

public class CoinController {
    public static final String BRONZE = "bronze";
    public static final String GOLD = "gold";
    public static final int INIT_BRONZE_COIN = 3;
    public static final int INIT_GOLD_COIN = 0;
    public static final int INIT_PLATINUM_COIN = 0;
    public static final int MAX_COIN = 99;
    public static final String PLATINUM = "platinum";

    public static int roundingNumber(int number) {
        if (number < 0) {
            return 0;
        }
        if (number > 99) {
            return 99;
        }
        return number;
    }
}
