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

        // 显示初始单词
        showWord(word);

        // 点击喇叭播放读音
        binding.btnPlayAudio.setOnClickListener(v -> playAudio(binding.tvDetailEnglish.getText().toString()));

        // 2. 点击“看答案”逻辑
        binding.btnShowAnswer.setOnClickListener(v -> {
            binding.tvDetailChinese.setVisibility(View.VISIBLE);
            binding.btnShowAnswer.setVisibility(View.GONE);
            binding.layoutQuizActions.setVisibility(View.VISIBLE);
        });

        // 3. 点击“认识”
        binding.btnKnow.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : (Word) getArguments().getSerializable("selected_word");
            if (currentWord != null) {
                currentWord.mastered = true;
                currentWord.learnCount++;
                handleWordAction(currentWord, "已标记为：认识");
            }
        });

        // 4. 点击“不认识”
        binding.btnDontKnow.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : (Word) getArguments().getSerializable("selected_word");
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
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getDatabase(getContext()).wordDao().update(word);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
                    
                    if (quizList != null && currentIndex < quizList.size() - 1) {
                        currentIndex++;
                        updateQuizProgress();
                        showWord(quizList.get(currentIndex));
                    } else {
                        NavHostFragment.findNavController(this).popBackStack();
                    }
                });
            }
        });
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
