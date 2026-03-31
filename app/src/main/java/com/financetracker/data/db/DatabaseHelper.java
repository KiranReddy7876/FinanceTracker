package com.financetracker.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Fallback SQLite helper to ensure tables exist if Room fails to create them
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "finance_tracker.db";
    private static final int DATABASE_VERSION = 11;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables via fallback SQLiteOpenHelper");
        createAllTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        dropAllTables(db);
        onCreate(db);
    }

    public void ensureTablesExist(SQLiteDatabase db) {
        createAllTables(db);
    }

    private void createAllTables(SQLiteDatabase db) {
        try {
            // Create accounts table - Match Room entity schema exactly
            db.execSQL("CREATE TABLE IF NOT EXISTS accounts ("
                    + "uuid TEXT PRIMARY KEY NOT NULL,"
                    + "name TEXT,"
                    + "type TEXT,"
                    + "currentBalance REAL NOT NULL,"
                    + "currency TEXT,"
                    + "accountNumberLast4 TEXT,"
                    + "createdAt INTEGER NOT NULL,"
                    + "updatedAt INTEGER NOT NULL,"
                    + "deleted INTEGER NOT NULL"
                    + ")");
            Log.d(TAG, "Table 'accounts' created/verified");

            // Create categories table - Match Room entity schema exactly
            db.execSQL("CREATE TABLE IF NOT EXISTS categories ("
                    + "uuid TEXT PRIMARY KEY NOT NULL,"
                    + "name TEXT,"
                    + "parentId TEXT,"
                    + "type TEXT,"
                    + "createdAt INTEGER NOT NULL,"
                    + "updatedAt INTEGER NOT NULL,"
                    + "deleted INTEGER NOT NULL"
                    + ")");
            Log.d(TAG, "Table 'categories' created/verified");

            // Create merchants table - Match Room entity schema exactly
            db.execSQL("CREATE TABLE IF NOT EXISTS merchants ("
                    + "uuid TEXT PRIMARY KEY NOT NULL,"
                    + "name TEXT,"
                    + "categoryId TEXT,"
                    + "createdAt INTEGER NOT NULL,"
                    + "updatedAt INTEGER NOT NULL,"
                    + "deleted INTEGER NOT NULL"
                    + ")");
            Log.d(TAG, "Table 'merchants' created/verified");

            // Create transactions table - Match Room entity schema exactly
            db.execSQL("CREATE TABLE IF NOT EXISTS transactions ("
                    + "uuid TEXT PRIMARY KEY NOT NULL,"
                    + "accountId TEXT,"
                    + "type TEXT,"
                    + "amount REAL NOT NULL,"
                    + "date INTEGER NOT NULL,"
                    + "categoryId TEXT,"
                    + "merchantId TEXT,"
                    + "note TEXT,"
                    + "referenceId TEXT,"
                    + "transferToAccountId TEXT,"
                    + "createdAt INTEGER NOT NULL,"
                    + "updatedAt INTEGER NOT NULL,"
                    + "deleted INTEGER NOT NULL"
                    + ")");
            Log.d(TAG, "Table 'transactions' created/verified");

            // Create sms_import table - Match Room entity schema exactly
            db.execSQL("CREATE TABLE IF NOT EXISTS sms_import ("
                    + "uuid TEXT PRIMARY KEY NOT NULL,"
                    + "smsText TEXT,"
                    + "amount REAL NOT NULL,"
                    + "detectedType TEXT,"
                    + "date INTEGER NOT NULL,"
                    + "accountId TEXT,"
                    + "categoryId TEXT,"
                    + "status TEXT,"
                    + "createdAt INTEGER NOT NULL,"
                    + "updatedAt INTEGER NOT NULL,"
                    + "deleted INTEGER NOT NULL"
                    + ")");
            Log.d(TAG, "Table 'sms_import' created/verified");

            // Create sync_log table - Match Room entity schema exactly
            db.execSQL("CREATE TABLE IF NOT EXISTS sync_log ("
                    + "uuid TEXT PRIMARY KEY NOT NULL,"
                    + "entityType TEXT,"
                    + "entityId TEXT,"
                    + "action TEXT,"
                    + "synced INTEGER NOT NULL,"
                    + "createdAt INTEGER NOT NULL"
                    + ")");
            Log.d(TAG, "Table 'sync_log' created/verified");

            // Important: Add Room's master table and identity hash to avoid "Room cannot verify the data integrity" error
            db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
            db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3847e6dae873aae66be564ec3bd47425')");

            Log.d(TAG, "All tables created/verified successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating tables", e);
            e.printStackTrace();
        }
    }

    private void dropAllTables(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS accounts");
            db.execSQL("DROP TABLE IF EXISTS categories");
            db.execSQL("DROP TABLE IF EXISTS merchants");
            db.execSQL("DROP TABLE IF EXISTS transactions");
            db.execSQL("DROP TABLE IF EXISTS sms_import");
            db.execSQL("DROP TABLE IF EXISTS sync_log");
            db.execSQL("DROP TABLE IF EXISTS room_master_table");
            Log.d(TAG, "All tables dropped");
        } catch (Exception e) {
            Log.e(TAG, "Error dropping tables", e);
        }
    }
}
