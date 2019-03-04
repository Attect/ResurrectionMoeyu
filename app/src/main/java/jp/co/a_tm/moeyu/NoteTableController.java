package jp.co.a_tm.moeyu;

import android.content.Context;

public class NoteTableController extends DatabaseTableController {
    public NoteTableController(Context context) {
        super(context);
        this.TABLE_NAME = "NoteTable";
    }
}
