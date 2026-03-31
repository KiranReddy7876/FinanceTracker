package com.financetracker.data.db.entity;

import androidx.room.Embedded;

/**
 * Represents an Account with its current calculated balance
 * Current Balance = Opening Balance + Total Income - Total Expenses
 */
public class AccountWithBalance {
    @Embedded
    public Account account;
    
    public double totalIncome;
    public double totalExpense;
    
    public AccountWithBalance() {}
    
    public AccountWithBalance(Account account, double totalIncome, double totalExpense) {
        this.account = account;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }
    
    /**
     * Calculate the current balance of the account
     * Formula: Current Balance is now stored directly in the account
     */
    public double getCurrentBalance() {
        return account.currentBalance;
    }
    
    /**
     * Get the account name
     */
    public String getName() {
        return account.name;
    }
    
    /**
     * Get the account type
     */
    public String getType() {
        return account.type;
    }
    
    /**
     * Get the account UUID
     */
    public String getUuid() {
        return account.uuid;
    }
}

