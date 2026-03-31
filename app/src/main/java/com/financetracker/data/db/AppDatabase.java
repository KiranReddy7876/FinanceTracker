package com.financetracker.data.db;

import android.content.Context;
import android.util.Log;
import androidx.room.*;
import com.financetracker.data.db.dao.*;
import com.financetracker.data.db.entity.*;

@Database(
    entities = {
        Account.class,
        Category.class,
        Merchant.class,
        com.financetracker.data.db.entity.Transaction.class,
        SmsImport.class,
        SyncLog.class
    },
    version = 11,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final String TAG = "AppDatabase";

    public abstract AccountDao accountDao();
    public abstract CategoryDao categoryDao();
    public abstract MerchantDao merchantDao();
    public abstract TransactionDao transactionDao();
    public abstract SmsImportDao smsImportDao();
    public abstract SyncLogDao syncLogDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d(TAG, "Creating AppDatabase instance...");
                    
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "finance_tracker.db"
                    )
                    // Register all migrations from DatabaseMigrations class
                    // These preserve user data during schema updates
                    .addMigrations(DatabaseMigrations.getAllMigrations())
                    // Fallback to destructive migration only for development/untracked versions
                    // Will NOT be triggered if explicit migration exists
                    .fallbackToDestructiveMigration()
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            Log.d(TAG, "Database onCreate callback - tables created by Room");
                        }
                        
                        @Override
                        public void onOpen(@androidx.annotation.NonNull androidx.sqlite.db.SupportSQLiteDatabase db) {
                            super.onOpen(db);
                            Log.d(TAG, "Database onOpen callback - database is now open");
                        }
                    })
                    .build();
                    Log.d(TAG, "AppDatabase instance created successfully");
                }
            }
        }
        return INSTANCE;
    }
}
