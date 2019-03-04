package jp.co.a_tm.moeyu.model;

import android.content.Context;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import jp.co.a_tm.moeyu.R;

public class EventData implements Serializable {
    private static final long serialVersionUID = -7139381876259880756L;
    private Type mType;
    private ArrayList<String> mVoiceList;

    public enum Type {
        Level2,
        Level3,
        Level4,
        Level5,
        Level6,
        Complete
    }

    public EventData(Context context, Type type) {
        this.mType = type;
        if (type == Type.Level2) {
            this.mVoiceList = toArrayList(context.getResources().getStringArray(R.array.event_levelup_2));
        } else if (type == Type.Level3) {
            this.mVoiceList = toArrayList(context.getResources().getStringArray(R.array.event_levelup_3));
        } else if (type == Type.Level4) {
            this.mVoiceList = toArrayList(context.getResources().getStringArray(R.array.event_levelup_4));
        } else if (type == Type.Level5) {
            this.mVoiceList = toArrayList(context.getResources().getStringArray(R.array.event_levelup_5));
        } else if (type == Type.Level6) {
            this.mVoiceList = toArrayList(context.getResources().getStringArray(R.array.event_levelup_6));
        } else if (type == Type.Complete) {
            this.mVoiceList = toArrayList(context.getResources().getStringArray(R.array.event_complete_item));
        }
    }

    private ArrayList<String> toArrayList(String[] str) {
        return new ArrayList<>(Arrays.asList(str));
    }

    public ArrayList<String> getVoiceList() {
        return this.mVoiceList;
    }

    public Type getType() {
        return this.mType;
    }
}
