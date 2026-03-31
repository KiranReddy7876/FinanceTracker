package com.financetracker.data.db;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.util.Log;

/**
 * Centralized migration registry for database schema changes
 * Add new migrations here and they will be automatically registered
 */
public class DatabaseMigrations {
    private static final String TAG = "DatabaseMigrations";

    /**
     * Migration: Add transferToAccountId to sms_import table
     * Supports TRANSFER type transactions (credit card payments, cash transfers)
     */
    public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(TAG, "Executing migration: v8 → v9 (Add transferToAccountId to sms_import)");
            try {
                database.execSQL(
                    "ALTER TABLE sms_import ADD COLUMN transferToAccountId TEXT"
                );
                Log.d(TAG, "✓ Migration v8→v9 completed - data preserved");
            } catch (Exception e) {
                Log.e(TAG, "✗ Migration v8→v9 failed: " + e.getMessage(), e);
                throw new RuntimeException("Migration v8→v9 failed", e);
            }
        }
    };

    /**
     * Migration: Add transfer type fields to transactions table
     * Supports: transferType (SELF, LOAN_OUT, SETTLE_PAYMENT, GIFT)
     * Supports: recipientName (for friend transfers)
     */
    public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d(TAG, "Executing migration: v9 → v10 (Add transfer type fields)");
            try {
                // Add transferType field (SELF, LOAN_OUT, SETTLE_PAYMENT, GIFT)
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN transferType TEXT"
                );
                Log.d(TAG, "  ✓ Added transferType column");
                
                // Add recipientName field (friend's name for friend transfers)
                database.execSQL(
                    "ALTER TABLE transactions ADD COLUMN recipientName TEXT"
                );
                Log.d(TAG, "  ✓ Added recipientName column");
                
                Log.d(TAG, "✓ Migration v9→v10 completed - data preserved");
            } catch (Exception e) {
                Log.e(TAG, "✗ Migration v9→v10 failed: " + e.getMessage(), e);
                throw new RuntimeException("Migration v9→v10 failed", e);
            }
        }
    };

    /**
     * Add future migrations here following the same pattern
     * Example:
     * public static final Migration MIGRATION_9_10 = new Migration(9, 10) {
     *     @Override
     *     public void migrate(SupportSQLiteDatabase database) {
     *         database.execSQL("ALTER TABLE accounts ADD COLUMN newField TEXT");
     *     }
     * };
     */

    /**
     * Get all migrations as an array
     * Add new migrations here automatically
     */
    public static Migration[] getAllMigrations() {
        return new Migration[]{
            MIGRATION_8_9,
            MIGRATION_9_10,
            // Add MIGRATION_10_11 here when created
            // etc...
        };
    }

    /**
     * Helper method to create a simple column addition migration
     * Usage: createAddColumnMigration(9, 10, "accounts", "newField", "TEXT")
     */
    public static class MigrationBuilder {
        private final int fromVersion;
        private final int toVersion;
        private final String tableName;
        private final String columnName;
        private final String columnType;
        private String defaultValue;

        public MigrationBuilder(int fromVersion, int toVersion, String tableName, 
                               String columnName, String columnType) {
            this.fromVersion = fromVersion;
            this.toVersion = toVersion;
            this.tableName = tableName;
            this.columnName = columnName;
            this.columnType = columnType;
        }

        public MigrationBuilder withDefault(String value) {
            this.defaultValue = value;
            return this;
        }

        public Migration build() {
            return new Migration(fromVersion, toVersion) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    Log.d(TAG, "Executing migration: v" + fromVersion + " → v" + toVersion);
                    try {
                        String sql = "ALTER TABLE " + tableName + 
                                   " ADD COLUMN " + columnName + " " + columnType;
                        if (defaultValue != null) {
                            sql += " DEFAULT '" + defaultValue + "'";
                        }
                        database.execSQL(sql);
                        Log.d(TAG, "✓ Migration v" + fromVersion + "→v" + toVersion + 
                             " completed - added column: " + columnName);
                    } catch (Exception e) {
                        Log.e(TAG, "✗ Migration v" + fromVersion + "→v" + toVersion + 
                             " failed: " + e.getMessage(), e);
                        throw new RuntimeException("Migration failed", e);
                    }
                }
            };
        }
    }
}

