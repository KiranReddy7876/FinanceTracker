package com.financetracker.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey
    @NonNull
    public String uuid;

    public String name;
    public String parentId;    // null = top-level category
    public String type;        // EXPENSE, INCOME
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Category() {}

    public Category(@NonNull String uuid, String name, String type) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deleted = false;
    }
}
