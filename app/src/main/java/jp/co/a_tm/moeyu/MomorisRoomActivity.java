package jp.co.a_tm.moeyu;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import jp.co.a_tm.moeyu.util.Logger;

public class MomorisRoomActivity extends BaseActivity {
    private NoteTableController controller;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("MomorisRoomAcitivity");
        setContentView(R.layout.activity_room);
        this.controller = new NoteTableController(this);
        changeImage(3, R.id.room_right_2, "room_right01_02");
        changeImage(6, R.id.room_right_3, "room_right01_03");
        changeImage(18, R.id.room_right_4, "room_right01_04");
        changeImage(10, R.id.room_right_5, "room_right01_05");
        changeImage(14, R.id.room_right_6, "room_right01_06");
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("ももりの部屋");
    }

    private void changeImage(int diaryId, int resId, String fileName) {
        ImageView imageView = (ImageView) findViewById(resId);
        if (this.controller.isOpened(diaryId)) {
            imageView.setImageResource(getResources().getIdentifier(fileName, "drawable", getPackageName()));
        }
    }

    public void toNoteClick(View view) {
        toDiary();
    }

    public void toKonyokuClick(View view) {
        toBath();
    }

    public void toGatyaClick(View view) {
        toGacha();
    }

    public void toCollecClick(View view) {
        toCollection();
    }

    public void toTitleClick(View view) {
        finish();
    }
}
