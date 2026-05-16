package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT COUNT(*) FROM user_table")
    int getUserCount();

    @Query("SELECT * FROM user_table WHERE username = :username LIMIT 1")
    User findUserByUsername(String username);
}
