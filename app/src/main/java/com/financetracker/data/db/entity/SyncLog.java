package com.financetracker.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sync_log")
public class SyncLog {

    @PrimaryKey
    @NonNull
    public String uuid;

    public String entityType;   // "transaction", "account", "category", "merchant"
    public String entityId;
    public String action;       // INSERT, UPDATE, DELETE
    public boolean synced;
    public long createdAt;

    public SyncLog() {}
}
