package jp.co.a_tm.moeyu;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import jp.co.a_tm.moeyu.util.Logger;

public class VoiceCollectionActivity extends BaseActivity {
    private final int BASE_ROW = 15;
    private final int EVENT_MAX_ROW = 288;
    private final int ITEM_MAX_ROW = 244;
    private final int NORMAL_MAX_ROW = 122;
    private TextView mCompleteState;
    /* access modifiers changed from: private */
    public VoiceTableController mController;
    private VoiceListAdapter mEventAdapter;
    /* access modifiers changed from: private */
    public boolean[] mEventOpened = new boolean[288];
    private VoiceListAdapter mItemAdapter;
    /* access modifiers changed from: private */
    public boolean[] mItemOpened = new boolean[244];
    private ListView mListView;
    private VoiceListAdapter mNormalAdapter;
    /* access modifiers changed from: private */
    public boolean[] mNormalOpened = new boolean[122];
    /* access modifiers changed from: private */
    public MediaPlayer mPlayer;
    /* access modifiers changed from: private */
    public Tab mSelectTab;

    enum Tab {
        NORMAL,
        ITEM,
        EVENT
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("VoiceCollectionAcitivity");
        setContentView(R.layout.activity_voicecollection);
        this.mController = new VoiceTableController(this);
        this.mListView = (ListView) findViewById(R.id.listview_voicecollec);
        this.mCompleteState = (TextView) findViewById(R.id.textview_voicecollec_getstate);
        setTab(Tab.NORMAL);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                switch (VoiceCollectionActivity.this.mSelectTab.ordinal()) {
                    case 1:
                        if (VoiceCollectionActivity.this.mNormalOpened[position]) {
                            VoiceCollectionActivity.this.getFileDescriptor(VoiceCollectionActivity.this.mPlayer, VoiceCollectionActivity.this.mController.isName(position + 1) + ".ogg");
                            VoiceCollectionActivity.this.mPlayer.start();
                            return;
                        }
                        return;
                    case 2:
                        if (VoiceCollectionActivity.this.mItemOpened[position]) {
                            VoiceCollectionActivity.this.getFileDescriptor(VoiceCollectionActivity.this.mPlayer, VoiceCollectionActivity.this.mController.isName(((position + 122) - 15) + 2) + ".ogg");
                            VoiceCollectionActivity.this.mPlayer.start();
                            return;
                        }
                        return;
                    case 3:
                        if (VoiceCollectionActivity.this.mEventOpened[position]) {
                            VoiceCollectionActivity.this.getFileDescriptor(VoiceCollectionActivity.this.mPlayer, VoiceCollectionActivity.this.mController.isName(((position + 244) - 15) + 2) + ".ogg");
                            VoiceCollectionActivity.this.mPlayer.start();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("ボイスコレクション");
        this.mPlayer = new MediaPlayer();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.mPlayer.release();
    }

    private void setTab(Tab tab) {
        this.mSelectTab = tab;
        switch (this.mSelectTab) {
            case NORMAL:
                if (this.mNormalAdapter == null) {
                    this.mNormalOpened = this.mController.isOpened(1, 108);
                    this.mNormalAdapter = addList(this.mController, this.mNormalOpened, 0);
                }
                this.mListView.setAdapter(this.mNormalAdapter);
                this.mCompleteState.setText(this.mController.countOpened(1, 108) + "/" + 108);
                return;
            case ITEM:
                if (this.mItemAdapter == null) {
                    this.mItemOpened = this.mController.isOpened(109, 230);
                    this.mItemAdapter = addList(this.mController, this.mItemOpened, 108);
                }
                this.mListView.setAdapter(this.mItemAdapter);
                this.mCompleteState.setText(this.mController.countOpened(108, 229) + "/" + 122);
                return;
            case EVENT:
                if (this.mEventAdapter == null) {
                    this.mEventOpened = this.mController.isOpened(231, 274);
                    this.mEventAdapter = addList(this.mController, this.mEventOpened, 230);
                }
                this.mListView.setAdapter(this.mEventAdapter);
                this.mCompleteState.setText(this.mController.countOpened(230, 273) + "/" + 44);
                return;
            default:
                return;
        }
    }

    private VoiceListAdapter addList(VoiceTableController controller, boolean[] opened, int base) {
        List<String> list = new ArrayList();
        for (int i = 0; i < opened.length; i++) {
            if (opened[i]) {
                list.add(controller.isTitle((base + i) + 1));
            } else {
                list.add("？？？？？？");
            }
        }
        return new VoiceListAdapter(this, list);
    }

    public void getFileDescriptor(MediaPlayer player, String str) {
        try {
            player.reset();
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

    public void tabClick(View view) {
        ImageButton normalButton = (ImageButton) findViewById(R.id.imgbutton_voicecollec_normal);
        ImageButton itemButton = (ImageButton) findViewById(R.id.imgbutton_voicecollec_item);
        ImageButton eventButton = (ImageButton) findViewById(R.id.imgbutton_voicecollec_event);
        int id = view.getId();
        if (id == R.id.imgbutton_voicecollec_normal) { /*2131624131*/
            normalButton.setImageResource(R.drawable.nomalvoice);
            itemButton.setImageResource(R.drawable.itemvoice_glay);
            eventButton.setImageResource(R.drawable.iventvoice_glay);
            setTab(Tab.NORMAL);
            return;
        } else if (id == R.id.imgbutton_voicecollec_item) { /*2131624132*/
            normalButton.setImageResource(R.drawable.nomalvoice_glay);
            itemButton.setImageResource(R.drawable.itemvoice);
            eventButton.setImageResource(R.drawable.iventvoice_glay);
            setTab(Tab.ITEM);
            return;
        } else if (id == R.id.imgbutton_voicecollec_event) { /*2131624133*/
            normalButton.setImageResource(R.drawable.nomalvoice_glay);
            itemButton.setImageResource(R.drawable.itemvoice_glay);
            eventButton.setImageResource(R.drawable.iventvoice);
            setTab(Tab.EVENT);
            return;
        }
        return;
    }

    public void toGatyaClick(View view) {
        toGacha();
    }

    public void toCollectionRoomClick(View view) {
        toCollection();
    }

    public void toTitleClick(View view) {
        finish();
    }
}
