package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.myapplication.databinding.ActivityMainBinding;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private WordViewModel wordViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        wordViewModel = new ViewModelProvider(this).get(WordViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setSupportActionBar(binding.toolbar);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }

        binding.fab.setOnClickListener(view -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_word, null);
            EditText etEnglish = dialogView.findViewById(R.id.et_english);
            EditText etChinese = dialogView.findViewById(R.id.et_chinese);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("添加新单词")
                    .setView(dialogView)
                    .setPositiveButton("添加", (d, which) -> {
                        String english = etEnglish.getText().toString().trim();
                        String chinese = etChinese.getText().toString().trim();
                        if (!english.isEmpty() && !chinese.isEmpty()) {
                            wordViewModel.insert(new Word(english, chinese));
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create();

            dialog.setOnShowListener(d -> {
                etEnglish.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(etEnglish, InputMethodManager.SHOW_IMPLICIT);
                }
            });

            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }

            dialog.show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        return super.onOptionsItemSelected(item);
    }

    private void syncWords() {
        WordApiService apiService = RetrofitClient.getClient().create(WordApiService.class);
        Call<List<Word>> call = apiService.getDailyWords();

        Toast.makeText(this, "正在同步...", Toast.LENGTH_SHORT).show();

        call.enqueue(new Callback<List<Word>>() {
            @Override
            public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Word> downloadedWords = response.body();
                    for (Word w : downloadedWords) {
                        wordViewModel.insert(w);
                    }
                    Toast.makeText(MainActivity.this, "同步成功！", Toast.LENGTH_SHORT).show();
                } else {
                    mockSync();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                mockSync(); 
            }
        });
    }

    private void mockSync() {
        Toast.makeText(this, "演示：正在从模拟网络下载...", Toast.LENGTH_SHORT).show();
        wordViewModel.insert(new Word("Retrofit", "一个很棒的网络库"));
        wordViewModel.insert(new Word("JSON", "数据交换格式"));
        Toast.makeText(MainActivity.this, "模拟同步成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            return NavigationUI.navigateUp(navController, appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
}
