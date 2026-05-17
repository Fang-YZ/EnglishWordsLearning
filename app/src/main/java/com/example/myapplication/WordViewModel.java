package com.example.myapplication;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordViewModel extends AndroidViewModel {

    private final WordDao wordDao;
    private final ExecutorService executorService;

    public WordViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        wordDao = db.wordDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Word>> getAllWords() {
        return wordDao.getWordsByReviewPriority();
    }

    /**
     * 根据排序类型获取单词
     * 0: 学习优先级, 1: 字母 A-Z, 2: 最新添加
     */
    public LiveData<List<Word>> getWordsSorted(int sortType) {
        switch (sortType) {
            case 1: return wordDao.getWordsAlphabetical();
            case 2: return wordDao.getWordsNewest();
            default: return wordDao.getWordsByReviewPriority();
        }
    }

    public LiveData<List<Word>> searchWords(String query) {
        return wordDao.searchWords("%" + query + "%");
    }

    public void syncFromNetwork(retrofit2.Callback<List<Word>> callback) {
        WordApiService apiService = RetrofitClient.getClient().create(WordApiService.class);
        apiService.getDailyWords().enqueue(callback);
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
}
