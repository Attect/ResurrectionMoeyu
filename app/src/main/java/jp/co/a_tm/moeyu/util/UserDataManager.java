package jp.co.a_tm.moeyu.util;

import android.content.Context;
import java.io.FileNotFoundException;
import jp.co.a_tm.moeyu.model.UserData;

public class UserDataManager {
    private static final String USER_DATA_FILE = "userData.dat";
    private Context mContext;

    public UserDataManager(Context context) {
        this.mContext = context;
    }

    public UserData loadUserData() {
        UserData userData = null;
        try {
            return UserData.restore(this.mContext.openFileInput(USER_DATA_FILE));
        } catch (FileNotFoundException e) {
            return userData;
        }
    }

    public boolean saveUserData(UserData userData) {
        boolean z = false;
        if (userData == null) {
            return z;
        }
        try {
            return userData.store(this.mContext.openFileOutput(USER_DATA_FILE, 0));
        } catch (FileNotFoundException e) {
            return z;
        }
    }

    public boolean isSavedUserData() {
        return this.mContext.getFileStreamPath(USER_DATA_FILE).exists();
    }
}
