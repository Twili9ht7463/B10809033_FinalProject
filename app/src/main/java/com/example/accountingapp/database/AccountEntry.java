package com.example.accountingapp.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "account")
public class AccountEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String amount;
    private int level;
    private String description;
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @Ignore
    public AccountEntry(String amount, int level, String description, Date updatedAt) {
        this.amount = amount;
        this.level = level;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public AccountEntry(int id, String amount, int level, String description, Date updatedAt) {
        this.id = id;
        this.amount = amount;
        this.level = level;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
