package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.List;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 准备演示用的单词数据
        List<Word> wordList = new ArrayList<>();
        wordList.add(new Word("Commit", "提交 (把代码保存到仓库)"));
        wordList.add(new Word("Variable", "变量 (存储数据的容器)"));
        wordList.add(new Word("Interface", "接口 (连接不同部分的桥梁)"));
        wordList.add(new Word("Fragment", "碎片 (页面的一部分)"));
        wordList.add(new Word("Adapter", "适配器 (连接数据和视图的中间人)"));

        // 2. 初始化适配器
        WordAdapter adapter = new WordAdapter(wordList, word -> {
            // 3. 点击逻辑：将选中的单词对象封装到 Bundle 中
            Bundle bundle = new Bundle();
            bundle.putSerializable("selected_word", word);

            // 4. 跳转到第二个页面 (SecondFragment)，并带上数据
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_SecondFragment, bundle);
        });

        // 5. 配置 RecyclerView (设置布局管理器和适配器)
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
