package jp.co.a_tm.moeyu;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;

import jp.co.a_tm.moeyu.api.MoeyuAPIException;
import jp.co.a_tm.moeyu.api.fragment.LoginFragment;
import jp.co.a_tm.moeyu.api.fragment.SignupFragment;
import jp.co.a_tm.moeyu.api.listener.UserDataListener;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.UserDataManager;

public class TitleActivity extends BaseActivity {
    private MediaPlayer mBathCall = new MediaPlayer();
    /* access modifiers changed from: private */
    public String mUserId;

    private class InitializeDataTask extends AsyncTask<Void, Void, Void> {
        private InitializeDataTask() {
        }

        /* synthetic */ InitializeDataTask(TitleActivity x0, InitializeDataTask x1) {
            this();
        }

        /* access modifiers changed from: protected */
        public void onPreExecute() {
            TitleActivity.this.findViewById(R.id.indicator).setVisibility(View.VISIBLE);
        }

        /* access modifiers changed from: protected|varargs */
        public Void doInBackground(Void... params) {
            try {
                TitleActivity.this.initializeData();
                new PreferencesHelper(TitleActivity.this.getApplicationContext()).setInitBoot(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Void result) {
            TitleActivity.this.findViewById(R.id.indicator).setVisibility(View.INVISIBLE);
            TitleActivity.this.login();
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title);

        findViewById(R.id.layout_top).setPadding(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        findViewById(R.id.layout_top).setBackgroundColor(Color.BLACK);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("トップページ");
        MoeyuApplication application = (MoeyuApplication) getApplication();
        if (application.isFirstRun()) {
            application.setFirstRun(false);
            if (new PreferencesHelper(this).isInitBoot()) {
                new InitializeDataTask(this, null).execute();
                return;
            } else {
                login();
                return;
            }
        }
        UserDataManager dataManager = new UserDataManager(this);
        if (dataManager.isSavedUserData()) {
            this.mUserId = dataManager.loadUserData().getUserId();
        }
    }

    /* access modifiers changed from: private */
    public void initializeData() throws IOException {
        new Decryption(getApplicationContext()).execute();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_terms) { /*2131624240*/
            showTerms();
        } else if (itemId == R.id.menu_inquiry) { /*2131624241*/
            sendInquiryMail();
        }
        return true;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        exit();
        return true;
    }

    private void showTerms() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("http://www.moe-yu.com/kiyaku.html")));
    }

    private void sendInquiryMail() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SENDTO");
        intent.setData(Uri.parse("mailto:moe-yu_support@a-tm.co.jp"));
        intent.putExtra("android.intent.extra.SUBJECT", "お問い合わせ " + this.mUserId);
        startActivity(intent);
    }

    /* access modifiers changed from: private */
    public void login() {
        UserDataManager dataManager = new UserDataManager(this);
        if (dataManager.isSavedUserData()) {
            this.mUserId = dataManager.loadUserData().getUserId();
            executeLogin(this.mUserId);
            return;
        }
        executeSignup();
    }

    private void getFileDescriptor(MediaPlayer player, String str) {
        try {
            player.setDataSource(openFileInput(str).getFD());
            player.prepare();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (IllegalStateException e12) {
            e12.printStackTrace();
        } catch (FileNotFoundException e13) {
            e13.printStackTrace();
        } catch (IOException e14) {
            e14.printStackTrace();
        }
    }

    public void toKonyokuClick(View view) {
        getFileDescriptor(this.mBathCall, String.format("%03d", new Object[]{Integer.valueOf(new Random().nextInt(3) + 3)}) + ".ogg");
        this.mBathCall.start();
        toBath();
    }

    public void toGatyaClick(View view) {
        toGacha();
    }

    public void toCollectionRoomClick(View view) {
        toCollection();
    }

    public void toTwitterClick(View view) {
        new TweetDialog(this).show(this);
    }

    public void toSetteiClick(View view) {
        toPreference();
    }

    private void executeLogin(String userId) {
        ((LoginFragment) getSupportFragmentManager().findFragmentById(R.id.login_fragment)).login(userId, new UserDataListener() {
            public void onSuccess(UserData userData) {
                TitleActivity.this.titleCall();
                if (userData.hasBonus()) {
                    TitleActivity.this.findViewById(R.id.login_bonus).setVisibility(View.VISIBLE);
                }
            }

            public void onError(MoeyuAPIException e) {
            }

            public void onCancel() {
            }
        }, false, false);
    }

    private void executeSignup() {
        ((SignupFragment) getSupportFragmentManager().findFragmentById(R.id.signup_fragment)).signup(new UserDataListener() {
            public void onSuccess(UserData userData) {
                TitleActivity.this.mUserId = userData.getUserId();
                TitleActivity.this.titleCall();
            }

            public void onError(MoeyuAPIException e) {
            }

            public void onCancel() {
            }
        });
    }

    public void onLoginBonusClick(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    /* access modifiers changed from: private */
    public void titleCall() {
        MediaPlayer mp = new MediaPlayer();
        getFileDescriptor(mp, "002_2b.ogg");
        mp.start();
    }
}
