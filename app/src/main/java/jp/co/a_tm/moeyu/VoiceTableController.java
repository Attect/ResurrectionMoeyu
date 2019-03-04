package jp.co.a_tm.moeyu;

import android.content.Context;

public class VoiceTableController extends DatabaseTableController {
    public VoiceTableController(Context context) {
        super(context);
        this.TABLE_NAME = "VoiceTable";
    }
}
