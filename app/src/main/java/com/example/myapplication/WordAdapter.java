package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.databinding.ItemWordBinding;
import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private List<Word> words;
    private OnItemClickListener listener;

    // 定义点击接口
    public interface OnItemClickListener {
        void onItemClick(Word word);
    }

    public WordAdapter(List<Word> words, OnItemClickListener listener) {
        this.words = words;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 加载 item_word.xml 布局
        ItemWordBinding binding = ItemWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = words.get(position);
        holder.binding.tvEnglish.setText(word.english); // 设置单词文本

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> listener.onItemClick(word));
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        ItemWordBinding binding;
        WordViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}