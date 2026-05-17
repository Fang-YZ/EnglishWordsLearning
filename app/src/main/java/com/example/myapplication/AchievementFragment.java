package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.databinding.FragmentAchievementBinding;

import java.util.List;

public class AchievementFragment extends Fragment {

    private FragmentAchievementBinding binding;
    private WordViewModel wordViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAchievementBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        // 观察数据并计算成就
        wordViewModel.getAllWords().observe(getViewLifecycleOwner(), wordList -> {
            if (wordList != null) {
                calculateAndDisplayStats(wordList);
            }
        });
    }

    private void calculateAndDisplayStats(List<Word> wordList) {
        int totalWords = wordList.size();
        int masteredWords = 0;
        int totalReviews = 0;

        for (Word word : wordList) {
            if (word.mastered) masteredWords++;
            totalReviews += word.learnCount;
        }

        binding.tvTotalWords.setText(String.valueOf(totalWords));
        binding.tvMasteredWords.setText(String.valueOf(masteredWords));
        binding.tvTotalReviews.setText(String.valueOf(totalReviews));
        
        // 动态改变激励语
        if (masteredWords > 10) {
            binding.tvMotivation.setText("已经掌握了这么多词，继续保持！");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
