package com.example.myapplication;

import android.graphics.Color;
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
        // 新增：长按接口
        void onItemLongClick(Word word);
    }

    public WordAdapter(List<Word> words, OnItemClickListener listener) {
        this.words = words;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // A. 加载 item_word.xml 布局并生成 Binding 对象
        ItemWordBinding binding = ItemWordBinding.inflate(
                LayoutInflater.from(parent.getContext()), // 获取加载器的上下文
                parent,                                   // 指定父容器
                false                                     // 不要立即挂载到父容器上
        );

        // B. 将生成的 Binding 对象交给“握持者（ViewHolder）”并返回
        return new WordViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        // A. 根据当前位置（position）从列表中拿到具体的单词数据对象
        Word word = words.get(position);

        // B. 将数据填充到 ViewBinding 提供的控件中
        holder.binding.tvEnglish.setText(word.english); 

        // 逻辑更新：根据掌握情况区分颜色
        if (word.mastered) {
            holder.binding.tvEnglish.setTextColor(Color.parseColor("#4CAF50")); // 认识的词：绿色
        } else {
            holder.binding.tvEnglish.setTextColor(Color.parseColor("#F44336")); // 不认识/新词：红色
        }

        // C. 为整个条目（ItemView）设置点击监听
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // 当这一行被点击时，通知外界（Fragment）哪个单词被选中了
                listener.onItemClick(word);
            }
        });

        // 新增：长按监听
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(word);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        // 告诉系统：数据列表里有多少个单词，界面就显示多少行
        return words != null ? words.size() : 0;
    }

    // 新增：让外界能根据位置拿到单词对象
    public Word getWordAt(int position) {
        return words.get(position);
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        ItemWordBinding binding;
        WordViewHolder(ItemWordBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}