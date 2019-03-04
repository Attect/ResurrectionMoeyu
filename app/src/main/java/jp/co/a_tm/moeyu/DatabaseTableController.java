package jp.co.a_tm.moeyu;

import android.content.Context;
import java.util.List;
import jp.co.a_tm.moeyu.util.Logger;

public class DatabaseTableController {
    protected String TABLE_NAME;
    protected DatabaseOpenHelper mHelper;

    public DatabaseTableController(Context context) {
        Logger.d("DBTableController Constructor");
        this.mHelper = new DatabaseOpenHelper(context);
    }

    public void update(String fileName) {
        this.mHelper.update(this.TABLE_NAME, fileName);
    }

    public void update(int id) {
        this.mHelper.update(this.TABLE_NAME, id);
    }

    public void update(int[] id) {
        this.mHelper.update(this.TABLE_NAME, id);
    }

    public void update(List<Integer> ids) {
        this.mHelper.update(this.TABLE_NAME, (List) ids);
    }

    public boolean isOpened(String fileName) {
        return this.mHelper.isOpened(this.TABLE_NAME, fileName);
    }

    public boolean isOpened(int id) {
        return this.mHelper.isOpened(this.TABLE_NAME, id);
    }

    public boolean[] isOpened(int startId, int endId) {
        return this.mHelper.isOpened(this.TABLE_NAME, startId, endId);
    }

    public String isName(int id) {
        return this.mHelper.isName(this.TABLE_NAME, id);
    }

    public String isTitle(int id) {
        return this.mHelper.isTitle(this.TABLE_NAME, id);
    }

    public int isTerm(int id) {
        return this.mHelper.isTerm(this.TABLE_NAME, id);
    }

    public int countOpened() {
        return this.mHelper.countOpened(this.TABLE_NAME);
    }

    public int countOpened(int startId, int endId) {
        return this.mHelper.countOpened(this.TABLE_NAME, startId, endId);
    }

    public int countRows() {
        return this.mHelper.countRows(this.TABLE_NAME);
    }

    public int getOpenedPercent() {
        return (this.mHelper.countOpened(this.TABLE_NAME) * 100) / this.mHelper.countRows(this.TABLE_NAME);
    }
}
