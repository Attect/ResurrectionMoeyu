package jp.co.a_tm.moeyu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class VoiceListAdapter extends ArrayAdapter<String> {
    private LayoutInflater mInflater;

    public VoiceListAdapter(Context context, List<String> list) {
        super(context, 0, list);
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = this.mInflater.inflate(R.layout.list_voice, null);
        ((TextView) view.findViewById(R.id.textview_listvoice_content)).setText((String) getItem(position));
        return view;
    }
}
