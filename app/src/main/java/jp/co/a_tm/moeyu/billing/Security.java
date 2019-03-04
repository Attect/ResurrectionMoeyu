package jp.co.a_tm.moeyu.billing;

import android.text.TextUtils;
import android.util.Log;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import jp.co.a_tm.moeyu.billing.Consts.PurchaseState;
import jp.co.a_tm.moeyu.billing.util.Base64;
import jp.co.a_tm.moeyu.billing.util.Base64DecoderException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Security {
    private static final String KEY_FACTORY_ALGORITHM = "RSA";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final String TAG = "Security";
    private static HashSet<Long> sKnownNonces = new HashSet();

    public static class VerifiedPurchase {
        public String developerPayload;
        public String notificationId;
        public String orderId;
        public String productId;
        public PurchaseState purchaseState;
        public long purchaseTime;

        public VerifiedPurchase(PurchaseState purchaseState, String notificationId, String productId, String orderId, long purchaseTime, String developerPayload) {
            this.purchaseState = purchaseState;
            this.notificationId = notificationId;
            this.productId = productId;
            this.orderId = orderId;
            this.purchaseTime = purchaseTime;
            this.developerPayload = developerPayload;
        }
    }

    public static long generateNonce() {
        long nonce = RANDOM.nextLong();
        sKnownNonces.add(Long.valueOf(nonce));
        return nonce;
    }

    public static void removeNonce(long nonce) {
        sKnownNonces.remove(Long.valueOf(nonce));
    }

    public static boolean isNonceKnown(long nonce) {
        return sKnownNonces.contains(Long.valueOf(nonce));
    }

    public static ArrayList<VerifiedPurchase> verifyPurchase(String signedData, String signature) {
        if (signedData == null) {
            Log.e(TAG, "data is null");
            return null;
        }
        Log.i(TAG, "signedData: " + signedData);
        boolean verified = false;
        if (!TextUtils.isEmpty(signature)) {
            verified = verify(generatePublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtvGLPFosZgkIMctfNBD5f35abJ3bko3JaUVEPdLWswjVle2LeZPFk2hNqsZKuZRAjbK+nwzrgLzq/EbSFGCzCX9IU3/G8WuAHfRes01V0I986QTtCrLGHyd5p1jUuL3rYVMIkHuaxI2onkxVkrDOkv1oqy1x5KgN04FCBrRFjVBYdlxXSmyXwLwy8Y4KOdOSxnjYHeUlL3KMTF5Wgic5nbrxewbbVtji9ki9P2ircrs9YFUEz7/L7gL/djSx7mcZL2mkVSbUunbH0QqfuPbZL9z1cEkFF74xeOfCS7HC52Wk3e7IsFOhEnfZoWidTwfC7l8PGbH+wh4U4jzv67PwDQIDAQAB"), signedData, signature);
            if (!verified) {
                Log.w(TAG, "signature does not match data.");
                return null;
            }
        }
        int numTransactions = 0;
        try {
            JSONObject jObject = new JSONObject(signedData);
            long nonce = jObject.optLong("nonce");
            JSONArray jTransactionsArray = jObject.optJSONArray("orders");
            if (jTransactionsArray != null) {
                numTransactions = jTransactionsArray.length();
            }
            if (isNonceKnown(nonce)) {
                ArrayList<VerifiedPurchase> purchases = new ArrayList();
                int i = 0;
                while (i < numTransactions) {
                    try {
                        JSONObject jElement = jTransactionsArray.getJSONObject(i);
                        PurchaseState purchaseState = PurchaseState.valueOf(jElement.getInt("purchaseState"));
                        String productId = jElement.getString("productId");
                        long purchaseTime = jElement.getLong("purchaseTime");
                        String orderId = jElement.optString("orderId", "");
                        String notifyId = null;
                        if (jElement.has("notificationId")) {
                            notifyId = jElement.getString("notificationId");
                        }
                        String developerPayload = jElement.optString("developerPayload", null);
                        if (purchaseState != PurchaseState.PURCHASED || verified) {
                            purchases.add(new VerifiedPurchase(purchaseState, notifyId, productId, orderId, purchaseTime, developerPayload));
                        }
                        i++;
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON exception: ", e);
                        return null;
                    }
                }
                removeNonce(nonce);
                return purchases;
            }
            Log.w(TAG, "Nonce not found: " + nonce);
            return null;
        } catch (JSONException e2) {
            return null;
        }
    }

    public static PublicKey generatePublicKey(String encodedPublicKey) {
        try {
            return KeyFactory.getInstance(KEY_FACTORY_ALGORITHM).generatePublic(new X509EncodedKeySpec(Base64.decode(encodedPublicKey)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeySpecException e2) {
            Log.e(TAG, "Invalid key specification.");
            throw new IllegalArgumentException(e2);
        } catch (Base64DecoderException e3) {
            Log.e(TAG, "Base64 decoding failed.");
            throw new IllegalArgumentException(e3);
        }
    }

    public static boolean verify(PublicKey publicKey, String signedData, String signature) {
        Log.i(TAG, "signature: " + signature);
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(signedData.getBytes());
            if (sig.verify(Base64.decode(signature))) {
                return true;
            }
            Log.e(TAG, "Signature verification failed.");
            return false;
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "NoSuchAlgorithmException.");
            return false;
        } catch (InvalidKeyException e2) {
            Log.e(TAG, "Invalid key specification.");
            return false;
        } catch (SignatureException e3) {
            Log.e(TAG, "Signature exception.");
            return false;
        } catch (Base64DecoderException e4) {
            Log.e(TAG, "Base64 decoding failed.");
            return false;
        }
    }
}
