package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppDatabase db = AppDatabase.getDatabase(getContext());
        UserDao userDao = db.userDao();

        // 初始化管理员账号
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            if (userDao.getUserCount() == 0) {
                userDao.insert(new User("root", "123456"));
            }
        });

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(getContext(), "请输入账号密码", Toast.LENGTH_SHORT).show();
                return;
            }

            // 后台验证登录
            java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
                User user = userDao.login(username, password);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (user != null) {
                            Toast.makeText(getContext(), "登录成功！欢迎 " + user.username, Toast.LENGTH_SHORT).show();
                            // 跳转到主列表页
                            NavHostFragment.findNavController(this).navigate(R.id.action_LoginFragment_to_FirstFragment);
                        } else {
                            Toast.makeText(getContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });

        // 跳转到注册页
        binding.tvGoToRegister.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_LoginFragment_to_RegisterFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
