package jp.co.a_tm.moeyu.api.model;

import android.util.Log;
import java.io.Serializable;
import jp.co.a_tm.moeyu.model.UserData;
import org.json.JSONObject;

public class GachaResult implements Serializable {
    private static final String TAG = GachaResult.class.getSimpleName();
    private static final long serialVersionUID = -720621375596638033L;
    private int itemId;
    private UserData userData;

    public static GachaResult fromJson(JSONObject json) {
        try {
            GachaResult result = new GachaResult();
            result.setUserData(UserData.fromJson(json.getJSONObject("user_data")));
            result.setItemId(json.getJSONObject("gacha").getInt("acquisition"));
            return result;
        } catch (Exception e) {
            Log.e(TAG, "JSON parse error: " + e.getMessage());
            return null;
        }
    }

    public UserData getUserData() {
        return this.userData;
    }

    public void setUserData(UserData userData) {
        this.userData = userData;
    }

    public int getItemId() {
        return this.itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
