package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;

public class ResetActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button btnResetSenha;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        editTextEmail = findViewById(R.id.edit_text_email_reset);
        btnResetSenha = findViewById(R.id.btn_reset_senha);
        progressBar = findViewById(R.id.progress_bar_reset);

        authService = new AuthService();

        btnResetSenha.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(ResetActivity.this, "Digite seu email", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            btnResetSenha.setEnabled(false);

            authService.resetPassword(email, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(Usuario usuario) {
                    progressBar.setVisibility(View.GONE);
                    btnResetSenha.setEnabled(true);
                    Toast.makeText(ResetActivity.this, "Um link de recuperação foi enviado para o seu email.", Toast.LENGTH_LONG).show();
                    finish(); // Volta para a tela de login
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnResetSenha.setEnabled(true);
                    Toast.makeText(ResetActivity.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}