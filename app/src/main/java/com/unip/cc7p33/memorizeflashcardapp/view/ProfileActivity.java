package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;

import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private Button btnImport, btnExport;
    private AuthService authService;
    private UsuarioDAO usuarioDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 1. Configura a Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Ação do botão de voltar
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2. Inicializa os serviços e DAO
        authService = new AuthService(this);
        usuarioDAO = AppDatabase.getInstance(this).usuarioDAO();

        // 3. Conecta os componentes da UI
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        btnImport = findViewById(R.id.btn_import);
        btnExport = findViewById(R.id.btn_export);

        // 4. Carrega os dados do usuário
        loadUserProfile();

        // 5. Configura os listeners dos botões (por enquanto, com placeholders)
        btnImport.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade de Importar em desenvolvimento!", Toast.LENGTH_SHORT).show();
            // Aqui chamaremos a lógica para abrir o seletor de arquivos
        });

        btnExport.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade de Exportar em desenvolvimento!", Toast.LENGTH_SHORT).show();
            // Aqui chamaremos a lógica para criar e salvar o arquivo de backup
        });
    }

    private void loadUserProfile() {
        if (authService.getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = authService.getCurrentUser().getUid();

        // Busca o usuário no banco de dados local (Room)
        Executors.newSingleThreadExecutor().execute(() -> {
            Usuario usuario = usuarioDAO.getUserByUID(userId);
            runOnUiThread(() -> {
                if (usuario != null) {
                    tvProfileName.setText(usuario.getNome());
                    tvProfileEmail.setText(usuario.getEmail());
                } else {
                    // Fallback, caso não encontre no Room (raro, mas seguro)
                    tvProfileName.setText("Não encontrado");
                    tvProfileEmail.setText(authService.getCurrentUser().getEmail());
                }
            });
        });
    }
}
