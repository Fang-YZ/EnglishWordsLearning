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

    // 艾宾浩斯：获取今日待复习的单词 (下次复习时间小于等于现在)
    @Query("SELECT * FROM word_table WHERE mastered = 0 AND nextReviewTime <= :currentTime ORDER BY nextReviewTime ASC")
    androidx.lifecycle.LiveData<List<Word>> getDueReviewWords(long currentTime);
    
    // 艾宾浩斯：获取所有待复习词的数量
    @Query("SELECT COUNT(*) FROM word_table WHERE mastered = 0 AND nextReviewTime <= :currentTime")
    int getDueReviewCount(long currentTime);

    // 字母排序
    @Query("SELECT * FROM word_table ORDER BY english ASC")
    androidx.lifecycle.LiveData<List<Word>> getWordsAlphabetical();

    // 最新添加排序
    @Query("SELECT * FROM word_table ORDER BY id DESC")
    androidx.lifecycle.LiveData<List<Word>> getWordsNewest();

    @Query("DELETE FROM word_table")
    void deleteAll();
}
