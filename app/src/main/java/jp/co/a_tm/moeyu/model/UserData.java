package jp.co.a_tm.moeyu.model;

import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserData implements Serializable {
    public static final int MAX_ITEM_COUNT = 25;
    private static final int MAX_LEVEL = 6;
    private static final String TAG = UserData.class.getSimpleName();
    private static final long serialVersionUID = 5964271611027782194L;
    private boolean bonus;
    private int bronzeCoin;
    private int exp;
    private int goldCoin;
    private List<Integer> items = new ArrayList<>();
    private int level;
    private int platinumCoin;
    private String state;
    private String userId;
    private long lastLoginTime;

    public static UserData createLocal(){
        UserData user = new UserData();
        user.bonus = true;
        user.bronzeCoin = 10;
        user.exp = 0;
        user.goldCoin = 0;
        user.level = 1;
        user.platinumCoin = 0;
        user.userId = "local";
        user.lastLoginTime= System.currentTimeMillis();
        return user;
    }

    /**
     * 从输入流恢复用户数据
     * 优先尝试 JSON 格式（新版），失败则回退到 Java Serialization（旧版兼容）
     */
    public static UserData restore(InputStream is) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            String jsonStr = baos.toString(StandardCharsets.UTF_8.name());
            // 如果内容以 { 开头，尝试 JSON 解析
            if (jsonStr.trim().startsWith("{")) {
                return fromJson(new JSONObject(jsonStr));
            }
            // 回退到旧版 Java Serialization
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            UserData userData = (UserData) ois.readObject();
            ois.close();
            return userData;
        } catch (Exception e) {
            Log.e(TAG, "Can't restore UserData object", e);
            return null;
        }
    }

    /**
     * 将用户数据存储为 JSON 格式到输出流
     */
    public boolean store(OutputStream os) {
        try {
            String jsonStr = toJson().toString();
            os.write(jsonStr.getBytes(StandardCharsets.UTF_8));
            os.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Can't store UserData object", e);
            return false;
        }
    }

    /**
     * 序列化为 JSON 对象
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("user_id", this.userId);
            json.put("bronze_coin", this.bronzeCoin);
            json.put("gold_coin", this.goldCoin);
            json.put("platinum_coin", this.platinumCoin);
            json.put("exp", this.exp);
            json.put("level", this.level);
            json.put("state", this.state);
            json.put("bonus", this.bonus);
            json.put("last_login_time", this.lastLoginTime);
            JSONArray itemsArray = new JSONArray();
            for (Integer item : this.items) {
                itemsArray.put(item);
            }
            json.put("items", itemsArray);
        } catch (JSONException e) {
            Log.e(TAG, "toJson failed", e);
        }
        return json;
    }

    /**
     * 从 JSON 对象反序列化
     */
    public static UserData fromJson(JSONObject json) throws JSONException {
        UserData user = new UserData();
        user.setUserId(json.optString("user_id", "local"));
        user.setBronzeCoin(json.optInt("bronze_coin", 0));
        user.setGoldCoin(json.optInt("gold_coin", 0));
        user.setPlatinumCoin(json.optInt("platinum_coin", 0));
        user.setExp(json.optInt("exp", 0));
        user.setLevel(json.optInt("level", 1));
        user.setState(json.optString("state", null));
        user.setBonus(json.optBoolean("bonus", false));
        user.setLastLoginTime(json.optLong("last_login_time", System.currentTimeMillis()));
        JSONArray itemsArray = json.optJSONArray("items");
        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.length(); i++) {
                user.addItem(itemsArray.getInt(i));
            }
        }
        return user;
    }

    public void addItem(int itemId) {
        this.items.add(Integer.valueOf(itemId));
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getBronzeCoin() {
        return this.bronzeCoin;
    }

    public void setBronzeCoin(int bronzeCoin) {
        this.bronzeCoin = bronzeCoin;
    }

    public int getGoldCoin() {
        return this.goldCoin;
    }

    public void setGoldCoin(int goldCoin) {
        this.goldCoin = goldCoin;
    }

    public int getPlatinumCoin() {
        return this.platinumCoin;
    }

    public void setPlatinumCoin(int platinumCoin) {
        this.platinumCoin = platinumCoin;
    }

    public List<Integer> getItems() {
        return this.items;
    }

    public void setItems(List<Integer> items) {
        this.items = items;
    }

    public boolean isItemGet(int num) {
        return this.items.contains(Integer.valueOf(num));
    }

    public boolean isItemComplete() {
        if (this.items == null || this.items.size() != 25) {
            return false;
        }
        return true;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean hasBonus() {
        return this.bonus;
    }

    public void setBonus(boolean bonus) {
        this.bonus = bonus;
    }

    public boolean hasItem(int itemId) {
        if (this.items != null) {
            return this.items.contains(Integer.valueOf(itemId));
        }
        return false;
    }

    public boolean isMaxLevel() {
        return this.level == 6;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
