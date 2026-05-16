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

        // 2. 初始化侧滑删除（只初始化一次，放在 observe 外面）
        new androidx.recyclerview.widget.ItemTouchHelper(new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0,
                androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder,
                                  @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                // 通过适配器拿到被滑动的单词
                WordAdapter adapter = (WordAdapter) binding.recyclerView.getAdapter();
                if (adapter != null) {
                    int position = viewHolder.getAdapterPosition();
                    Word wordToDelete = adapter.getWordAt(position);

                    // 执行数据库删除
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        wordDao.delete(wordToDelete);
                    });
                    android.widget.Toast.makeText(getContext(), "单词已删除", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(binding.recyclerView);

        // 3. 【核心】观察数据库的变化
        wordDao.getAllWords().observe(getViewLifecycleOwner(), wordList -> {
            if (wordList.isEmpty()) {
                java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                    wordDao.insert(new Word("Commit", "提交"));
                    wordDao.insert(new Word("Variable", "变量"));
                    wordDao.insert(new Word("Adapter", "适配器"));
                });
            }

            // 刷新列表界面
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
