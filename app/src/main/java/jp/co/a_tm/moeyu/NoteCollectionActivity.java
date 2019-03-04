package jp.co.a_tm.moeyu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
//import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import java.util.ArrayList;
import java.util.List;
import jp.co.a_tm.moeyu.util.Logger;

public class NoteCollectionActivity extends BaseActivity {
    private int mItemCountOpened;
    private NoteTableController mNoteController;
    private int mVoiceCountOpened;

    private class NoteListAdapter extends ArrayAdapter<NoteListItem> {
        private LayoutInflater mInflater;

        public NoteListAdapter(Context context, List<NoteListItem> list) {
            super(context, 0, list);
            this.mInflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = this.mInflater.inflate(R.layout.list_note, null);
            final NoteListItem item = (NoteListItem) getItem(position);
            setLeftContents(view, item);
            setRightContents(view, item);
            ((ImageButton) view.findViewById(R.id.imgbutton_listview_right_bottom_readicon)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Logger.d("imgButton");
                    if (item.getCleared()) {
//                        GoogleAnalyticsTracker.getInstance().trackEvent("日記", "日記1", "", 1);
                        new NoteTableController(NoteListAdapter.this.getContext()).update(String.format("%02d", new Object[]{Integer.valueOf(item.mNo)}));
                        ((ImageView) NoteCollectionActivity.this.findViewById(R.id.img_diarydialog_contents_words)).setImageResource(NoteListAdapter.this.getContext().getResources().getIdentifier("diary_" + item.mNo, "drawable", "jp.co.a_tm.moeyu"));
                        NoteCollectionActivity.this.findViewById(R.id.layout_diarydialog).setVisibility(View.VISIBLE);
                    }
                }
            });
            return view;
        }

        private void setLeftContents(View view, NoteListItem item) {
            int noTenths = getTenths(item.mNo);
            int noOnes = item.mNo % 10;
            setImageResource(view, R.id.img_listview_left_tenth, "diary_number" + String.format("%02d", new Object[]{Integer.valueOf(noTenths)}));
            setImageResource(view, R.id.img_listview_left_one, "diary_number" + String.format("%02d", new Object[]{Integer.valueOf(noOnes)}));
            if (item.getCleared()) {
                setImageViewVisibility(view, R.id.img_listview_left_lock, 4);
            } else {
                setImageViewVisibility(view, R.id.img_listview_left_lock, 0);
            }
        }

        private void setRightContents(View view, NoteListItem item) {
            int termHundreds = item.mTerm / 100;
            int termTenths = getTenths(item.mTerm);
            int termOnes = item.mTerm % 10;
            if (item.mNo % 2 == 1) {
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_voice, 0);
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_item, 4);
                if (item.getPreCleared()) {
                    setImageResource(view, R.id.img_listview_right_center_voice_center_hundred, "number" + String.format("%02d", new Object[]{Integer.valueOf(termHundreds)}));
                    setImageResource(view, R.id.img_listview_right_center_voice_center_tenth, "number" + String.format("%02d", new Object[]{Integer.valueOf(termTenths)}));
                    setImageResource(view, R.id.img_listview_right_center_voice_center_one, "number" + String.format("%02d", new Object[]{Integer.valueOf(termOnes)}));
                    if (item.mTerm < 10) {
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 4);
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 4);
                    } else if (item.mTerm < 100) {
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 4);
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 0);
                    } else {
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 0);
                        setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 0);
                    }
                } else {
                    setImageResource(view, R.id.img_listview_right_center_voice_center_hundred, "number_q");
                    setImageResource(view, R.id.img_listview_right_center_voice_center_tenth, "number_q");
                    setImageResource(view, R.id.img_listview_right_center_voice_center_one, "number_q");
                    setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_hundred, 0);
                    setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_tenth, 0);
                    setImageViewVisibility(view, R.id.img_listview_right_center_voice_center_one, 0);
                }
            } else {
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_voice, 4);
                setLinearLayoutVisibility(view, R.id.layout_listview_right_center_item, 0);
                if (item.getPreCleared()) {
                    setImageResource(view, R.id.img_listview_right_center_item_center_tenth, "number" + String.format("%02d", new Object[]{Integer.valueOf(termTenths)}));
                    setImageResource(view, R.id.img_listview_right_center_item_center_one, "number" + String.format("%02d", new Object[]{Integer.valueOf(termOnes)}));
                    if (item.mTerm < 10) {
                        setImageViewVisibility(view, R.id.img_listview_right_center_item_center_tenth, 4);
                    } else {
                        setImageViewVisibility(view, R.id.img_listview_right_center_item_center_tenth, 0);
                    }
                } else {
                    setImageResource(view, R.id.img_listview_right_center_item_center_tenth, "number_q");
                    setImageResource(view, R.id.img_listview_right_center_item_center_one, "number_q");
                    setImageViewVisibility(view, R.id.img_listview_right_center_item_center_tenth, 0);
                    setImageViewVisibility(view, R.id.img_listview_right_center_item_center_one, 0);
                }
            }
            if (item.getCleared()) {
                setImageResource(view, R.id.imgbutton_listview_right_bottom_readicon, "readicon_a");
            } else {
                setImageResource(view, R.id.imgbutton_listview_right_bottom_readicon, "readicon_b");
            }
        }

        private void setImageResource(View view, int resId, String file) {
            ((ImageView) view.findViewById(resId)).setImageResource(getContext().getResources().getIdentifier(file, "drawable", "jp.co.a_tm.moeyu"));
        }

        private void setImageViewVisibility(View view, int resId, int visibility) {
            ((ImageView) view.findViewById(resId)).setVisibility(visibility);
        }

        private void setLinearLayoutVisibility(View view, int resId, int visibility) {
            ((LinearLayout) view.findViewById(resId)).setVisibility(visibility);
        }

        private int getTenths(int num) {
            return (num % 100) / 10;
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("NoteCollectionAcitivity");
        setContentView(R.layout.activity_notecollection);
        ItemTableController itemController = new ItemTableController(this);
        VoiceTableController voiceController = new VoiceTableController(this);
        this.mItemCountOpened = itemController.countOpened();
        this.mVoiceCountOpened = voiceController.countOpened();
        this.mNoteController = new NoteTableController(this);
        List<NoteListItem> list = new ArrayList();
        for (int i = 1; i <= this.mNoteController.countRows(); i++) {
            list.add(new NoteListItem(i, this.mNoteController.isTerm(i), checkCleared(i), checkPreCleared(i)));
        }
        ((ListView) findViewById(R.id.listview_notecollec)).setAdapter(new NoteListAdapter(this, list));
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("秘密の日記");
    }

    private boolean checkCleared(int rowNumber) {
        if (rowNumber % 2 == 1) {
            if (this.mVoiceCountOpened >= this.mNoteController.isTerm(rowNumber)) {
                return true;
            }
        } else if (this.mItemCountOpened >= this.mNoteController.isTerm(rowNumber)) {
            return true;
        }
        return false;
    }

    private boolean checkPreCleared(int rowNumber) {
        if (rowNumber > 2 && !checkCleared(rowNumber - 2)) {
            return false;
        }
        return true;
    }

    public void onDiaryClick(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    public void toKonyokuClick(View view) {
        toBath();
    }

    public void toGatyaClick(View view) {
        toGacha();
    }

    public void toMomorisClick(View view) {
        toRoom();
    }

    public void toTitleClick(View view) {
        finish();
    }
}
