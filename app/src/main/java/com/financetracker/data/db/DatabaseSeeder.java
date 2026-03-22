package com.financetracker.data.db;

import android.util.Log;
import com.financetracker.data.db.entity.Account;
import com.financetracker.data.db.entity.Category;
import java.util.UUID;
import java.util.concurrent.Executors;

public class DatabaseSeeder {
    private static final String TAG = "DatabaseSeeder";

    public static void seedDefaults(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                int accountCount = db.accountDao().getCount();
                Log.d(TAG, "Current account count: " + accountCount);
                
                if (accountCount > 0) {
                    Log.d(TAG, "Database already seeded, skipping seed");
                    return;
                }

                Log.d(TAG, "Seeding default data...");

                // Default accounts
                db.accountDao().insert(new Account(UUID.randomUUID().toString(), "Cash", "CASH", 0.0, "INR"));
                db.accountDao().insert(new Account(UUID.randomUUID().toString(), "Bank Account", "BANK", 0.0, "INR"));
                db.accountDao().insert(new Account(UUID.randomUUID().toString(), "Credit Card", "CREDIT_CARD", 0.0, "INR"));
                Log.d(TAG, "Default accounts created");

                // Default expense categories
                String[] expenseCategories = {
                    "Food & Dining", "Transportation", "Shopping", "Entertainment",
                    "Bills & Utilities", "Health & Medical", "Education", "Personal Care",
                    "Travel", "Groceries", "Rent", "Insurance", "Investments", "Gifts", "Other"
                };
                for (String name : expenseCategories) {
                    Category c = new Category(UUID.randomUUID().toString(), name, "EXPENSE");
                    db.categoryDao().insert(c);
                }
                Log.d(TAG, "Expense categories created: " + expenseCategories.length);

                // Default income categories
                String[] incomeCategories = {"Salary", "Freelance", "Business", "Investment", "Rental", "Other Income"};
                for (String name : incomeCategories) {
                    Category c = new Category(UUID.randomUUID().toString(), name, "INCOME");
                    db.categoryDao().insert(c);
                }
                Log.d(TAG, "Income categories created: " + incomeCategories.length);
                Log.d(TAG, "Database seeding completed successfully");
                
            } catch (Exception e) {
                Log.e(TAG, "Error seeding database", e);
                e.printStackTrace();
            }
        });
    }
}
