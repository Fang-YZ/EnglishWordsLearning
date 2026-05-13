package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(view -> {
            // 1. 加载自定义布局
            android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_word, null);
            android.widget.EditText etEnglish = dialogView.findViewById(R.id.et_english);
            android.widget.EditText etChinese = dialogView.findViewById(R.id.et_chinese);

            // 2. 构建对话框
            androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("添加新单词")
                    .setView(dialogView)
                    .setPositiveButton("添加", (d, which) -> {
                        String english = etEnglish.getText().toString().trim();
                        String chinese = etChinese.getText().toString().trim();
                        
                        if (!english.isEmpty() && !chinese.isEmpty()) {
                            // 后台插入数据库
                            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                                AppDatabase.getDatabase(this).wordDao().insert(new Word(english, chinese));
                            });
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create(); // 使用 create() 而不是直接 .show()

            // 3. 【核心优化】解决中文输入法不弹出的问题
            // 必须在 dialog.show() 之前或之后立即设置以下属性
            dialog.setOnShowListener(d -> {
                // 自动弹出软键盘
                etEnglish.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etEnglish, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            });

            // 强制对话框窗口允许显示输入法
            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }

            dialog.show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            syncWords();
            return true;
        }

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 使用 Retrofit 同步网络单词
     */
    private void syncWords() {
        WordApiService apiService = RetrofitClient.getClient().create(WordApiService.class);
        retrofit2.Call<java.util.List<Word>> call = apiService.getDailyWords();

        android.widget.Toast.makeText(this, "正在同步...", android.widget.Toast.LENGTH_SHORT).show();

        call.enqueue(new retrofit2.Callback<java.util.List<Word>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<Word>> call, retrofit2.Response<java.util.List<Word>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<Word> downloadedWords = response.body();
                    
                    // 后台存入数据库
                    java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                        WordDao dao = AppDatabase.getDatabase(MainActivity.this).wordDao();
                        for (Word w : downloadedWords) {
                            dao.insert(w);
                        }
                        
                        runOnUiThread(() -> {
                            android.widget.Toast.makeText(MainActivity.this, "同步成功！", android.widget.Toast.LENGTH_SHORT).show();
                        });
                    });
                } else {
                    // 由于没有真实地址，这里通常会失败
                    // 为了演示，我们在这里手动造几个“模拟下载”的单词
                    mockSync();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<Word>> call, Throwable t) {
                // 网络连接失败时
                mockSync(); 
            }
        });
    }

    /**
     * 模拟同步成功（因为演示环境没有真实 API 接口）
     */
    private void mockSync() {
        android.widget.Toast.makeText(this, "演示：正在从模拟网络下载...", android.widget.Toast.LENGTH_SHORT).show();
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            WordDao dao = AppDatabase.getDatabase(MainActivity.this).wordDao();
            dao.insert(new Word("Retrofit", "一个很棒的网络库"));
            dao.insert(new Word("JSON", "数据交换格式"));
            
            runOnUiThread(() -> {
                android.widget.Toast.makeText(MainActivity.this, "模拟同步成功！", android.widget.Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        NavController navController = navHostFragment.getNavController();
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}