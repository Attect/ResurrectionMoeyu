package jp.co.a_tm.moeyu;

import android.content.Context;
import java.io.Serializable;
import java.util.ArrayList;
import jp.co.a_tm.moeyu.model.EventData;
import jp.co.a_tm.moeyu.model.EventData.Type;
import jp.co.a_tm.moeyu.model.UserData;
import jp.co.a_tm.moeyu.util.UserDataManager;

public class EventController implements Serializable {
    private static final long serialVersionUID = 2418885684766671952L;
    private Context mContext;
    private ArrayList<EventData> mList = new ArrayList();

    public EventController(Context context) {
        this.mContext = context;
        checkEvent();
    }

    private void checkEvent() {
        VoiceTableController voice = new VoiceTableController(this.mContext);
        ItemTableController item = new ItemTableController(this.mContext);
        checkLevelUpEvent(voice);
        checkItemComplete(item, voice);
    }

    private void checkLevelUpEvent(VoiceTableController voice) {
        UserData user = new UserDataManager(this.mContext).loadUserData();
        if (2 <= user.getLevel() && !isOpenedLastVoice(voice, R.array.event_levelup_2)) {
            this.mList.add(new EventData(this.mContext, Type.Level2));
        }
        if (3 <= user.getLevel() && !isOpenedLastVoice(voice, R.array.event_levelup_3)) {
            this.mList.add(new EventData(this.mContext, Type.Level3));
        }
        if (4 <= user.getLevel() && !isOpenedLastVoice(voice, R.array.event_levelup_4)) {
            this.mList.add(new EventData(this.mContext, Type.Level4));
        }
        if (5 <= user.getLevel() && !isOpenedLastVoice(voice, R.array.event_levelup_5)) {
            this.mList.add(new EventData(this.mContext, Type.Level5));
        }
        if (6 <= user.getLevel() && !isOpenedLastVoice(voice, R.array.event_levelup_6)) {
            this.mList.add(new EventData(this.mContext, Type.Level6));
        }
    }

    private void checkItemComplete(ItemTableController item, VoiceTableController voice) {
        if (item.getOpenedPercent() == 100 && !isOpenedLastVoice(voice, R.array.event_complete_item)) {
            this.mList.add(new EventData(this.mContext, Type.Complete));
        }
    }

    private boolean isOpenedLastVoice(VoiceTableController voice, int rId) {
        String[] strVoiceNumber = this.mContext.getResources().getStringArray(rId);
        return voice.isOpened(strVoiceNumber[strVoiceNumber.length - 1]);
    }

    public void push(Type type) {
        this.mList.add(new EventData(this.mContext, type));
    }

    public EventData pop() {
        if (this.mList.isEmpty()) {
            return null;
        }
        EventData data = (EventData) this.mList.get(0);
        this.mList.remove(0);
        return data;
    }

    public ArrayList<EventData> getList() {
        return this.mList;
    }
}
