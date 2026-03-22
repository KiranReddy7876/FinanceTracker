package com.financetracker.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {

    @PrimaryKey
    @NonNull
    public String uuid;

    public String name;
    public String type;                // CASH, BANK, CREDIT_CARD, WALLET
    public double openingBalance;
    public String currency;
    public String accountNumberLast4;  // Last 4 digits of account number (from SMS)
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Account() {}

    public Account(@NonNull String uuid, String name, String type,
                   double openingBalance, String currency) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.openingBalance = openingBalance;
        this.currency = currency;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deleted = false;
    }
}
