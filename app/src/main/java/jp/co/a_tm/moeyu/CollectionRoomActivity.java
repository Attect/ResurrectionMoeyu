package jp.co.a_tm.moeyu;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

public class CollectionRoomActivity extends BaseActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectionroom);
        findViewById(R.id.layout_collecroom).setPadding(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        findViewById(R.id.layout_collecroom).setBackgroundColor(Color.BLACK);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("コレクション");
    }

    public void toItemCollectionClick(View view) {
        toItemCollection();
    }

    public void toVoiceCollectionClick(View view) {
        toVoiceCollection();
    }

    public void toMomorisRoomClick(View view) {
        toRoom();
    }

    public void toTitleClick(View view) {
        finish();
    }
}
