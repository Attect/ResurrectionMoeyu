package jp.co.a_tm.moeyu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
//import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import jp.co.a_tm.moeyu.model.EventData;

public class BaseActivity extends FragmentActivity {
    protected static final String EXTRA_NEXT_ACTIVITY = "extra_next_activity";
    protected static final int NEXT_ACTIVITY_BATH = 2;
    protected static final int NEXT_ACTIVITY_COLLECTION = 6;
    protected static final int NEXT_ACTIVITY_DIARY = 10;
    protected static final int NEXT_ACTIVITY_GACHA = 1;
    protected static final int NEXT_ACTIVITY_GACHA_RESULT = 5;
    protected static final int NEXT_ACTIVITY_ITEM_COLLECTION = 7;
    protected static final int NEXT_ACTIVITY_PREFERENCE = 3;
    protected static final int NEXT_ACTIVITY_PREFERENCE_FROM_BATH = 4;
    protected static final int NEXT_ACTIVITY_ROOM = 9;
    protected static final int NEXT_ACTIVITY_TITLE = 0;
    protected static final int NEXT_ACTIVITY_VOICE_COLLECTION = 8;
    protected static final int NEXT_EXIT = -1;
//    protected GoogleAnalyticsTracker mTracker;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.mTracker = GoogleAnalyticsTracker.getInstance();
//        this.mTracker.startNewSession("UA-30080453-1", 30, this);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
//        this.mTracker.stopSession();
//        cleanupView(findViewById(16908290));
    }

    public static final void cleanupView(View view) {
        if (view instanceof ImageButton) {
            ((ImageButton) view).setImageDrawable(null);
        } else if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(null);
        }
        view.setBackgroundDrawable(null);
        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            int size = vg.getChildCount();
            for (int i = 0; i < size; i++) {
                cleanupView(vg.getChildAt(i));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        release();
    }

    /* access modifiers changed from: protected */
    public void release() {
    }

    /* access modifiers changed from: protected */
    public void exit() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, -1);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toTitle() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 0);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toGacha() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 1);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toBath() {
        toBath(null);
    }

    /* access modifiers changed from: protected */
    public void toBath(String scene, int itemId) {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 2);
        data.putExtra(getString(R.string.intent_scene), scene);
        data.putExtra(getString(R.string.intent_item), itemId);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toBath(EventData eventData) {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 2);
        if (eventData != null) {
            data.putExtra(getString(R.string.intent_event), eventData);
        }
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toPreference() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 3);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toPreferenceFromBath() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 4);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 6);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toItemCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 7);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toVoiceCollection() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 8);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toRoom() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, 9);
        setResult(-1, data);
        finish();
    }

    /* access modifiers changed from: protected */
    public void toDiary() {
        Intent data = new Intent();
        data.putExtra(EXTRA_NEXT_ACTIVITY, NEXT_ACTIVITY_DIARY);
        setResult(-1, data);
        finish();
    }
}
