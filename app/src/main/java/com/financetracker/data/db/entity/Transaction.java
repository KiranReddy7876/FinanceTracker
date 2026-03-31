package com.financetracker.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey
    @NonNull
    public String uuid;

    public String accountId;
    public String type;          // EXPENSE, INCOME, TRANSFER
    public double amount;
    public long date;
    public String categoryId;
    public String merchantId;
    public String note;
    public String referenceId;   // for bank statement duplicate detection
    public String transferToAccountId; // used when type = TRANSFER and SELF transfer
    public String transferType;  // "SELF", "LOAN_OUT", "SETTLE_PAYMENT", "GIFT"
    public String recipientName; // for friend transfers: name of friend
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Transaction() {}

    public Transaction(@NonNull String uuid, String accountId, String type,
                       double amount, long date) {
        this.uuid = uuid;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deleted = false;
    }
}
