// Word.java
package com.example.myapplication;
import java.io.Serializable;

public class Word implements Serializable {
    public String english;
    public String chinese;
    public boolean mastered = false; // 新增：是否已掌握，默认是 false

    public Word(String english, String chinese) {
        this.english = english;
        this.chinese = chinese;
    }
}