// Word.java
package com.example.myapplication;
import java.io.Serializable;

public class Word implements Serializable {
    public String english;
    public String chinese;

    public Word(String english, String chinese) {
        this.english = english;
        this.chinese = chinese;
    }
}