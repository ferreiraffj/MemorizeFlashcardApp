package com.unip.cc7p33.memorizeflashcardapp.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.NotificationWorker;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextSenha;
    private Button btnLogin;
    private TextView textViewEsqueciSenha, textViewRegistrarAgora;
    private ProgressBar progressBar;
    private AuthService authService;
    private String currentUserId;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            scheduleReviewNotification();
        } else {
            Toast.makeText(this, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show();
        }
        redirectToMain();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SystemUIUtils.setImmersiveMode(this);

        editTextEmail = findViewById(R.id.edit_text_email_login);
        editTextSenha = findViewById(R.id.edit_text_senha_login);
        btnLogin = findViewById(R.id.btn_login);
        textViewEsqueciSenha = findViewById(R.id.text_view_esqueci_senha);
        textViewRegistrarAgora = findViewById(R.id.text_view_registrar_agora);
        progressBar = findViewById(R.id.progress_bar_login);

        authService = new AuthService(this);

        if (authService.getCurrentUser() != null) {
            redirectToMain();
            finish();
        }

        btnLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);

            authService.loginUser(email, senha, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(Usuario usuario) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Bem-vindo, " + usuario.getNome() + "!", Toast.LENGTH_SHORT).show();
                        currentUserId = usuario.getUid();
                        requestNotificationPermission();
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });

        textViewRegistrarAgora.setOnClickListener(vi -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        textViewEsqueciSenha.setOnClickListener(vie -> {
            startActivity(new Intent(LoginActivity.this, ResetActivity.class));
        });

    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                scheduleReviewNotification();
                redirectToMain();
            }
        } else {
            scheduleReviewNotification();
            redirectToMain();
        }
    }

    private void scheduleReviewNotification() {
        if (currentUserId == null || currentUserId.isEmpty()) {
            return;
        }

        Data inputData = new Data.Builder()
                .putString(NotificationWorker.USER_ID_KEY, currentUserId)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 8, TimeUnit.HOURS)
                .setInputData(inputData)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork("reviewNotificationWork", ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    private void redirectToMain(){
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
