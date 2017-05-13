package com.techgig.wallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by GokulSiva on 13-05-2017.
 */

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_COMMENTS = "offline_transactions";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SENDERID = "senderId";
    public static final String COLUMN_SENDERPASSWORD = "senderPassword";
    public static final String COLUMN_RECEIVERID = "receiverId";
    public static final String COLUMN_AMOUNT = "amount";

    private static final String DATABASE_NAME = "offline_transactions.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_COMMENTS + "( " + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_SENDERID + " text not null, "
            + COLUMN_SENDERPASSWORD + " text not null, "
            + COLUMN_RECEIVERID + " text not null, "
            + COLUMN_AMOUNT + " text not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        onCreate(db);
    }

}
