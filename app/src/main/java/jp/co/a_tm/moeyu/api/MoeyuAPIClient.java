package jp.co.a_tm.moeyu.api;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import androidx.annotation.NonNull;

import jp.co.a_tm.moeyu.CoinController;
import jp.co.a_tm.moeyu.LovePoint;
import jp.co.a_tm.moeyu.api.model.GachaResult;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.util.UserDataManager;

/**
 * 本地化API客户端
 * 
 * 所有远程API调用已移除，仅保留本地化逻辑。
 * 用户数据统一通过UserDataManager管理，消除双重存储问题。
 */
public class MoeyuAPIClient {
    /** 扭蛋概率因子: 青铜币 */
    private static final int RATE_BRONZE = 2;
    /** 扭蛋概率因子: 黄金币 */
    private static final int RATE_GOLD = 3;
    /** 扭蛋概率因子: 白金币 */
    private static final int RATE_PLATINUM = 4;
    /** 扭蛋随机范围（% 10 用于概率计算） */
    private static final int GACHA_RANDOM_RANGE = 10;

    /** 产品ID: 3枚金币 */
    private static final String PRODUCT_GOLD_COIN_3 = "gold_coin_3";
    /** 产品ID: 10枚金币 */
    private static final String PRODUCT_GOLD_COIN_10 = "gold_coin_10";
    /** 产品ID: 1枚白金币 */
    private static final String PRODUCT_PLATINUM_COIN_1 = "platinum_coin_1";

    /** 旧版本地数据文件名（用于迁移） */
    private static final String LEGACY_DATA_FILE = "localUserData.dat";

    private Context mContext;
    private UserDataManager mUserDataManager;
    private LovePoint mLovePoint = new LovePoint();

    /** 内存中的用户数据缓存（静态，跨实例共享） */
    private static UserData sUserData;

    public enum GachaCoin {
        BRONZE("bronze_coin"),
        GOLD("gold_coin"),
        PLATINUM("platinum_coin"),
        None("none");

        private String value;

        private GachaCoin(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    /**
     * 保存用户数据到本地存储
     * 使用 UserDataManager 作为唯一数据源
     */
    private void saveUserData() {
        if (sUserData != null) {
            mUserDataManager.saveUserData(sUserData);
        }
    }

    /**
     * 从本地存储加载用户数据
     * 优先从 UserDataManager（fileDir/userData.dat）加载，
     * 若不存在则尝试从旧版文件（cacheDir/localUserData.dat）迁移，
     * 若均不存在则创建新的本地用户数据。
     */
    private void loadUserData() {
        // 1. 尝试从 UserDataManager 加载
        sUserData = mUserDataManager.loadUserData();

        // 2. 迁移：如果 userData.dat 不存在，尝试从旧版 localUserData.dat 读取
        if (sUserData == null) {
            File legacyFile = new File(mContext.getCacheDir(), LEGACY_DATA_FILE);
            if (legacyFile.exists()) {
                try {
                    FileInputStream in = new FileInputStream(legacyFile);
                    sUserData = UserData.restore(in);
                    Logger.d("UserData", "已从旧版数据文件迁移用户数据");
                } catch (FileNotFoundException e) {
                    Logger.e("UserData", "迁移失败：旧版文件未找到");
                }
            }
        }

        // 3. 如果仍然为空，创建新的本地用户数据
        if (sUserData == null) {
            sUserData = UserData.createLocal();
        } else {
            // 4. 检查每日登录奖励
            applyDailyBonus();
        }

        // 5. 保存到统一存储
        saveUserData();
    }

    /**
     * 检查并应用每日登录奖励
     * 如果不是同一天登录，赠送铜币（+1）和金币（+1，替代付费功能）
     */
    private void applyDailyBonus() {
        Date date = new Date();
        Date loginDate = new Date(sUserData.getLastLoginTime());
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(loginDate);

        boolean isSameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
        if (!isSameDay) {
            sUserData.setBonus(true);
            // 每日登录赠送铜币（已有逻辑）
            sUserData.setBronzeCoin(sUserData.getBronzeCoin() + 1);
            // 每日登录赠送金币（新增，替代付费功能）
            sUserData.setGoldCoin(CoinController.roundingNumber(sUserData.getGoldCoin() + 1));
            // 更新最后登录时间，避免重复发放奖励
            sUserData.setLastLoginTime(System.currentTimeMillis());
        } else {
            sUserData.setBonus(false);
        }
    }

    public MoeyuAPIClient(Context context) {
        this.mContext = context;
        this.mUserDataManager = new UserDataManager(context);
        loadUserData();
    }

    public UserData userSignUp() throws MoeyuAPIException {
        return sUserData;
    }

    public UserData userData(String userId) throws MoeyuAPIException {
        return sUserData;
    }

    /**
     * 本地扭蛋逻辑
     * 扣除对应货币，随机获取物品，计算经验值并更新等级
     *
     * @param userId 用户ID
     * @param use    使用的货币类型
     * @return 扭蛋结果
     */
    public GachaResult userGatya(String userId, GachaCoin use) throws MoeyuAPIException {
        ArrayList<Integer> noHolds = new ArrayList<>();
        for (int i = 0; i < UserData.MAX_ITEM_COUNT; i++) {
            int id = i + 1;
            if (!sUserData.hasItem(id)) {
                noHolds.add(id);
            }
        }
        if (noHolds.isEmpty()) {
            noHolds.addAll(sUserData.getItems());
        }

        int rate = 0;
        String coinType = CoinController.BRONZE;
        switch (use) {
            case BRONZE:
                rate = RATE_BRONZE;
                coinType = CoinController.BRONZE;
                sUserData.setBronzeCoin(sUserData.getBronzeCoin() - 1);
                break;
            case GOLD:
                rate = RATE_GOLD;
                coinType = CoinController.GOLD;
                sUserData.setGoldCoin(sUserData.getGoldCoin() - 1);
                break;
            case PLATINUM:
                rate = RATE_PLATINUM;
                coinType = CoinController.PLATINUM;
                sUserData.setPlatinumCoin(sUserData.getPlatinumCoin() - 1);
                break;
            case None:
                break;
        }

        GachaResult result = getGachaResult(rate, noHolds);

        // 计算经验值并更新等级
        int gainedExp = mLovePoint.getPoint(coinType);
        sUserData.setExp(sUserData.getExp() + gainedExp);
        int newLevel = mLovePoint.currentLevel(sUserData.getExp());
        sUserData.setLevel(newLevel);

        // 更新 GachaResult 中的 UserData（含新的 exp 和 level）
        result.setUserData(sUserData);

        saveUserData();
        return result;
    }

    @NonNull
    private static GachaResult getGachaResult(int rate, ArrayList<Integer> noHolds) {
        Random random = new Random();
        int randomResult = Math.abs(random.nextInt() % GACHA_RANDOM_RANGE);
        int itemId = 0;
        if (rate > randomResult || sUserData.getItems().isEmpty()) {
            randomResult = Math.abs(random.nextInt() % noHolds.size());
            itemId = noHolds.get(randomResult);
            sUserData.addItem(itemId);
        } else {
            randomResult = Math.abs(random.nextInt() % sUserData.getItems().size());
            itemId = sUserData.getItems().get(randomResult);
        }
        GachaResult result = new GachaResult();
        result.setUserData(sUserData);
        result.setItemId(itemId);
        return result;
    }

    /**
     * 本地化计费：根据产品ID直接赠送货币
     *
     * @param productId 产品ID（如 "gold_coin_3", "gold_coin_10", "platinum_coin_1"）
     * @return 更新后的用户数据
     */
    public UserData userBilling(String productId) {
        if (sUserData == null) return null;
        switch (productId) {
            case PRODUCT_GOLD_COIN_3:
                sUserData.setGoldCoin(CoinController.roundingNumber(sUserData.getGoldCoin() + 3));
                break;
            case PRODUCT_GOLD_COIN_10:
                sUserData.setGoldCoin(CoinController.roundingNumber(sUserData.getGoldCoin() + 10));
                break;
            case PRODUCT_PLATINUM_COIN_1:
                sUserData.setPlatinumCoin(CoinController.roundingNumber(sUserData.getPlatinumCoin() + 1));
                break;
        }
        saveUserData();
        return sUserData;
    }
}
