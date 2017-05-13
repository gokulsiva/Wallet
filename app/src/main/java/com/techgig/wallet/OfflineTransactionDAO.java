package com.techgig.wallet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GokulSiva on 13-05-2017.
 */

public class OfflineTransactionDAO {


    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
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

    public void login() {
        open();
        database.execSQL(MySQLiteHelper.DATABASE_CREATE);
        close();
    }

    public void logout() {
        open();
        database.execSQL("DROP TABLE IF EXISTS " + MySQLiteHelper.TABLE_COMMENTS);
        close();
    }


    public OfflineTransaction createOfflineTransaction(String senderId, String senderPassword, String receiverId, String amount){

        ContentValues values = new ContentValues();
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

    public List<OfflineTransaction> getAllOfflineTransactions() {
        List<OfflineTransaction> offlineTransactions = new ArrayList<OfflineTransaction>();

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
        return offlineTransactions;
    }

    private OfflineTransaction cursorToOfflineTransaction(Cursor cursor){
        OfflineTransaction transaction = new OfflineTransaction();
        transaction.setId(cursor.getLong(0));
        transaction.setSenderId(cursor.getString(1));
        transaction.setSenderPassword(cursor.getString(2));
        transaction.setReceiverId(cursor.getString(3));
        transaction.setAmount(cursor.getLong(4));
        return transaction;
    }
}
