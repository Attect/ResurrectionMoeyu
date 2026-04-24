package jp.co.a_tm.moeyu;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import jp.co.a_tm.moeyu.util.Logger;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.UserDataManager;

public class TweetDialog extends Dialog {
    PreferencesHelper mPreferencesHelper;

    public TweetDialog(final Context context) {
        super(context, R.style.clear_dialog);
        this.mPreferencesHelper = new PreferencesHelper(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_tweet);
        setCanceledOnTouchOutside(true);
        ImageButton toBackButton = findViewById(R.id.imgbutton_tweet_dialog_top_to_back);
        ImageButton toTweetButton = findViewById(R.id.imgbutton_tweet_dialog_top_to_tweet);
        ImageButton checkBoxButton = findViewById(R.id.imgbutton_tweet_dialog_center_center_check_box);
        ImageButton checkMarkButton = findViewById(R.id.imgbutton_tweet_dialog_center_center_check_mark);
        if (this.mPreferencesHelper.isTwitterCheck()) {
            checkMarkButton.setVisibility(View.VISIBLE);
        } else {
            checkMarkButton.setVisibility(View.INVISIBLE);
        }
        toBackButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TweetDialog.this.dismiss();
            }
        });
        toTweetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TweetDialog.this.start(context);
                TweetDialog.this.dismiss();
            }
        });
        checkBoxButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TweetDialog.this.mPreferencesHelper.setTwitterCheck(true);
                ((ImageButton) TweetDialog.this.findViewById(R.id.imgbutton_tweet_dialog_center_center_check_mark)).setVisibility(View.VISIBLE);
            }
        });
        checkMarkButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TweetDialog.this.mPreferencesHelper.setTwitterCheck(false);
                view.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void show(Context context) {
        if (this.mPreferencesHelper.isInitTweet() || !this.mPreferencesHelper.isTwitterCheck()) {
            this.mPreferencesHelper.setInitTweet(false);
            super.show();
            return;
        }
        start(context);
    }

    public void dismiss() {
        super.dismiss();
    }

    public void start(Context context) {
        UserData userData = new UserDataManager(context).loadUserData();
        ItemTableController controller = new ItemTableController(context);
        String strTweet = "";
        try {
            strTweet = context.getString(R.string.tweet_https) + context.getString(R.string.tweet_text_left) + context.getResources().getStringArray(R.array.tweet_level_array)[userData.getLevel() - 1] + context.getString(R.string.tweet_text_center) + controller.getOpenedPercent() + context.getString(R.string.tweet_text_right) + "+" + URLEncoder.encode(context.getString(R.string.tweet_hash), "UTF-8") + "&url=" + context.getString(R.string.tweet_url);
        } catch (UnsupportedEncodingException e) {
            Logger.e("TweetDialog", "URL编码失败", e);
        }
        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(strTweet)));
    }
}
