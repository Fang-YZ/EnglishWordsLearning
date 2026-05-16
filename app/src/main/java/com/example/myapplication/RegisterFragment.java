package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentRegisterBinding;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase db = AppDatabase.getDatabase(getContext());
        UserDao userDao = db.userDao();

        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etRegUsername.getText().toString().trim();
            String password = binding.etRegPassword.getText().toString().trim();
            String confirmPassword = binding.etRegConfirmPassword.getText().toString().trim();

            // 1. 基础验证
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(getContext(), "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. 数据库验证与插入
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                User existingUser = userDao.findUserByUsername(username);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (existingUser != null) {
                            Toast.makeText(getContext(), "该用户名已存在", Toast.LENGTH_SHORT).show();
                        } else {
                            // 执行注册
                            registerUser(userDao, username, password);
                        }
                    });
                }
            });
        });

        // 返回登录页
        binding.tvBackToLogin.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    private void registerUser(UserDao userDao, String username, String password) {
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            userDao.insert(new User(username, password));
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "注册成功！请登录", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).popBackStack();
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
