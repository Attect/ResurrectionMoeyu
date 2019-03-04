package jp.co.a_tm.moeyu.model;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
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
    private List<Integer> items = new ArrayList();
    private int level;
    private int platinumCoin;
    private String state;
    private String userId;

    public static UserData fromJson(JSONObject json) throws JSONException {
        UserData user = new UserData();
        user.setUserId(String.valueOf(json.get("user_id")));
        user.setBronzeCoin(json.getInt("bronze_coin"));
        user.setGoldCoin(json.getInt("gold_coin"));
        user.setPlatinumCoin(json.getInt("platinum_coin"));
        JSONArray jsonArray = json.getJSONArray("items");
        for (int i = 0; i < jsonArray.length(); i++) {
            user.addItem(jsonArray.getInt(i));
        }
        user.setExp(json.getInt("exp"));
        user.setLevel(json.getInt("level"));
        if (json.has("state")) {
            user.setState(json.getString("state"));
        }
        if (json.has("bonus")) {
            user.setBonus(json.getBoolean("bonus"));
        }
        return user;
    }

    public static UserData restore(InputStream is) {
        UserData userData = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(is);
            userData = (UserData) ois.readObject();
            ois.close();
            return userData;
        } catch (Exception e) {
            Log.e(TAG, "Can't restore UserData object");
            return userData;
        }
    }

    public boolean store(OutputStream os) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(os);
            oos.writeObject(this);
            oos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Can't store UserData object");
            return false;
        }
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
}
