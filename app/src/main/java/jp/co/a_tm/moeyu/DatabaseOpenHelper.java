package jp.co.a_tm.moeyu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import jp.co.a_tm.moeyu.util.Logger;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "collection.db";
    private static final int DB_VER = 1;
    private final int ITEM_MAX_ROWS = 25;
    private final String NAME = "name";
    private final String OPENED = "opened";
    private final String TERM = "term";
    private final String TITLE = "title";
    private final String[] ITEM_COLUMNS;


    private final String[] NOTE_COLUMNS;
    private final String[] TABLE_NAME;
    private final String[] VOICE_COLUMNS;


    {
        ITEM_COLUMNS = new String[]{"name", "opened"};
        NOTE_COLUMNS = new String[]{"name", "opened", "term"};
        TABLE_NAME = new String[]{"ItemTable", "VoiceTable", "NoteTable"};
        VOICE_COLUMNS = new String[]{"name", "opened", "title"};
    }

    private Context mContext;

    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VER);
        Logger.d("DatabaseOpenHelper Constructor");
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        Logger.d("DatabaseOpenHelper onCreate");
        Logger.d("all table create init start");
        createItemTable(db);
        createVoiceTable(db);
        createNoteTable(db);
        initItemRows(db);
        initVoiceRows(db);
        initNoteRows(db);
        Logger.d("all table create init end");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createItemTable(SQLiteDatabase db) {
        Logger.d("item table create start");
        try {
            db.execSQL("create table " + this.TABLE_NAME[0] + "( _id integer primary key autoincrement, " + this.ITEM_COLUMNS[0] + " text not null, " + this.ITEM_COLUMNS[1] + " text not null);");
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "创建物品表失败", e);
        }
    }

    private void createVoiceTable(SQLiteDatabase db) {
        Logger.d("voice table create start");
        try {
            db.execSQL("create table " + this.TABLE_NAME[1] + "( _id integer primary key autoincrement, " + this.VOICE_COLUMNS[0] + " text not null, " + this.VOICE_COLUMNS[1] + " text not null, " + this.VOICE_COLUMNS[2] + " text not null);");
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "创建语音表失败", e);
        }
    }

    private void createNoteTable(SQLiteDatabase db) {
        Logger.d("note table create start");
        try {
            db.execSQL("create table " + this.TABLE_NAME[2] + "( _id integer primary key autoincrement, " + this.NOTE_COLUMNS[0] + " text not null, " + this.NOTE_COLUMNS[1] + " text not null, " + this.NOTE_COLUMNS[2] + " text not null);");
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "创建笔记表失败", e);
        }
    }

    public void update(String tableName, String fileName) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.update(tableName, values, "name = '" + fileName + "'", null);
            db.close();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        }
    }

    public void update(String tableName, int id) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.update(tableName, values, "_id = '" + id + "'", null);
            db.close();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        }
    }

    public void update(String tableName, int[] id) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        try {
            SQLiteDatabase db = getWritableDatabase();
            for (int i : id) {
                db.update(tableName, values, "_id = '" + i + "'", null);
            }
            db.close();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        }
    }

    public void update(String tableName, List<Integer> ids) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        try {
            SQLiteDatabase db = getWritableDatabase();
            for (Integer integer : ids) {
                db.update(tableName, values, "_id = '" + integer.intValue() + "'", null);
            }
            db.close();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        }
    }

    public boolean isOpened(String tableName, String fileName) {
        String[] columns = new String[]{"name", "opened"};
        String[] where_args = new String[]{fileName};
        boolean opened = false;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, columns[0] + " = ?", where_args, null, null, null);
            cursor.moveToFirst();
            opened = Boolean.valueOf(cursor.getString(1)).booleanValue();
            cursor.close();
            db.close();
            return opened;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询是否打开失败", e);
            return opened;
        }
    }

    public boolean isOpened(String tableName, int id) {
        String[] columns = new String[]{"_id", "opened"};
        boolean opened = false;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, columns[0] + " = " + id, null, null, null, null);
            cursor.moveToFirst();
            opened = Boolean.valueOf(cursor.getString(1)).booleanValue();
            cursor.close();
            db.close();
            return opened;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询是否打开失败", e);
            return opened;
        }
    }

    public boolean[] isOpened(String tableName, int startId, int endId) {
        String[] columns = new String[]{"_id", "opened"};
        String where = columns[0] + " >= '" + startId + "' and " + columns[0] + " <= '" + endId + "'";
        boolean[] opened = new boolean[((endId - startId) + 1)];
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, where, null, null, null, null);
            while (cursor.moveToNext()) {
                opened[cursor.getPosition()] = Boolean.valueOf(cursor.getString(1)).booleanValue();
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "批量查询是否打开失败", e);
        }
        return opened;
    }

    public String getName(String tableName, int id) {
        String[] columns = new String[]{"_id", "name"};
        String name = "";
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, columns[0] + " = " + id, null, null, null, null);
            cursor.moveToFirst();
            name = String.valueOf(cursor.getString(1));
            cursor.close();
            db.close();
            return name;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询名称失败", e);
            return name;
        }
    }

    public String getTitle(String tableName, int id) {
        String[] columns = new String[]{"_id", "title"};
        String title = "";
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, columns[0] + " = " + id, null, null, null, null);
            cursor.moveToFirst();
            title = String.valueOf(cursor.getString(1));
            cursor.close();
            db.close();
            return title;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询标题失败", e);
            return title;
        }
    }

    public int getTerm(String tableName, int id) {
        String[] columns = new String[]{"_id", "term"};
        int name = 0;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, columns[0] + " = " + id, null, null, null, null);
            cursor.moveToFirst();
            name = Integer.valueOf(cursor.getString(1)).intValue();
            cursor.close();
            db.close();
            return name;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询条件失败", e);
            return name;
        }
    }

    public int countOpened(String tableName) {
        String[] columns = new String[]{"opened"};
        String[] where_args = new String[]{"true"};
        int count = -1;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, columns[0] + " = ?", where_args, null, null, null);
            count = cursor.getCount();
            cursor.close();
            db.close();
            return count;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "统计已打开数失败", e);
            return count;
        }
    }

    public int countOpened(String tableName, int startId, int endId) {
        String[] columns = new String[]{"_id", "opened"};
        String where = columns[0] + " >= '" + startId + "' and " + columns[0] + " <= '" + endId + "' and " + columns[1] + " = 'true'";
        int count = -1;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, where, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
            db.close();
            return count;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "统计已打开数失败", e);
            return count;
        }
    }

    public int countRows(String tableName) {
        String[] columns = new String[]{"_id"};
        int count = 0;
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.query(tableName, columns, null, null, null, null, null);
            count = cursor.getCount();
            cursor.close();
            db.close();
            return count;
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "统计行数失败", e);
            return count;
        }
    }

    private void initItemRows(SQLiteDatabase db) {
        int i = 0;
        while (i < ITEM_MAX_ROWS) {
            try {
                db.execSQL("insert into " + this.TABLE_NAME[0] + "(" + this.ITEM_COLUMNS[0] + "," + this.ITEM_COLUMNS[1] + ") values ('" + String.format("%02d", new Object[]{Integer.valueOf(i + 1)}) + "','false');");
                i++;
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "初始化物品行失败", e);
                return;
            }
        }
    }

    private void initVoiceRows(SQLiteDatabase db) {
        ArrayList<VoiceTitle> list = new CSV().loadVoice(this.mContext);
        int i = 0;
        while (i < list.size()) {
            try {
                db.execSQL("insert into " + this.TABLE_NAME[1] + "(" + this.VOICE_COLUMNS[0] + "," + this.VOICE_COLUMNS[1] + "," + this.VOICE_COLUMNS[2] + ") values ('" + ((VoiceTitle) list.get(i)).getFileName() + "','false','" + ((VoiceTitle) list.get(i)).getTitle() + "');");
                i++;
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "初始化语音行失败", e);
                return;
            }
        }
    }

    private void initNoteRows(SQLiteDatabase db) {
        ArrayList<String> list = new CSV().loadNote(this.mContext);
        int i = 0;
        while (i < list.size()) {
            try {
                db.execSQL("insert into " + this.TABLE_NAME[2] + "(" + this.NOTE_COLUMNS[0] + "," + this.NOTE_COLUMNS[1] + "," + this.NOTE_COLUMNS[2] + ") values ('" + String.format("%02d", new Object[]{Integer.valueOf(i + 1)}) + "','false','" + ((String) list.get(i)) + "');");
                i++;
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "初始化笔记行失败", e);
                return;
            }
        }
    }
}
