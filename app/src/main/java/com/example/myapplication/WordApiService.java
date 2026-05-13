package com.example.myapplication;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface WordApiService {
    // 假设这是一个返回每日单词列表的接口
    // 这里使用一个示例地址，实际开发时替换为真实的 API 地址
    @GET("daily_words.json") 
    Call<List<Word>> getDailyWords();
}
