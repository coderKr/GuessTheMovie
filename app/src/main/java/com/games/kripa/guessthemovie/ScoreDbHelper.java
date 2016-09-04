package com.games.kripa.guessthemovie;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScoreDbHelper extends SQLiteOpenHelper {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + ScoreContract.ScoreEntry.TABLE_NAME + " (" +
                    ScoreContract.ScoreEntry.COLUMN_NAME_PLAYER_ID + " TEXT PRIMARY KEY," +
                    ScoreContract.ScoreEntry.COLUMN_NAME_SCORE + TEXT_TYPE + COMMA_SEP +
                    ScoreContract.ScoreEntry.COLUMN_NAME_TOTAL_MATCHES + TEXT_TYPE + COMMA_SEP +
                    ScoreContract.ScoreEntry.COLUMN_NAME_LOST_MATCHES+ TEXT_TYPE+
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ScoreContract.ScoreEntry.TABLE_NAME;
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ScoreReader";
    ScoreContract scores = new ScoreContract();

    public ScoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
