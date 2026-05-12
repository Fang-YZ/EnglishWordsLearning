package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 获取传递来的单词，并增加【防御性检查】防止闪退
        if (getArguments() == null) {
            Toast.makeText(getContext(), "数据出错了", Toast.LENGTH_SHORT).show();
            return;
        }

        Word word = (Word) getArguments().getSerializable("selected_word");
        
        if (word == null) {
            Toast.makeText(getContext(), "找不到该单词", Toast.LENGTH_SHORT).show();
            return;
        }

        // 确保 word 不为空后再设置文字
        binding.tvDetailEnglish.setText(word.english);
        binding.tvDetailChinese.setText(word.chinese);

        // 2. 点击“看答案”逻辑
        binding.btnShowAnswer.setOnClickListener(v -> {
            // 显示中文
            binding.tvDetailChinese.setVisibility(View.VISIBLE);
            // 隐藏当前按钮
            binding.btnShowAnswer.setVisibility(View.GONE);
            // 显示“认识/不认识”按钮组
            binding.layoutQuizActions.setVisibility(View.VISIBLE);
        });

        // 3. 点击“认识”
        binding.btnKnow.setOnClickListener(v -> {
            word.mastered = true; // 记录进度
            Toast.makeText(getContext(), "太棒了！记住了 +1", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack(); // 返回列表
        });

        // 4. 点击“不认识”
        binding.btnDontKnow.setOnClickListener(v -> {
            word.mastered = false;
            Toast.makeText(getContext(), "没关系，再接再厉！", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack(); // 返回列表
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
