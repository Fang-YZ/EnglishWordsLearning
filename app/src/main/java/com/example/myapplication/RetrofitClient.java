package com.example.myapplication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // 这里的地址根据你的情况修改：
    // 1. 如果是公网服务器： "https://api.yourdomain.com/"
    // 2. 如果是本地测试（模拟器访问电脑）： "http://10.0.2.2:8080/" 
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
