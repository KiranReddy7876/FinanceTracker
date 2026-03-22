package com.financetracker.data.db;

import android.content.Context;
import android.util.Log;

/**
 * Utility to ensure database is properly initialized with tables and seed data
 */
public class DatabaseInitializer {
    private static final String TAG = "DatabaseInitializer";
    private static boolean initialized = false;

    public static void initialize(Context context) {
        if (initialized) {
            Log.d(TAG, "Database already initialized");
            return;
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "===== Starting database initialization =====");
                
                // Step 1: Get Room database instance
                // Room will handle table creation and versioning
                Log.d(TAG, "Step 1: Getting Room database instance...");
                AppDatabase db = AppDatabase.getInstance(context);
                Log.d(TAG, "Step 1: Database instance obtained");
                
                // Give database time to stabilize
                Thread.sleep(1000);
                
                // Step 2: Verify database is working
                Log.d(TAG, "Step 2: Verifying database access...");
                try {
                    int accountCount = db.accountDao().getCount();
                    Log.d(TAG, "SUCCESS: Database is working. Current account count: " + accountCount);
                    
                    // If no accounts, seed the database
                    if (accountCount == 0) {
                        Log.d(TAG, "Step 3: No accounts found. Seeding default data...");
                        DatabaseSeeder.seedDefaults(db);
                        
                        // Wait for seeding to complete
                        Thread.sleep(2000);
                        
                        int newCount = db.accountDao().getCount();
                        Log.d(TAG, "After seeding, account count: " + newCount);
                        
                        if (newCount > 0) {
                            Log.d(TAG, "SUCCESS: Database seeding completed with " + newCount + " accounts");
                        } else {
                            Log.w(TAG, "WARNING: Seeding did not work - no accounts created, will try on next access");
                        }
                    } else {
                        Log.d(TAG, "Database already has data: " + accountCount + " accounts");
                    }
                    
                    Log.d(TAG, "===== Database initialization COMPLETED =====");
                    initialized = true;
                    
                } catch (Exception e) {
                    Log.e(TAG, "ERROR: Cannot access database", e);
                    e.printStackTrace();
                }
                
            } catch (InterruptedException e) {
                Log.e(TAG, "Database initialization interrupted", e);
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "ERROR during database initialization", e);
                e.printStackTrace();
            }
        }, "DatabaseInitializer-Thread").start();
    }
}
