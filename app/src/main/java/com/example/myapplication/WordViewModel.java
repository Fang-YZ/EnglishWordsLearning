package com.example.myapplication;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.List;
import retrofit2.Callback;

/**
 * ViewModel 升级版：通过 Repository 获取数据
 * 遵循 MVVM 架构
 */
public class WordViewModel extends AndroidViewModel {

    private final WordRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> sortOrder = new MutableLiveData<>(0);
    private final LiveData<List<Word>> words;

    public WordViewModel(@NonNull Application application) {
        super(application);
        repository = new WordRepository(application);
        
        // 核心优化：动态监听搜索和排序的变化
        words = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.isEmpty()) {
                return Transformations.switchMap(sortOrder, order -> 
                    repository.getWordsSorted(order)
                );
            } else {
                return repository.searchWords(query);
            }
        });
    }

    public LiveData<List<Word>> getAllWords() {
        return words;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSortOrder(int order) {
        sortOrder.setValue(order);
    }

    public void insert(Word word) {
        repository.insert(word);
    }

    public void update(Word word) {
        repository.update(word);
    }

    public void delete(Word word) {
        repository.delete(word);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public void syncFromNetwork(Callback<List<Word>> callback) {
        repository.syncFromNetwork(callback);
    }
}
