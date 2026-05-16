package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private WordViewModel wordViewModel;
    private WordAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 初始化 ViewModel
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        // 2. 初始化 RecyclerView 和 Adapter
        adapter = new WordAdapter(new WordAdapter.OnItemClickListener() {
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

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);

        // 3. 配置侧滑删除
        setupSwipeToDelete();

        // 4. 配置搜索
        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                observeWords(newText);
                return true;
            }
        });

        // 5. 随机测验逻辑
        binding.btnStartQuiz.setOnClickListener(v -> startQuiz());

        // 初始加载全部单词
        observeWords("");
    }

    private void observeWords(String query) {
        // ViewModel 会处理数据的加载，我们只需“观察”
        wordViewModel.getAllWords().removeObservers(getViewLifecycleOwner());
        wordViewModel.searchWords(query).removeObservers(getViewLifecycleOwner());

        if (query.isEmpty()) {
            wordViewModel.getAllWords().observe(getViewLifecycleOwner(), this::updateUI);
        } else {
            wordViewModel.searchWords(query).observe(getViewLifecycleOwner(), this::updateUI);
        }
    }

    private void updateUI(List<Word> wordList) {
        if (wordList.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
        }
        
        // ListAdapter 的核心：通过 submitList 提交新数据，它会自动做差异对比
        adapter.submitList(wordList);
        updateProgress(wordList);
    }

    private void startQuiz() {
        List<Word> currentList = adapter.getCurrentList();
        List<Word> unmasteredWords = new ArrayList<>();
        for (Word w : currentList) {
            if (!w.mastered) unmasteredWords.add(w);
        }

        if (unmasteredWords.isEmpty()) {
            Toast.makeText(getContext(), "所有单词都已掌握！", Toast.LENGTH_SHORT).show();
        } else {
            Collections.shuffle(unmasteredWords);
            int quizSize = Math.min(10, unmasteredWords.size());
            ArrayList<Word> quizList = new ArrayList<>(unmasteredWords.subList(0, quizSize));
            
            Bundle bundle = new Bundle();
            bundle.putSerializable("quiz_list", quizList);
            NavHostFragment.findNavController(this).navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
        }
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Word wordToDelete = adapter.getWordAt(position);
                wordViewModel.delete(wordToDelete);
                Toast.makeText(getContext(), "已删除: " + wordToDelete.english, Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(binding.recyclerView);
    }

    private void updateProgress(List<Word> wordList) {
        int total = wordList.size();
        int mastered = 0;
        for (Word w : wordList) {
            if (w.mastered) mastered++;
        }
        binding.tvProgressText.setText("已掌握 " + mastered + " / 总词库 " + total);
        if (total > 0) {
            binding.pbLearning.setProgress((mastered * 100) / total);
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
                        wordViewModel.update(word);
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
