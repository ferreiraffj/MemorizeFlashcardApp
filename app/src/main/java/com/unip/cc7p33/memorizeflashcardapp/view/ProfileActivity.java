package com.unip.cc7p33.memorizeflashcardapp.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.ProfileService; // Importar
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Executors;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName, tvProfileEmail;
    private Button btnImport, btnExport;
    private AuthService authService;
    private UsuarioDAO usuarioDAO;
    private ProfileService profileService; // Novo serviço
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;
    private Queue<ProfileService.ImportConflict> conflitosQueue; // Fila para resolver conflitos um por um

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        SystemUIUtils.setImmersiveMode(this);

        // 1. Configura a Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Ação do botão de voltar
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2. Inicializa os serviços e DAO
        // Inicializa os serviços e DAO
        authService = new AuthService(this);
        usuarioDAO = AppDatabase.getInstance(this).usuarioDAO();
        profileService = new ProfileService(this); // Inicializa o novo serviço
        conflitosQueue = new LinkedList<>(); // Inicializa a fila

        // Conecta componentes da UI
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        btnImport = findViewById(R.id.btn_import);
        btnExport = findViewById(R.id.btn_export);

        // Carrega dados do usuário
        loadUserProfile();

        // Configura os launchers de arquivo
        setupFileLaunchers();

        // Configura os listeners dos botões
        btnImport.setOnClickListener(v -> openFilePickerForImport());
        btnExport.setOnClickListener(v -> createFileForExport());
    }

    private void setupFileLaunchers() {
        // Launcher para EXPORTAÇÃO (cria um arquivo)
        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String userId = authService.getCurrentUser().getUid();
                            profileService.exportarDados(userId, uri, new ProfileService.ProfileCallback() {
                                @Override public void onSuccess(String message) { Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show(); }
                                @Override public void onFailure(String errorMessage) { Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show(); }
                            });
                        }
                    }
                });

        // Launcher para IMPORTAÇÃO (abre um arquivo)
        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            String userId = authService.getCurrentUser().getUid();

                            // Usa o novo callback
                            profileService.importarDados(userId, uri, new ProfileService.ImportCallback() {
                                @Override
                                public void onConflictsFound(List<ProfileService.ImportConflict> conflicts) {
                                    Toast.makeText(ProfileActivity.this, "Conflitos encontrados! Resolva um por um.", Toast.LENGTH_SHORT).show();
                                    conflitosQueue.clear();
                                    conflitosQueue.addAll(conflicts);
                                    resolverProximoConflito(); // Inicia o processo de resolução
                                }

                                @Override
                                public void onImportFinished(String message) {
                                    Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
    }
    private void resolverProximoConflito() {
        if (conflitosQueue.isEmpty()) {
            Toast.makeText(this, "Todos os conflitos foram resolvidos. Importação finalizada!", Toast.LENGTH_LONG).show();
            return;
        }

        ProfileService.ImportConflict conflito = conflitosQueue.poll(); // Pega o próximo conflito da fila
        if (conflito == null) return;

        String userId = authService.getCurrentUser().getUid();

        // Cria o AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_import_conflict, null);
        TextView message = dialogView.findViewById(R.id.tv_conflict_message);
        message.setText("Um baralho chamado '" + conflito.baralhoExistente.getNome() + "' já existe. O que você deseja fazer?");

        builder.setView(dialogView)
                .setTitle("Conflito de Importação")
                .setPositiveButton("Sobrescrever", (dialog, id) -> {
                    profileService.sobrescreverBaralho(userId, conflito.baralhoDoBackup, conflito.baralhoExistente);
                    resolverProximoConflito(); // Chama para o próximo
                })
                .setNeutralButton("Manter Ambos", (dialog, id) -> {
                    profileService.salvarBaralhoComoNovo(userId, conflito.baralhoDoBackup);
                    resolverProximoConflito(); // Chama para o próximo
                })
                .setNegativeButton("Ignorar", (dialog, id) -> {
                    Toast.makeText(this, "Baralho '" + conflito.baralhoExistente.getNome() + "' ignorado.", Toast.LENGTH_SHORT).show();
                    resolverProximoConflito(); // Chama para o próximo
                })
                .setCancelable(false) // Força o usuário a escolher uma opção
                .show();
    }

    private void createFileForExport() {
        String dataFormatada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String nomeArquivo = "memorize_backup_" + dataFormatada + ".json";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, nomeArquivo);

        exportLauncher.launch(intent);
    }

    private void openFilePickerForImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");

        importLauncher.launch(intent);
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
