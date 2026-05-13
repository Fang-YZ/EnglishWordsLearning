package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase db = AppDatabase.getDatabase(getContext());
        WordDao wordDao = db.wordDao();

        // 1. 设置 RecyclerView 的基本外观
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 2. 【核心】观察数据库的变化
        // 只要 word_table 表里的数据一变，这个里面的代码就会自动执行！
        wordDao.getAllWords().observe(getViewLifecycleOwner(), wordList -> {
            // 如果数据库空空如也，我们还是在后台偷偷塞入几个演示单词
            if (wordList.isEmpty()) {
                java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    wordDao.insert(new Word("Commit", "提交"));
                    wordDao.insert(new Word("Variable", "变量"));
                    wordDao.insert(new Word("Adapter", "适配器"));
                });
                return; // 插入后 LiveData 会再次触发，所以这里直接返回
            }

            // 3. 刷新列表界面
            WordAdapter adapter = new WordAdapter(wordList, word -> {
                Bundle bundle = new Bundle();
                bundle.putSerializable("selected_word", word);
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
            });
            binding.recyclerView.setAdapter(adapter);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
