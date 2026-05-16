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

        // 2. 初始化侧滑删除
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
                WordAdapter adapter = (WordAdapter) binding.recyclerView.getAdapter();
                if (adapter != null) {
                    int position = viewHolder.getAdapterPosition();
                    Word wordToDelete = adapter.getWordAt(position);
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> wordDao.delete(wordToDelete));
                    android.widget.Toast.makeText(getContext(), "单词已删除", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }).attachToRecyclerView(binding.recyclerView);

        // 3. 【核心】设置搜索监听逻辑
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                // 当搜索框文字变化时，重新加载数据
                observeWords(wordDao, newText);
                return true;
            }
        });

        // 初始加载全部单词
        observeWords(wordDao, "");
    }

    /**
     * 根据搜索关键词观察数据变化
     */
    private void observeWords(WordDao wordDao, String query) {
        // 先移除旧的观察者，防止数据重叠
        wordDao.getAllWords().removeObservers(getViewLifecycleOwner());
        wordDao.searchWords("%" + query + "%").removeObservers(getViewLifecycleOwner());

        // 开启新的观察
        androidx.lifecycle.LiveData<java.util.List<Word>> liveData;
        if (query.isEmpty()) {
            // 使用复习优先算法排序：学习次数少的排在前面
            liveData = wordDao.getWordsByReviewPriority();
        } else {
            liveData = wordDao.searchWords("%" + query + "%");
        }

        liveData.observe(getViewLifecycleOwner(), wordList -> {
            if (wordList.isEmpty()) {
                if (query.isEmpty()) {
                    binding.layoutEmpty.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                }
                return;
            } else {
                binding.layoutEmpty.setVisibility(View.GONE);
                binding.recyclerView.setVisibility(View.VISIBLE);
            }

            WordAdapter adapter = new WordAdapter(wordList, new WordAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Word word) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("selected_word", word);
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                }

                @Override
                public void onItemLongClick(Word word) {
                    showEditDialog(word);
                }
            });
            binding.recyclerView.setAdapter(adapter);

            // 【新增】更新进度条逻辑
            updateProgress(wordList);

            // 【新增】随机测验逻辑
            binding.btnStartQuiz.setOnClickListener(v -> {
                // 筛选出不认识的词（红色）
                java.util.List<Word> unmasteredWords = new java.util.ArrayList<>();
                for (Word w : wordList) {
                    if (!w.mastered) unmasteredWords.add(w);
                }

                if (unmasteredWords.isEmpty()) {
                    android.widget.Toast.makeText(getContext(), "太棒了！所有单词都已掌握", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    // 随机挑出最多 10 个词组成测验集
                    java.util.Collections.shuffle(unmasteredWords);
                    int quizSize = Math.min(10, unmasteredWords.size());
                    java.util.ArrayList<Word> quizList = new java.util.ArrayList<>(unmasteredWords.subList(0, quizSize));
                    
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("quiz_list", quizList);

                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
                }
            });
        });
    }

    /**
     * 更新进度条和统计文字
     */
    private void updateProgress(java.util.List<Word> wordList) {
        int total = wordList.size();
        int mastered = 0;
        for (Word w : wordList) {
            if (w.mastered) mastered++;
        }

        binding.tvProgressText.setText("已掌握 " + mastered + " / 总词库 " + total);
        if (total > 0) {
            int progress = (mastered * 100) / total;
            binding.pbLearning.setProgress(progress);
        } else {
            binding.pbLearning.setProgress(0);
        }
    }

    private void showEditDialog(Word word) {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_word, null);
        android.widget.EditText etEnglish = dialogView.findViewById(R.id.et_english);
        android.widget.EditText etChinese = dialogView.findViewById(R.id.et_chinese);

        etEnglish.setText(word.english);
        etChinese.setText(word.chinese);

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("修改单词")
                .setView(dialogView)
                .setPositiveButton("保存", (d, which) -> {
                    String eng = etEnglish.getText().toString().trim();
                    String chi = etChinese.getText().toString().trim();
                    if (!eng.isEmpty() && !chi.isEmpty()) {
                        word.english = eng;
                        word.chinese = chi;
                        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase.getDatabase(getContext()).wordDao().update(word);
                        });
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
