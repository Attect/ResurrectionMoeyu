package jp.co.a_tm.moeyu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import java.util.List;

public class ItemGridAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Integer> mList;

    public ItemGridAdapter(Context context, List<Integer> list) {
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return this.mList.size();
    }

    public Object getItem(int position) {
        return this.mList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = (ImageView) mInflater.inflate(R.layout.item, null);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageResource(mList.get(position));
        return imageView;
    }
}
