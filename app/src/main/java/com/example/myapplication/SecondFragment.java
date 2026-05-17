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
    private int correctCount = 0; // 记录认识的数量

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

        // 初始化 ViewModel
        wordViewModel = new androidx.lifecycle.ViewModelProvider(requireActivity()).get(WordViewModel.class);

        if (getArguments() == null) {
            Toast.makeText(getContext(), "数据出错了", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查模式
        quizList = (ArrayList<Word>) getArguments().getSerializable("quiz_list");
        
        Word word;
        if (quizList != null && !quizList.isEmpty()) {
            word = quizList.get(currentIndex);
            binding.tvQuizProgress.setVisibility(View.VISIBLE);
            updateQuizProgress();
        } else {
            word = (Word) getArguments().getSerializable("selected_word");
            binding.tvQuizProgress.setVisibility(View.GONE);
        }

        if (word == null) {
            Toast.makeText(getContext(), "找不到单词", Toast.LENGTH_SHORT).show();
            return;
        }

        showWord(word);

        binding.btnPlayAudio.setOnClickListener(v -> playAudio(binding.tvDetailEnglish.getText().toString()));

        binding.btnDeleteWord.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : word;
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("确认删除")
                    .setMessage("确定要删除吗？")
                    .setPositiveButton("删除", (d, which) -> {
                        wordViewModel.delete(currentWord);
                        if (quizList != null && currentIndex < quizList.size() - 1) {
                            quizList.remove(currentIndex);
                            updateQuizProgress();
                            showWord(quizList.get(currentIndex));
                        } else {
                            NavHostFragment.findNavController(this).popBackStack();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        binding.btnShowAnswer.setOnClickListener(v -> {
            binding.tvDetailChinese.setVisibility(View.VISIBLE);
            binding.btnShowAnswer.setVisibility(View.GONE);
            binding.layoutQuizActions.setVisibility(View.VISIBLE);
        });

        binding.btnKnow.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : word;
            if (currentWord != null) {
                currentWord.mastered = true;
                currentWord.learnCount++;
                if (quizList != null) correctCount++;
                handleWordAction(currentWord, "已标记为：认识");
            }
        });

        binding.btnDontKnow.setOnClickListener(v -> {
            Word currentWord = (quizList != null) ? quizList.get(currentIndex) : word;
            if (currentWord != null) {
                currentWord.mastered = false;
                handleWordAction(currentWord, "已标记为：不认识");
            }
        });
    }

    private void showWord(Word word) {
        binding.tvDetailEnglish.setText(word.english);
        binding.tvDetailChinese.setText(word.chinese);
        binding.tvDetailChinese.setVisibility(View.INVISIBLE);
        binding.btnShowAnswer.setVisibility(View.VISIBLE);
        binding.layoutQuizActions.setVisibility(View.GONE);
        playAudio(word.english);
    }

    private void updateQuizProgress() {
        if (quizList != null) {
            binding.tvQuizProgress.setText((currentIndex + 1) + " / " + quizList.size());
        }
    }

    private void handleWordAction(Word word, String toastMsg) {
        wordViewModel.update(word);
        Toast.makeText(getContext(), toastMsg, Toast.LENGTH_SHORT).show();
        
        if (quizList != null && currentIndex < quizList.size() - 1) {
            currentIndex++;
            updateQuizProgress();
            showWord(quizList.get(currentIndex));
        } else {
            if (quizList != null) {
                showResultDialog();
            } else {
                NavHostFragment.findNavController(this).popBackStack();
            }
        }
    }

    private void showResultDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_quiz_result, null);
        android.widget.TextView tvScore = dialogView.findViewById(R.id.tv_score_detail);
        int total = quizList.size();
        int percent = (total > 0) ? (correctCount * 100) / total : 0;
        tvScore.setText("正确率：" + percent + "%\n本次掌握：" + correctCount + " / " + total);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("我知道了", (d, w) -> NavHostFragment.findNavController(this).popBackStack())
                .show();
    }

    private void playAudio(String wordText) {
        String audioUrl = "https://dict.youdao.com/dictvoice?audio=" + wordText + "&type=1";
        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA).build());
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
