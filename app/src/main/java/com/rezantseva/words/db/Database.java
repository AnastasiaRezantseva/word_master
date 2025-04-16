package com.rezantseva.words.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Database extends SQLiteOpenHelper {

    private static final String TAG = "Database";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "words.db";
    public static final String SQL_CREATE_GROUPS = "CREATE TABLE groups (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL);";
    public static final String SQL_CREATE_WORDS = "CREATE TABLE words (_id INTEGER PRIMARY KEY AUTOINCREMENT, _groupid INTEGER, origin TEXT NOT NULL, translation TEXT NOT NULL);";

    private final Context mContext;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private static final String SQL_SELECT_GROUPS = "SELECT g._id AS id, g.name AS name FROM groups AS g ORDER BY g.name ASC";

    public ArrayList<Group> getAllGroups() {
        ArrayList<Group> groups = new ArrayList<Group>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SQL_SELECT_GROUPS, null);
        while (cursor.moveToNext()) {
            Group group = new Group();
            group.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            group.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            groups.add(group);
        }
        return groups;
    }

    private static final String SQL_SELECT_GROUP_WORDS = "SELECT w._id AS id, w._groupid AS groupid, w.origin AS origin, w.translation AS translation FROM words AS w WHERE w._groupid = ?";

    public ArrayList<Word> getGroupWords(long groupId) {
        ArrayList<Word> words = new ArrayList<Word>();
        SQLiteDatabase db = getReadableDatabase();
        String[] whereArgs = {String.valueOf(groupId)};
        Cursor cursor = db.rawQuery(SQL_SELECT_GROUP_WORDS, whereArgs);
        while (cursor.moveToNext()) {
            Word word = new Word();
            word.setId(cursor.getLong(cursor.getColumnIndexOrThrow("id")));
            word.setGroupId(cursor.getLong(cursor.getColumnIndexOrThrow("groupid")));
            word.setOrigin(cursor.getString(cursor.getColumnIndexOrThrow("origin")));
            word.setTranslation(cursor.getString(cursor.getColumnIndexOrThrow("translation")));
            words.add(word);
        }
        return words;
    }

    public void insertOrUpdateGroup(Group group, List<Word> words) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", group.getName());
        // update group
        long id = group.getId();
        if (id == 0) {
            id = db.insertOrThrow("groups", null, values);
            group.setId(id);
        } else {
            String whereClause = "_id = ?";
            String[] whereArgs = {String.valueOf(id)};
            db.update("groups", values, whereClause, whereArgs);
        }
        //update words
        ContentValues wordValues = new ContentValues();
        for (Word word : words) {
            if (TextUtils.isEmpty(word.getOrigin()) || TextUtils.isEmpty(word.getTranslation())) {
                continue;
            }
            word.setGroupId(group.getId());
            wordValues.put("_groupid", word.getGroupId());
            wordValues.put("origin", word.getOrigin());
            wordValues.put("translation", word.getTranslation());
            long wId = word.getId();
            if (wId == 0) {
                wId = db.insertOrThrow("words", null, wordValues);
                word.setId(wId);
            } else {
                String whereClause = "_id = ?";
                String[] whereArgs = {String.valueOf(wId)};
                db.update("words", wordValues, whereClause, whereArgs);
            }
        }
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_GROUPS);
        db.execSQL(SQL_CREATE_WORDS);
        Map<String, Word[]> groups = Group.getInitialGroups();
        ContentValues contentValues = new ContentValues();
        for (Map.Entry<String, Word[]> entry : groups.entrySet()) {
            contentValues.put("name", entry.getKey());
            long groupid = db.insertOrThrow("groups", null, contentValues);

            for (Word word : entry.getValue()) {
                ContentValues wordContentValues = new ContentValues();
                wordContentValues.put("_groupid", groupid);
                wordContentValues.put("origin", word.getOrigin());
                wordContentValues.put("translation", word.getTranslation());
                db.insertOrThrow("words", null, wordContentValues);
            }
        }
    }

    public void deleteWord(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        String whereClause = "_id = ?";
        db.delete("words", whereClause, whereArgs);
    }

    public void insertOrUpdateWord(long groupid, Word word) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues wordValues = new ContentValues();
        if (TextUtils.isEmpty(word.getOrigin()) || TextUtils.isEmpty(word.getTranslation())) {
            return;
        }
        word.setGroupId(groupid);
        wordValues.put("_groupid", word.getGroupId());
        wordValues.put("origin", word.getOrigin());
        wordValues.put("translation", word.getTranslation());
        long wId = word.getId();
        if (wId == 0) {
            wId = db.insertOrThrow("words", null, wordValues);
            word.setId(wId);
        } else {
            String whereClause = "_id = ?";
            String[] whereArgs = {String.valueOf(wId)};
            db.update("words", wordValues, whereClause, whereArgs);
        }
    }

    public void deleteGroup(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String[] whereArgs = {String.valueOf(id)};
        db.delete("words", "_groupid = ?", whereArgs);
        db.delete("groups", "_id = ?", whereArgs);
    }

    public void deleteGroupAsync(final Context context, final long id) {
        final Database db = new Database(context);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    db.deleteGroup(id);
                } catch (SQLException e) {
                    Log.e(TAG, "Delete group error", e);
                }
                return null;
            }
        };
        task.execute();
    }
}
