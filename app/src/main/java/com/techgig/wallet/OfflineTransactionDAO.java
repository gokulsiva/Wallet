package com.techgig.wallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GokulSiva on 13-05-2017.
 */

public class OfflineTransactionDAO {


    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_USERID,
            MySQLiteHelper.COLUMN_SENDERID, MySQLiteHelper.COLUMN_SENDERPASSWORD,
            MySQLiteHelper.COLUMN_RECEIVERID, MySQLiteHelper.COLUMN_AMOUNT};

    public OfflineTransactionDAO(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public OfflineTransaction createOfflineTransaction(String userId, String senderId, String senderPassword, String receiverId, String amount){

        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_USERID, userId);
        values.put(MySQLiteHelper.COLUMN_SENDERID, senderId);
        values.put(MySQLiteHelper.COLUMN_SENDERPASSWORD, senderPassword);
        values.put(MySQLiteHelper.COLUMN_RECEIVERID, receiverId);
        values.put(MySQLiteHelper.COLUMN_AMOUNT, amount);
        long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        OfflineTransaction transaction = cursorToOfflineTransaction(cursor);
        cursor.close();
        return transaction;
    }


    public void deleteOfflineTransaction(OfflineTransaction transaction){

        long id = transaction.getId();
        System.out.println("OfflineTransaction deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_COMMENTS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);

    }

    public List<OfflineTransaction> getAllOfflineTransactions(String userId) {
        List<OfflineTransaction> offlineTransactions = new ArrayList<OfflineTransaction>();
        List<OfflineTransaction> offlineTransactions1 = new ArrayList<OfflineTransaction>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            OfflineTransaction transaction = cursorToOfflineTransaction(cursor);
            offlineTransactions.add(transaction);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        for(OfflineTransaction transaction : offlineTransactions){
            Log.v("Offline Transaction", transaction.toString());
            Log.v("Value of userId", transaction.getUserId());
            Log.v("Value of passed", userId);
            if (transaction.getUserId().equals(userId)){
                offlineTransactions1.add(transaction);
            }
        }
        return offlineTransactions1;
    }

    private OfflineTransaction cursorToOfflineTransaction(Cursor cursor){
        OfflineTransaction transaction = new OfflineTransaction();
        transaction.setId(cursor.getLong(0));
        transaction.setUserId(cursor.getString(1));
        transaction.setSenderId(cursor.getString(2));
        transaction.setSenderPassword(cursor.getString(3));
        transaction.setReceiverId(cursor.getString(4));
        transaction.setAmount(cursor.getLong(5));
        return transaction;
    }
}
