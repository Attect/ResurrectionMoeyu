package jp.co.a_tm.moeyu;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.co.a_tm.moeyu.util.Logger;

public class ItemCollectionActivity extends BaseActivity {
    private static Scene[] sScenes = new Scene[]{null, Scene.bath_a, Scene.body, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.bath_a, Scene.body, Scene.head, Scene.bath_a, Scene.body, Scene.bath_a, Scene.body, Scene.bath_a, Scene.bath_a};
    private final int MAX_ROW = 25;

    private class ItemListAdapter extends ArrayAdapter<ItemListItem> {
        private LayoutInflater mInflater;

        public ItemListAdapter(Context context, List<ItemListItem> list) {
            super(context, 0, list);
            this.mInflater = LayoutInflater.from(context);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View view = this.mInflater.inflate(R.layout.list_item, null);
            ItemListItem item = (ItemListItem) getItem(position);
            for (int i = 1; i <= item.getButtonCount(); i++) {
                if (item.getItemNumber(i) == -1) {
                    hideImageButton(view, i);
                } else {
                    setImageButton(view, i, item);
                }
            }
            return view;
        }

        private void hideImageButton(View view, int buttonNumber) {
            ((ImageButton) view.findViewById(getResID("imgbutton_listitem_item" + buttonNumber, "id"))).setVisibility(View.INVISIBLE);
        }

        private void setImageButton(View view, final int buttonNumber, final ItemListItem item) {
            ImageButton imgButton = (ImageButton) view.findViewById(getResID("imgbutton_listitem_item" + buttonNumber, "id"));
            if (item.getOpened(buttonNumber)) {
                imgButton.setImageResource(getResID(String.format("item%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
            } else {
                imgButton.setImageResource(getResID(String.format("gray_item%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
            }
            imgButton.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Logger.d("Click item=" + item.getItemNumber(buttonNumber));
                    final Dialog dialog = new Dialog(ItemCollectionActivity.this, R.style.clear_dialog);
                    dialog.requestWindowFeature(1);
                    dialog.setContentView(R.layout.dialog_itemcollec);
                    dialog.setCanceledOnTouchOutside(true);
                    ItemListAdapter.this.setDialogImageView(dialog, item, buttonNumber);
                    ((ImageButton) dialog.findViewById(R.id.imgbutton_itemcollec_dialog_cancel)).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.findViewById(R.id.imgbutton_itemcollec_dialog_konyoku).setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (item.getOpened(buttonNumber)) {
                                ItemCollectionActivity.this.startBathActivity(item.getItemNumber(buttonNumber));
                            }
                        }
                    });
                    dialog.show();
                }
            });
        }

        /* access modifiers changed from: private */
        public void setDialogImageView(Dialog dialog, ItemListItem item, int buttonNumber) {
            ImageView imgName = (ImageView) dialog.findViewById(R.id.img_itemcollec_dialog_itemname);
            ImageView imgPict = (ImageView) dialog.findViewById(R.id.img_itemcollec_dialog_itempicture);
            ImageView imgComment = (ImageView) dialog.findViewById(R.id.img_itemcollec_dialog_itemcomment);
            if (item.getOpened(buttonNumber)) {
                imgName.setImageResource(getResID("itemname_bar_" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
                imgPict.setImageResource(getResID("item" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}) + "_2x", "drawable"));
                imgComment.setImageResource(getResID("item_comment" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
                return;
            }
            imgName.setImageResource(getResID("itemname_bar_" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}) + "glay", "drawable"));
            imgPict.setImageResource(getResID("gray_item" + String.format("%02d", new Object[]{Integer.valueOf(item.getItemNumber(buttonNumber))}), "drawable"));
            imgComment.setImageResource(getResID("item_comment00", "drawable"));
        }

        private int getResID(String strName, String strRes) {
            return getContext().getResources().getIdentifier(strName, strRes, ItemCollectionActivity.this.getPackageName());
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemcollection);
        findViewById(R.id.layout_itemcollec).setPadding(0, MainActivity.FIX_HEIGHT / 2, 0, MainActivity.FIX_HEIGHT / 2);
        findViewById(R.id.layout_itemcollec).setBackgroundColor(Color.BLACK);

        ItemTableController itemController = new ItemTableController(getApplicationContext());
        List<ItemListItem> list = new ArrayList();
        list.add(new ItemListItem(new int[]{-1, 1, -1}, itemController));
        for (int i = 2; i <= itemController.countRows(); i += 3) {
            list.add(new ItemListItem(new int[]{i, i + 1, i + 2}, itemController));
        }
        ((ListView) findViewById(R.id.listview_itemcollec)).setAdapter(new ItemListAdapter(this, list));
        ((TextView) findViewById(R.id.textview_itemcollec_getstate)).setText(itemController.countOpened() + "/" + 25);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
//        this.mTracker.trackPageView("アイテムコレクション");
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

    public void toTitleClick(View view) {
        finish();
    }

    /* access modifiers changed from: private */
    public void startBathActivity(int itemId) {
        toBath(getScene(itemId), itemId);
    }

    private String getScene(int itemId) {
        return sScenes[itemId].toString();
    }
}
