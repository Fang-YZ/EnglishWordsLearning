package com.example.myapplication;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.List;
import java.util.concurrent.ExecutorService;
import retrofit2.Callback;

/**
 * ViewModel：管理全局状态，共享数据源
 */
public class WordViewModel extends AndroidViewModel {

    private final WordRepository repository;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> sortOrder = new MutableLiveData<>(0);
    private final LiveData<List<Word>> words;

    public WordViewModel(@NonNull Application application) {
        super(application);
        // 使用单例仓库
        repository = WordRepository.getInstance(application);
        
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
    
    public ExecutorService getExecutor() {
        return repository.getExecutor();
    }
}
