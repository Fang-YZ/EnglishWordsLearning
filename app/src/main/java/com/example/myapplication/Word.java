package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * 单词数据模型（Room 实体）
 * @Entity 告诉 Room 这个类对应数据库里的一张表
 */
@Entity(tableName = "word_table")
public class Word implements Serializable {
    
    @PrimaryKey(autoGenerate = true)
    public int id; // 数据库主键，自动递增

    public String english;
    public String chinese;
    public boolean mastered = false;
    public int learnCount = 0;
    public int reviewStage = 0; // 新增：复习阶段 (0-7)
    public long nextReviewTime = 0; // 新增：下次复习的时间戳 (毫秒)

    public Word(String english, String chinese) {
        this.english = english;
        this.chinese = chinese;
    }
}
