package jp.co.a_tm.moeyu;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
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
        Logger.d("DatabaseOpenHelper onUpgrade: " + oldVersion + " -> " + newVersion);
        if (oldVersion < newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + this.TABLE_NAME[0]);
                db.execSQL("DROP TABLE IF EXISTS " + this.TABLE_NAME[1]);
                db.execSQL("DROP TABLE IF EXISTS " + this.TABLE_NAME[2]);
                onCreate(db);
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "数据库升级失败", e);
            }
        }
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
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.update(tableName, values, "name = ?", new String[]{fileName});
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        } finally {
            if (db != null) db.close();
        }
    }

    public void update(String tableName, int id) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.update(tableName, values, "_id = ?", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        } finally {
            if (db != null) db.close();
        }
    }

    public void update(String tableName, int[] ids) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            for (int i : ids) {
                db.update(tableName, values, "_id = ?", new String[]{String.valueOf(i)});
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        } finally {
            if (db != null) db.close();
        }
    }

    public void update(String tableName, List<Integer> ids) {
        ContentValues values = new ContentValues();
        values.put("opened", "true");
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            for (Integer id : ids) {
                db.update(tableName, values, "_id = ?", new String[]{String.valueOf(id)});
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "更新数据失败", e);
        } finally {
            if (db != null) db.close();
        }
    }

    public boolean isOpened(String tableName, String fileName) {
        String[] columns = new String[]{"name", "opened"};
        String[] whereArgs = new String[]{fileName};
        boolean opened = false;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " = ?", whereArgs, null, null, null);
            if (cursor.moveToFirst()) {
                opened = Boolean.valueOf(cursor.getString(1)).booleanValue();
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询是否打开失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return opened;
    }

    public boolean isOpened(String tableName, int id) {
        String[] columns = new String[]{"_id", "opened"};
        boolean opened = false;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToFirst()) {
                opened = Boolean.valueOf(cursor.getString(1)).booleanValue();
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询是否打开失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return opened;
    }

    public boolean[] isOpened(String tableName, int startId, int endId) {
        String[] columns = new String[]{"_id", "opened"};
        boolean[] opened = new boolean[((endId - startId) + 1)];
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " >= ? and " + columns[0] + " <= ?",
                    new String[]{String.valueOf(startId), String.valueOf(endId)}, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                if (id >= startId && id <= endId) {
                    opened[id - startId] = Boolean.valueOf(cursor.getString(1)).booleanValue();
                }
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "批量查询是否打开失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return opened;
    }

    public String getName(String tableName, int id) {
        String[] columns = new String[]{"_id", "name"};
        String name = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToFirst()) {
                name = cursor.getString(1);
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询名称失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return name;
    }

    public String getTitle(String tableName, int id) {
        String[] columns = new String[]{"_id", "title"};
        String title = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToFirst()) {
                title = cursor.getString(1);
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询标题失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return title;
    }

    public int getTerm(String tableName, int id) {
        String[] columns = new String[]{"_id", "term"};
        int term = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " = ?", new String[]{String.valueOf(id)}, null, null, null);
            if (cursor.moveToFirst()) {
                term = Integer.valueOf(cursor.getString(1));
            }
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "查询条件失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return term;
    }

    public int countOpened(String tableName) {
        String[] columns = new String[]{"opened"};
        String[] whereArgs = new String[]{"true"};
        int count = -1;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " = ?", whereArgs, null, null, null);
            count = cursor.getCount();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "统计已打开数失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return count;
    }

    public int countOpened(String tableName, int startId, int endId) {
        String[] columns = new String[]{"_id", "opened"};
        int count = -1;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, columns[0] + " >= ? and " + columns[0] + " <= ? and " + columns[1] + " = ?",
                    new String[]{String.valueOf(startId), String.valueOf(endId), "true"}, null, null, null);
            count = cursor.getCount();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "统计已打开数失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return count;
    }

    public int countRows(String tableName) {
        String[] columns = new String[]{"_id"};
        int count = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            cursor = db.query(tableName, columns, null, null, null, null, null);
            count = cursor.getCount();
        } catch (Exception e) {
            Logger.e("DatabaseOpenHelper", "统计行数失败", e);
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }
        return count;
    }

    private void initItemRows(SQLiteDatabase db) {
        String sql = "insert into " + this.TABLE_NAME[0] + "(" + this.ITEM_COLUMNS[0] + "," + this.ITEM_COLUMNS[1] + ") values (?, ?);";
        SQLiteStatement stmt = db.compileStatement(sql);
        for (int i = 0; i < ITEM_MAX_ROWS; i++) {
            try {
                stmt.bindString(1, String.format("%02d", i + 1));
                stmt.bindString(2, "false");
                stmt.executeInsert();
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "初始化物品行失败", e);
                return;
            } finally {
                stmt.clearBindings();
            }
        }
        stmt.close();
    }

    private void initVoiceRows(SQLiteDatabase db) {
        ArrayList<VoiceTitle> list = new CSV().loadVoice(this.mContext);
        String sql = "insert into " + this.TABLE_NAME[1] + "(" + this.VOICE_COLUMNS[0] + "," + this.VOICE_COLUMNS[1] + "," + this.VOICE_COLUMNS[2] + ") values (?, ?, ?);";
        SQLiteStatement stmt = db.compileStatement(sql);
        for (int i = 0; i < list.size(); i++) {
            try {
                VoiceTitle vt = list.get(i);
                stmt.bindString(1, vt.getFileName());
                stmt.bindString(2, "false");
                stmt.bindString(3, vt.getTitle());
                stmt.executeInsert();
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "初始化语音行失败", e);
                stmt.close();
                return;
            } finally {
                stmt.clearBindings();
            }
        }
        stmt.close();
    }

    private void initNoteRows(SQLiteDatabase db) {
        ArrayList<String> list = new CSV().loadNote(this.mContext);
        String sql = "insert into " + this.TABLE_NAME[2] + "(" + this.NOTE_COLUMNS[0] + "," + this.NOTE_COLUMNS[1] + "," + this.NOTE_COLUMNS[2] + ") values (?, ?, ?);";
        SQLiteStatement stmt = db.compileStatement(sql);
        for (int i = 0; i < list.size(); i++) {
            try {
                stmt.bindString(1, String.format("%02d", i + 1));
                stmt.bindString(2, "false");
                stmt.bindString(3, list.get(i));
                stmt.executeInsert();
            } catch (Exception e) {
                Logger.e("DatabaseOpenHelper", "初始化笔记行失败", e);
                stmt.close();
                return;
            } finally {
                stmt.clearBindings();
            }
        }
        stmt.close();
    }
}
