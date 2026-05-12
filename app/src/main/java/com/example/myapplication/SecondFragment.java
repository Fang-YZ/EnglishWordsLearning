package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

        // 1. 接收从 FirstFragment 传递过来的数据
        if (getArguments() != null) {
            Word selectedWord = (Word) getArguments().getSerializable("selected_word");
            
            if (selectedWord != null) {
                // 设置界面显示的英文单词
                binding.tvDetailEnglish.setText(selectedWord.english);
                // 设置隐藏的中文释义
                binding.tvDetailChinese.setText(selectedWord.chinese);
            }
        }

        // 2. “显示答案”按钮的点击逻辑
        binding.buttonSecond.setOnClickListener(v -> {
            // 如果中文释义还没显示，说明是第一次点击
            if (binding.tvDetailChinese.getVisibility() != View.VISIBLE) {
                // 执行第一步：让中文释义变为可见
                binding.tvDetailChinese.setVisibility(View.VISIBLE);
                // 修改按钮文字，引导用户下一步操作
                binding.buttonSecond.setText("记住了，返回列表");
            } else {
                // 如果中文已经显示了，说明是第二次点击
                // 执行第二步：利用导航控制器返回上一页（列表页）
                androidx.navigation.fragment.NavHostFragment.findNavController(SecondFragment.this)
                        .popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
