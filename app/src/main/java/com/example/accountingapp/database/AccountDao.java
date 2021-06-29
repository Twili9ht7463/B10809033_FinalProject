package com.example.accountingapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AccountDao {

    @Query("SELECT * FROM account ORDER BY level")
    List<AccountEntry> loadAllAccounts();

    @Insert
    void insertAccount(AccountEntry accountEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateAccount(AccountEntry accountEntry);

    @Delete
    void deleteAccount(AccountEntry accountEntry);

    @Query("SELECT * FROM account WHERE id = :id")
    AccountEntry loadAccountById(int id);
}
