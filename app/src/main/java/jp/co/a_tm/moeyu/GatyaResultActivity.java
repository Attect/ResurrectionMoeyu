package jp.co.a_tm.moeyu;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import jp.co.a_tm.moeyu.api.model.GachaResult;
import jp.co.a_tm.moeyu.model.EventData;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.Logger;

public class GatyaResultActivity extends BaseActivity {
    public static final String EXTRA_GACHA_RESULT = "extra_gacha_result";
    public static final String EXTRA_PRE_USER_DATA = "extra_pre_user_data";
    private EventData mEventData;
    private PreferencesHelper mHelper;
    private UserData mPreUserData;
    private RecommendStatus mRecommendGachaStatus = RecommendStatus.None;
    private RecommendStatus mRecommendLevelupStatus = RecommendStatus.None;
    private UserData mUserData;

    private enum RecommendStatus {
        None,
        Item1,
        Item2
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gatya_result);
        findViewById(R.id.layout_gatya_result).setPadding(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        findViewById(R.id.layout_gatya_result).setBackgroundColor(Color.BLACK);

        GachaResult result = (GachaResult) getIntent().getSerializableExtra(EXTRA_GACHA_RESULT);
        this.mUserData = result.getUserData();
        this.mPreUserData = (UserData) getIntent().getSerializableExtra(EXTRA_PRE_USER_DATA);
        this.mHelper = new PreferencesHelper(getApplicationContext());
        setItemImage(result.getItemId());
        setBackImage(this.mPreUserData.hasItem(result.getItemId()));
        showEventViews(this.mPreUserData.getLevel(), this.mPreUserData.isItemComplete());
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("ガチャ結果");
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setMeterImage();
    }

    public void onRecommendLevelupClick(View view) {
        ImageView recommendView = (ImageView) view;
        recommendView.setImageDrawable(null);
        switch (this.mRecommendLevelupStatus) {
            case Item1:
                this.mRecommendLevelupStatus = RecommendStatus.Item2;
                recommendView.setImageResource(R.drawable.recommend_plate06b);
                return;
            case Item2:
                this.mRecommendLevelupStatus = RecommendStatus.None;
                recommendView.setVisibility(View.INVISIBLE);
                return;
            default:
                return;
        }
    }

    public void onRecommendGachaClick(View view) {
        ImageView recommendView = (ImageView) view;
        recommendView.setImageDrawable(null);
        switch (this.mRecommendGachaStatus) {
            case Item1:
                this.mRecommendGachaStatus = RecommendStatus.Item2;
                recommendView.setImageResource(R.drawable.recommend_plate04bb);
                return;
            case Item2:
                this.mRecommendGachaStatus = RecommendStatus.None;
                recommendView.setVisibility(View.INVISIBLE);
                return;
            default:
                return;
        }
    }

    public void onItemCompleteClick(View view) {
        view.setVisibility(View.INVISIBLE);
        toBath(this.mEventData);
    }

    public void onLevelupClick(View view) {
        view.setVisibility(View.INVISIBLE);
        toBath(this.mEventData);
    }

    private void setBackImage(boolean opened) {
        String fileName = "result_back_";
        ImageView newImage = (ImageView) findViewById(R.id.img_gatya_result_body_item_new);
        if (opened) {
            newImage.setVisibility(View.INVISIBLE);
        } else {
            fileName = fileName + "new_";
            newImage.setVisibility(View.VISIBLE);
        }
        ImageView[] image = new ImageView[]{(ImageView) findViewById(R.id.img_gatya_result_header), (ImageView) findViewById(R.id.img_gatya_result_body_top_back), (ImageView) findViewById(R.id.img_gatya_result_body_center_back), (ImageView) findViewById(R.id.img_gatya_result_body_meter_left), (ImageView) findViewById(R.id.img_gatya_result_body_meter_right), (ImageView) findViewById(R.id.img_gatya_result_body_under_back)};
        for (int i = 0; i < image.length; i++) {
            ImageView imageResource = image[i];
            imageResource.setImageResource(getResources().getIdentifier(fileName + i, "drawable", getPackageName()));
        }
    }

    private void setItemImage(int num) {
        ImageView picture = (ImageView) findViewById(R.id.img_gatya_result_body_item_picture);
        ((ImageView) findViewById(R.id.img_gatya_result_body_item_name)).setImageResource(getResources().getIdentifier("itemname_bar_" + num, "drawable", "jp.co.a_tm.moeyu"));
        picture.setImageResource(getResources().getIdentifier("item" + num + "_2x", "drawable", "jp.co.a_tm.moeyu"));
    }

    private void setMeterImage() {
        if (!this.mPreUserData.isMaxLevel()) {
            float start;
            float end;
            int[] exps = getResources().getIntArray(R.array.require_exp);
            float lowerExp;
            if (this.mUserData.getLevel() > this.mPreUserData.getLevel()) {
                lowerExp = (float) exps[this.mPreUserData.getLevel()];
                start = (((float) this.mPreUserData.getExp()) - lowerExp) / (((float) exps[this.mUserData.getLevel()]) - lowerExp);
                end = 1.0f;
            } else {
                lowerExp = (float) exps[this.mUserData.getLevel()];
                float range = ((float) exps[this.mUserData.getLevel() + 1]) - lowerExp;
                start = (((float) this.mPreUserData.getExp()) - lowerExp) / range;
                end = (((float) this.mUserData.getExp()) - lowerExp) / range;
            }
            View bar = findViewById(R.id.img_gatya_result_body_meter_center_bar);
            float startX = (float) (bar.getLeft() - bar.getWidth());
            float fromX = startX + (((float) bar.getWidth()) * start);
            float toX = startX + (((float) bar.getWidth()) * end);
            float fromY = (float) bar.getTop();
            Logger.d(String.format("%f, %f, %f, %f", new Object[]{Float.valueOf(fromX), Float.valueOf(toX), Float.valueOf(fromY), Float.valueOf(fromY)}));
            TranslateAnimation animation = new TranslateAnimation(fromX, toX, fromY, fromY);
            animation.setDuration((long) ((toX - fromX) * 10.0f));
            animation.setFillAfter(true);
            bar.startAnimation(animation);
        }
    }

    private void showEventViews(int loveLevelBefore, boolean itemCompleteBefore) {
        if (this.mHelper.isInitGatyaResult()) {
            findViewById(R.id.recommend_first_gacha_view).setVisibility(View.VISIBLE);
            this.mRecommendGachaStatus = RecommendStatus.Item1;
            this.mHelper.setInitGatyaResult(false);
        }
        if (this.mUserData.getLevel() > loveLevelBefore && this.mHelper.isInitLevelUp()) {
            findViewById(R.id.recommend_first_levelup_view).setVisibility(View.VISIBLE);
            this.mRecommendLevelupStatus = RecommendStatus.Item1;
            this.mHelper.setInitLevelUp(false);
        }
        this.mEventData = new EventController(getApplicationContext()).pop();
        if (this.mEventData != null) {
            switch (this.mEventData.getType()) {
                case Complete:
                    findViewById(R.id.item_complete_view).setVisibility(View.VISIBLE);
                    return;
                default:
                    findViewById(R.id.levelup_view).setVisibility(View.VISIBLE);
                    return;
            }
        }
    }

    public void toKonyokuClick(View view) {
        toBath();
    }

    public void toGatyaClick(View view) {
        toGacha();
    }

    public void toItemClick(View view) {
        toItemCollection();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Logger.d(getClass().getSimpleName() + " onDestroy()");
    }

    /* access modifiers changed from: protected */
    public void release() {
        findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        for (int id : new int[]{R.id.img_gatya_result_body_item_name, R.id.img_gatya_result_body_item_picture, R.id.img_gatya_result_header, R.id.img_gatya_result_body_top_back, R.id.img_gatya_result_body_center_back, R.id.img_gatya_result_body_meter_left, R.id.img_gatya_result_body_meter_right, R.id.img_gatya_result_body_under_back}) {
            ((ImageView) findViewById(id)).setImageDrawable(null);
        }
    }
}
