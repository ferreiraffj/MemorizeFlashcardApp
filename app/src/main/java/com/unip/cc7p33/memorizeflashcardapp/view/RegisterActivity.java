package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextNome, editTextEmail, editTextSenha;
    private Button btnRegistrar;
    private ProgressBar progressBar;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextNome = findViewById(R.id.edit_text_nome_registro);
        editTextEmail = findViewById(R.id.edit_text_email_registro);
        editTextSenha = findViewById(R.id.edit_text_senha_registro);
        btnRegistrar = findViewById(R.id.btn_registrar);
        progressBar = findViewById(R.id.progress_bar_registro);

        // Instancia a classe de serviço
        authService = new AuthService();

        // Configura o evento de clique do botão de registro
        btnRegistrar.setOnClickListener(v -> {
            String nome = editTextNome.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String senha = editTextSenha.getText().toString().trim();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Exibe o ProgressBar e desabilita o botão
            progressBar.setVisibility(View.VISIBLE);
            btnRegistrar.setEnabled(false);

            // Chama o metodo registerUser da AuthService
            authService.registerUser(email, senha, nome, new AuthService.AuthCallback() {
                @Override
                public void onSuccess(Usuario usuario) {
                    progressBar.setVisibility(View.GONE);
                    btnRegistrar.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Registro realizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Redireciona para a tela principal (MainActivity)
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish(); // Fecha esta Activity para evitar que o usuário volte
                }

                @Override
                public void onFailure(String errorMessage) {
                    progressBar.setVisibility(View.GONE);
                    btnRegistrar.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Erro: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        });

    }
}