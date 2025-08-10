package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextSenha;
    private Button btnLogin;
    private TextView textViewEsqueciSenha, textViewRegistrarAgora;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.edit_text_email_login);
        editTextSenha = findViewById(R.id.edit_text_senha_login);
        btnLogin = findViewById(R.id.btn_login);
        textViewEsqueciSenha = findViewById(R.id.text_view_esqueci_senha);
        textViewRegistrarAgora = findViewById(R.id.text_view_registrar_agora);
        progressBar = findViewById(R.id.progress_bar_login);

        authService = new AuthService();

        // Redireciona para a tela principal se o usuário já estiver logado
//        if (authService.getCurrentUser() != null) {
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//        }

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

                        // Redireciona para a tela principal
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
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

        // Evento de clique para ir para a tela de registro
        textViewRegistrarAgora.setOnClickListener(vi -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Evento de clique para ir para a tela de reset de senha
        textViewEsqueciSenha.setOnClickListener(vie -> {
            startActivity(new Intent(LoginActivity.this, ResetActivity.class));
        });

    }
}