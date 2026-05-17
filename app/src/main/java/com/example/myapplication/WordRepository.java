package com.example.myapplication;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Callback;

/**
 * 仓库类 (Repository)：单例模式
 * 负责协调所有数据的存取，确保全局只有一个线程池
 */
public class WordRepository {

    private static volatile WordRepository INSTANCE;
    private final WordDao wordDao;
    private final WordApiService apiService;
    private final ExecutorService executorService;

    private WordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        wordDao = db.wordDao();
        apiService = RetrofitClient.getClient().create(WordApiService.class);
        // 全局共享一个线程池，防止线程过多导致系统卡顿
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static WordRepository getInstance(Application application) {
        if (INSTANCE == null) {
            synchronized (WordRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WordRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    // --- 数据方法 ---

    public LiveData<List<Word>> getWordsSorted(int sortType) {
        switch (sortType) {
            case 1: return wordDao.getWordsAlphabetical();
            case 2: return wordDao.getWordsNewest();
            case 3: return wordDao.getDueReviewWords(System.currentTimeMillis());
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

    public void syncFromNetwork(Callback<List<Word>> callback) {
        apiService.getDailyWords().enqueue(callback);
    }

    public ExecutorService getExecutor() {
        return executorService;
    }
}
