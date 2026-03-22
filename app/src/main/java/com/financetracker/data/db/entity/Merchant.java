package com.financetracker.data.db.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "merchants")
public class Merchant {

    @PrimaryKey
    @NonNull
    public String uuid = "";

    public String name;
    public String nickName;
    public String categoryId;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

    public Merchant() {}

    public Merchant(@NonNull String uuid, String name, String categoryId) {
        this.uuid = uuid;
        this.name = name;
        this.nickName = null;
        this.categoryId = categoryId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deleted = false;
    }
}
