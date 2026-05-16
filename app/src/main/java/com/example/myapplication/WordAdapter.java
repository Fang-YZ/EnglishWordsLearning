package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.databinding.ItemWordBinding;

/**
 * 升级版适配器：使用 ListAdapter 和 DiffUtil
 * ListAdapter 会自动处理数据的差异对比，只刷新变动的条目，并自带动画。
 */
public class WordAdapter extends ListAdapter<Word, WordAdapter.WordViewHolder> {

    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Word word);
        void onItemLongClick(Word word);
    }

    // DiffUtil.ItemCallback 是对比算法的核心：告诉系统如何判断两个单词是否相同
    private static final DiffUtil.ItemCallback<Word> DIFF_CALLBACK = new DiffUtil.ItemCallback<Word>() {
        @Override
        public boolean areItemsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
            // 如果 ID 相同，说明是同一个单词条目
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Word oldItem, @NonNull Word newItem) {
            // 如果内容（拼写、翻译、掌握状态）都一样，说明完全不需要刷新
            return oldItem.english.equals(newItem.english) &&
                    oldItem.chinese.equals(newItem.chinese) &&
                    oldItem.mastered == newItem.mastered;
        }
    };

    public WordAdapter(OnItemClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWordBinding binding = ItemWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        // 使用 getItem(position) 拿到当前数据
        Word word = getItem(position);

        holder.binding.tvEnglish.setText(word.english);

        // 颜色逻辑
        if (word.mastered) {
            holder.binding.tvEnglish.setTextColor(Color.parseColor("#4CAF50")); // 绿色
        } else {
            holder.binding.tvEnglish.setTextColor(Color.parseColor("#F44336")); // 红色
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(word);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onItemLongClick(word);
            return true;
        });
    }

    // 依然保留这个方法供侧滑删除使用
    public Word getWordAt(int position) {
        return getItem(position);
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        final ItemWordBinding binding;
        WordViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
