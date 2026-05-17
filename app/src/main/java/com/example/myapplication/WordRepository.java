package com.example.myapplication;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * 仓库类 (Repository)：数据的唯一调度中心
 * 负责协调数据库 (Room) 和 网络接口 (Retrofit)
 */
public class WordRepository {

    private final WordDao wordDao;
    private final WordApiService apiService;
    private final ExecutorService executorService;

    public WordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        wordDao = db.wordDao();
        apiService = RetrofitClient.getClient().create(WordApiService.class);
        executorService = Executors.newFixedThreadPool(4);
    }

    // --- 数据库操作 ---

    public LiveData<List<Word>> getWordsSorted(int sortType) {
        switch (sortType) {
            case 1: return wordDao.getWordsAlphabetical();
            case 2: return wordDao.getWordsNewest();
            case 3: return wordDao.getDueReviewWords(System.currentTimeMillis()); // 艾宾浩斯
            default: return wordDao.getWordsByReviewPriority();
        }
    }

    public LiveData<List<Word>> searchWords(String query) {
        return wordDao.searchWords("%" + query + "%");
    }

    public void insert(Word word) {
        executorService.execute(() -> wordDao.insert(word));
    }

    public void update(Word word) {
        executorService.execute(() -> wordDao.update(word));
    }

    public void delete(Word word) {
        executorService.execute(() -> wordDao.delete(word));
    }

    public void deleteAll() {
        executorService.execute(wordDao::deleteAll);
    }

    // --- 网络操作 ---

    public void syncFromNetwork(Callback<List<Word>> callback) {
        apiService.getDailyWords().enqueue(callback);
    }
}
