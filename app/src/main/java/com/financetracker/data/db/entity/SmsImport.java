package com.financetracker.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sms_import")
public class SmsImport {

    @PrimaryKey
    @NonNull
    public String uuid;

    public String smsText;
    public double amount;
    public String detectedType;   // EXPENSE or INCOME
    public long date;
    public String accountId;      // user assigns in review queue
    public String categoryId;     // user assigns in review queue
    public String merchantName;   // extracted merchant/payee name from SMS
    public String status;         // PENDING, CONFIRMED, IGNORED
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public SmsImport() {}
}
