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

import java.util.ArrayList;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private WordViewModel wordViewModel;
    private MediaPlayer mediaPlayer;
    private ArrayList<Word> quizList;
    private int currentIndex = 0;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化 ViewModel（共享 Activity 作用域，确保数据同步）
        wordViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(WordViewModel.class);

        if (getArguments() == null) {
            Toast.makeText(getContext(), "数据出错了", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查是单个单词模式还是连续测验模式
        quizList = (ArrayList<Word>) getArguments().getSerializable("quiz_list");
        
        Word word;
        if (quizList != null && !quizList.isEmpty()) {
            // 测验模式
            word = quizList.get(currentIndex);
            binding.tvQuizProgress.setVisibility(View.VISIBLE);
            updateQuizProgress();
        } else {
            // 单个查看模式
            word = (Word) getArguments().getSerializable("selected_word");
            binding.tvQuizProgress.setVisibility(View.GONE);
        }

        if (word == null) {
            Toast.makeText(getContext(), "找不到单词", Toast.LENGTH_SHORT).show();
            return;
        }

        // 显示单词内容
        showWord(word);

        // 点击喇叭播放读音
        binding.btnPlayAudio.setOnClickListener(v -> playAudio(binding.tvDetailEnglish.getText().toString()));

        // 【新增】点击垃圾桶图标删除当前单词
        binding.btnDeleteWord.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : word;
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("确认删除")
                    .setMessage("确定要从词库中永久删除单词 \"" + currentWord.english + "\" 吗？")
                    .setPositiveButton("删除", (d, which) -> {
                        wordViewModel.delete(currentWord);
                        Toast.makeText(getContext(), "已删除", Toast.LENGTH_SHORT).show();
                        
                        // 如果是测验模式，跳到下一个；如果是单词模式，返回列表
                        if (quizList != null && currentIndex < quizList.size() - 1) {
                            quizList.remove(currentIndex);
                            updateQuizProgress();
                            showWord(quizList.get(currentIndex));
                        } else {
                            androidx.navigation.fragment.NavHostFragment.findNavController(this).popBackStack();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        // 2. 点击“看答案”逻辑
        binding.btnShowAnswer.setOnClickListener(v -> {
            binding.tvDetailChinese.setVisibility(View.VISIBLE);
            binding.btnShowAnswer.setVisibility(View.GONE);
            binding.layoutQuizActions.setVisibility(View.VISIBLE);
        });

        // 3. 点击“认识”
        binding.btnKnow.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : word;
            if (currentWord != null) {
                currentWord.mastered = true;
                currentWord.learnCount++;
                handleWordAction(currentWord, "已标记为：认识");
            }
        });

        // 4. 点击“不认识”
        binding.btnDontKnow.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : word;
            if (currentWord != null) {
                currentWord.mastered = false;
                handleWordAction(currentWord, "已标记为：不认识");
            }
        });
    }

    /**
     * 显示单词的内容，并处理 UI 状态
     */
    private void showWord(Word word) {
        binding.tvDetailEnglish.setText(word.english);
        binding.tvDetailChinese.setText(word.chinese);
        binding.tvDetailChinese.setVisibility(View.INVISIBLE);
        binding.btnShowAnswer.setVisibility(View.VISIBLE);
        binding.layoutQuizActions.setVisibility(View.GONE);
        playAudio(word.english);
    }

    /**
     * 更新顶部进度（如 3 / 10）
     */
    private void updateQuizProgress() {
        if (quizList != null) {
            binding.tvQuizProgress.setText((currentIndex + 1) + " / " + quizList.size());
        }
    }

    /**
     * 处理单词操作并自动切换到下一个
     */
    private void handleWordAction(Word word, String toastMsg) {
        // 使用 ViewModel 更新，逻辑更专业
        wordViewModel.update(word);
        
        Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
        
        if (quizList != null && currentIndex < quizList.size() - 1) {
            currentIndex++;
            updateQuizProgress();
            showWord(quizList.get(currentIndex));
        } else {
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

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
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        binding = null;
    }
}
