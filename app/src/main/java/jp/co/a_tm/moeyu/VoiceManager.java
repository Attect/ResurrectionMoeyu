package jp.co.a_tm.moeyu;

import android.content.Context;

import androidx.core.view.accessibility.AccessibilityEventCompat;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VoiceManager {
    private Context mContext;
    private JSONObject mVoiceJson;

    public VoiceManager(Context context) throws IOException, JSONException {
        this.mContext = context;
        InputStream inputStream = this.mContext.getAssets().open("voice.json");
        StringBuilder stringBuilder = new StringBuilder();
        byte[] buffer = new byte[AccessibilityEventCompat.TYPE_TOUCH_EXPLORATION_GESTURE_END];
        while (true) {
            int length = inputStream.read(buffer);
            if (length >= 0) {
                stringBuilder.append(new String(buffer, 0, length));
            } else {
                inputStream.close();
                this.mVoiceJson = new JSONObject(stringBuilder.toString());
                return;
            }
        }
    }

    public FileDescriptor getVoiceFileDescripter(String voiceName) throws FileNotFoundException, IOException {
        return this.mContext.openFileInput(voiceName + ".ogg").getFD();
    }

    public String getVoiceName(Scene scene, Region region, int item, int level) throws JSONException {
        int random = new Random().nextInt(99) + 1;
        JSONObject itemJson = this.mVoiceJson.getJSONObject(scene.name()).getJSONObject(region.name()).getJSONObject(String.valueOf(item));
        JSONArray levelJsons = itemJson.has("0") ? itemJson.getJSONArray("0") : itemJson.getJSONArray(String.valueOf(level));
        for (int i = 0; i < levelJsons.length(); i++) {
            JSONObject entry = levelJsons.getJSONObject(i);
            int probability = entry.getInt("probability");
            if (random <= probability) {
                return entry.getString("voice");
            }
            random -= probability;
        }
        return null;
    }
}
