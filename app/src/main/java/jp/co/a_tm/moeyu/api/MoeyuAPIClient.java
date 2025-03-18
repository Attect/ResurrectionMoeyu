package jp.co.a_tm.moeyu.api;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import jp.co.a_tm.moeyu.api.model.GachaResult;
import jp.co.a_tm.moeyu.live2d.motion.LAppAnimation;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Config;
import jp.co.a_tm.moeyu.util.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class MoeyuAPIClient {
    private static final String APP_ID = "app_id";
    private static final String APP_VERSION = "app_version";
    private static String BASE_URL = "http://api.moeapk.com/third_party/moeyu/";
    private static final String BASE_URL_DEV = "http://api.moeapk.com/third_party/moeyu/";
    private static final String BASE_URL_PROD = "http://api.moeapk.com/third_party/moeyu/";
    private static final String BASE_URL_STAGING = "http://api.moeapk.com/third_party/moeyu/";
    private static final String INAPP_SIGNATURE = "inapp_signature";
    private static final String INAPP_SIGNED_DATA = "inapp_signed_data";
    private static final String NONCE = "nonce";
    private static final String SIGNATURE = "signature";
    private static final String TIMESTAMP = "timestamp";
    private static final String USE = "use";
    private static final String USER_ID = "user_id";
    private static final String appId = "MOEYU_001";
    private static final String appVersion = "1";
    private Config mConfig;

    private static File userDataFile;
    private static UserData userData;

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



    private void saveUserData(){
        try{
            FileOutputStream out = new FileOutputStream(userDataFile);
            userData.store(out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            Logger.e("UserData","保存失败：文件没有找到");
        } catch (IOException e) {
            Logger.e("UserData","保存失败，IO错误");
        }
    }

    private void readUserData(){
        try{
            FileInputStream in = new FileInputStream(userDataFile);
            userData = UserData.restore(in);
            if (userData == null){
                userData = UserData.createLocal();
            }else{
                Date date = new Date();
                Date loginDate = new Date(userData.getLastLoginTime());
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(date);

                Calendar cal2 = Calendar.getInstance();
                cal2.setTime(loginDate);

                boolean isSameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                        && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                        && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
                if (!isSameDay){
                    userData.setBonus(true);
                    userData.setBronzeCoin(userData.getBronzeCoin()+1);
                }else{
                    userData.setBonus(false);
                }
            }
            saveUserData();
        } catch (FileNotFoundException e) {
            Logger.e("UserData","读取失败：文件没有找到");
        }
    }

    public MoeyuAPIClient(Context context) {
        this.mConfig = Config.getInstance(context);
        userDataFile = new File(context.getCacheDir(),"localUserData.dat");
        if (!userDataFile.exists()){
            userData = UserData.createLocal();
            saveUserData();
        }else{
            readUserData();
        }
    }

    public UserData userSignUp() throws MoeyuAPIException {
        return userData;
    }

    public UserData userData(String userId) throws MoeyuAPIException {
        return userData;
    }

    public GachaResult userGatya(String userId, GachaCoin use) throws MoeyuAPIException {
        ArrayList<Integer> noHolds = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            int id = i+1;
            if (!userData.hasItem(id)){
                noHolds.add(id);
            }
        }
        if (noHolds.isEmpty()){
            noHolds.addAll(userData.getItems());
        }
        int rate = 0;
        switch (use){
            case BRONZE:
                rate = 2;
                userData.setBronzeCoin(userData.getBronzeCoin()-1);
                break;
            case GOLD:
                rate = 3;
                userData.setGoldCoin(userData.getGoldCoin()-1);
                break;
            case PLATINUM:
                rate = 4;
                userData.setPlatinumCoin(userData.getPlatinumCoin()-1);
                break;
            case None:
                break;
        }
        GachaResult result = getGachaResult(rate, noHolds);
        saveUserData();
        return result;
    }

    @NonNull
    private static GachaResult getGachaResult(int rate, ArrayList<Integer> noHolds) {
        Random random = new Random();
        int randomResult = Math.abs(random.nextInt() % 10);
        int itemId = 0;
        if (rate > randomResult || userData.getItems().isEmpty()){
            randomResult = Math.abs(random.nextInt() % noHolds.size());
            itemId = noHolds.get(randomResult);
            userData.addItem(itemId);
        }else{
            randomResult = Math.abs(random.nextInt() % userData.getItems().size());
            itemId = userData.getItems().get(randomResult);
        }
        GachaResult result = new GachaResult();
        result.setUserData(userData);
        result.setItemId(itemId);
        return result;
    }

    public UserData userBilling(String signedData, String signature) throws MoeyuAPIException {
        int statusCode = -1;
        try {
            List<NameValuePair> params = createBaseParams();
            params.add(new BasicNameValuePair("inapp_signed_data", signedData));
            params.add(new BasicNameValuePair("inapp_signature", signature));
            HttpPost post = new HttpPost(BASE_URL + "user/billing");
            DefaultHttpClient client = new DefaultHttpClient();
            post.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse response = client.execute(post);
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == LAppAnimation.FLIP_START_FACE_Y) {
                String json = responseToString(response);
                Logger.d("userBilling response json = " + json);
                return UserData.fromJson(new JSONObject(json));
            }
            throw new MoeyuAPIException(statusCode);
        } catch (JSONException e) {
            throw new MoeyuAPIException(e, statusCode);
        } catch (ClientProtocolException e2) {
            throw new MoeyuAPIException(e2);
        } catch (IOException e22) {
            throw new MoeyuAPIException(e22);
        }
    }

    private void checkErrorCode(HttpResponse response) {
        Logger.d("checkErrorCode start");
        try {
            String strJSON = EntityUtils.toString(response.getEntity());
            Logger.d("debug: " + strJSON);
            switch (new Integer(String.valueOf(new JSONObject(strJSON).get("error_code"))).intValue()) {
                case 0:
                    Logger.d("タイムスタンプが５分以上ずれている");
                    return;
                case 1:
                    Logger.d("タイムスタンプと一時キーの組み合わせが使用済みだった");
                    return;
                default:
                    Logger.d("予期しないエラーコードを受け取った");
                    return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
    }

    private List<NameValuePair> createBaseParams() {
        ArrayList<NameValuePair> params = new ArrayList();
        String nonce = "abcdef";
        String time = String.valueOf(System.currentTimeMillis());
        params.add(new BasicNameValuePair(APP_ID, appId));
        params.add(new BasicNameValuePair(APP_VERSION, appVersion));
        params.add(new BasicNameValuePair(NONCE, "abcdef"));
        params.add(new BasicNameValuePair(TIMESTAMP, time));
        return params;
    }

    private String createSignature(List<NameValuePair> params) {
        StringBuffer sb = new StringBuffer();
        String baseString = createBaseString(params);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(baseString.getBytes());
            byte[] hash = md.digest();
            int cnt = hash.length;
            for (int i = 0; i < cnt; i++) {
                sb.append(Integer.toHexString((hash[i] >> 4) & 15));
                sb.append(Integer.toHexString(hash[i] & 15));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String createBaseString(List<NameValuePair> params) {
        String secret = "local_secret";
        Collections.sort(params, new Comparator<NameValuePair>() {
            public int compare(NameValuePair object1, NameValuePair object2) {
                return object1.getName().compareTo(object2.getName());
            }
        });
        StringBuffer sb = new StringBuffer();
        for (NameValuePair param : params) {
            sb.append(param.getValue());
        }
        return sb.toString() + "local_secret";
    }

    private String responseToString(HttpResponse response) throws UnsupportedEncodingException, IllegalStateException, IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        while (true) {
            String line = reader.readLine();
            if (line != null) {
                sb.append(line);
            } else {
                reader.close();
                return sb.toString();
            }
        }
    }
}
