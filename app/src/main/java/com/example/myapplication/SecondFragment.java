package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.media.AudioAttributes;

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

    private MediaPlayer mediaPlayer;

    @Override
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

        // 【新增】点击喇叭图标播放读音
        binding.btnPlayAudio.setOnClickListener(v -> playAudio(word.english));
        
        // 进入页面自动播放一次
        playAudio(word.english);

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
            word.mastered = true; 
            word.learnCount++; // 学习次数 +1
            
            // 后台更新数据库
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getDatabase(getContext()).wordDao().update(word);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "已标记为：认识", Toast.LENGTH_SHORT).show();
                        androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
                    });
                }
            });
        });

        // 4. 点击“不认识”
        binding.btnDontKnow.setOnClickListener(v -> {
            word.mastered = false;

            // 后台更新数据库
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getDatabase(getContext()).wordDao().update(word);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "已标记为：不认识", Toast.LENGTH_SHORT).show();
                        androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
                    });
                }
            });
        });
    }

    /**
     * 调用有道 API 播放单词读音
     */
    private void playAudio(String wordText) {
        String audioUrl = "https://dict.youdao.com/dictvoice?audio=" + wordText + "&type=1";
        
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync(); // 异步准备，不卡界面
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(getContext(), "播放失败，请检查网络", Toast.LENGTH_SHORT).show();
                return true;
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 释放播放器资源
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        binding = null;
    }
}
