package jp.co.a_tm.moeyu;

import android.content.Context;

public class ItemTableController extends DatabaseTableController {
    public ItemTableController(Context context) {
        super(context);
        this.TABLE_NAME = "ItemTable";
    }
}
