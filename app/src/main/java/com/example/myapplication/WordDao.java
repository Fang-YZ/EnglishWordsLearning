package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface WordDao {

    @Insert
    void insert(Word word);

    @Update
    void update(Word word);

    @Delete
    void delete(Word word);

    @Query("SELECT * FROM word_table ORDER BY id DESC")
    androidx.lifecycle.LiveData<List<Word>> getAllWords();

    @Query("SELECT * FROM word_table WHERE english LIKE :searchQuery OR chinese LIKE :searchQuery ORDER BY id DESC")
    androidx.lifecycle.LiveData<List<Word>> searchWords(String searchQuery);

    // 简易算法：优先获取学习次数少的单词
    @Query("SELECT * FROM word_table ORDER BY learnCount ASC, id DESC")
    androidx.lifecycle.LiveData<List<Word>> getWordsByReviewPriority();
}
