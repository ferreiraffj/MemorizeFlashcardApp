package com.unip.cc7p33.memorizeflashcardapp.service;

import static java.util.UUID.randomUUID;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.unip.cc7p33.memorizeflashcardapp.App;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.BaralhoDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.BackupDTO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.BaralhoComCartas;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID; // <--- GARANTA QUE ESTE IMPORT EXISTA


public class ProfileService {

    private final BaralhoDAO baralhoDAO;
    private final BaralhoService baralhoService;
    private final FlashcardService flashcardService;
    private final ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson;

    // --- INTERFACES DE CALLBACK ---

    // 1. Callback para operações simples (como a exportação)
    public interface ProfileCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // 2. Callback mais complexo para o processo de importação
    public interface ImportCallback {
        void onConflictsFound(List<ImportConflict> conflicts);
        void onImportFinished(String message);
        void onFailure(String errorMessage);
    }

    public static class ImportConflict {
        public final BaralhoComCartas baralhoDoBackup;
        public final Baralho baralhoExistente;

        public ImportConflict(BaralhoComCartas baralhoDoBackup, Baralho baralhoExistente) {
            this.baralhoDoBackup = baralhoDoBackup;
            this.baralhoExistente = baralhoExistente;
        }
    }

    public ProfileService(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.baralhoDAO = db.baralhoDAO();
        this.baralhoService = new BaralhoService();
        this.baralhoService.setBaralhoDAO(db.baralhoDAO());
        this.flashcardService = new FlashcardService();
        this.flashcardService.setFlashcardDAO(db.flashcardDAO());
        this.executor = Executors.newSingleThreadExecutor();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void exportarDados(String userId, Uri uri, ProfileCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Busca todos os baralhos e suas cartas do Room
                List<BaralhoComCartas> dados = baralhoDAO.getBaralhosComCartas(userId);

                // 2. Monta o objeto de backup
                BackupDTO backup = new BackupDTO();
                backup.usuarioId = userId;
                backup.dataExportacao = new Date().getTime();
                backup.baralhos = dados;

                // 3. Converte para JSON e escreve no arquivo
                try (OutputStream outputStream = App.getContext().getContentResolver().openOutputStream(uri);
                     OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                    gson.toJson(backup, writer);
                }

                mainHandler.post(() -> callback.onSuccess("Dados exportados com sucesso!"));

            } catch (Exception e) {
                Log.e("ProfileService", "Erro ao exportar dados", e);
                mainHandler.post(() -> callback.onFailure("Erro ao exportar: " + e.getMessage()));
            }
        });
    }

    public void importarDados(String userId, Uri uri, ImportCallback callback) {
        executor.execute(() -> {
            try {
                // 1. Lê o arquivo JSON
                BackupDTO backup = lerBackupDoUri(uri);
                if (backup == null || backup.baralhos == null || backup.baralhos.isEmpty()) {
                    mainHandler.post(() -> callback.onFailure("Arquivo de backup inválido ou vazio."));
                    return;
                }

                // 2. Busca os baralhos existentes no banco de dados
                List<Baralho> baralhosLocais = baralhoDAO.getAllDecksSync(userId);
                List<ImportConflict> conflitos = new ArrayList<>();
                List<BaralhoComCartas> baralhosSemConflito = new ArrayList<>();

                // 3. Compara os baralhos do backup com os locais para encontrar conflitos
                for (BaralhoComCartas baralhoDoBackup : backup.baralhos) {
                    Baralho conflitoEncontrado = null;
                    for (Baralho baralhoLocal : baralhosLocais) {
                        if (baralhoLocal.getNome().equalsIgnoreCase(baralhoDoBackup.baralho.getNome())) {
                            conflitoEncontrado = baralhoLocal;
                            break;
                        }
                    }

                    if (conflitoEncontrado != null) {
                        conflitos.add(new ImportConflict(baralhoDoBackup, conflitoEncontrado));
                    } else {
                        baralhosSemConflito.add(baralhoDoBackup);
                    }
                }

                // 4. Salva imediatamente os baralhos que não têm conflito
                for (BaralhoComCartas bcc : baralhosSemConflito) {
                    salvarBaralhoComoNovo(userId, bcc);
                }

                // 5. Retorna a lista de conflitos para a Activity resolver
                if (!conflitos.isEmpty()) {
                    mainHandler.post(() -> callback.onConflictsFound(conflitos));
                } else {
                    mainHandler.post(() -> callback.onImportFinished("Importação concluída com sucesso!"));
                }

            } catch (Exception e) {
                Log.e("ProfileService", "Erro ao importar dados", e);
                mainHandler.post(() -> callback.onFailure("Erro ao ler o arquivo: " + e.getMessage()));
            }
        });
    }

    // Metodo auxiliar para ler o JSON
    private BackupDTO lerBackupDoUri(Uri uri) throws Exception {
        try (InputStream inputStream = App.getContext().getContentResolver().openInputStream(uri);
             InputStreamReader reader = new InputStreamReader(inputStream)) {
            return gson.fromJson(reader, BackupDTO.class);
        }
    }

    /**
     * Salva o baralho do backup como uma cópia, gerando novos IDs para tudo.
     */
    public void salvarBaralhoComoNovo(String userId, BaralhoComCartas bcc) {
        executor.execute(() -> {
            Baralho baralhoOriginal = bcc.baralho;
            String novoIdDoBaralho = randomUUID().toString();
            baralhoOriginal.setBaralhoId(novoIdDoBaralho);
            baralhoOriginal.setUsuarioId(userId);

            baralhoService.criarBaralho(baralhoOriginal, userId, new BaralhoService.OnCompleteListener<Baralho>() {
                @Override
                public void onSuccess(Baralho result) {
                    if (bcc.flashcards != null) {
                        for (Flashcard carta : bcc.flashcards) {
                            carta.setFlashcardId(randomUUID().toString());
                            carta.setDeckId(novoIdDoBaralho);
                            carta.setUserId(userId);
                            flashcardService.adicionarCarta(userId, novoIdDoBaralho, carta, new FlashcardService.OnCompleteListener<Flashcard>() {
                                @Override public void onSuccess(Flashcard result) {}
                                @Override public void onFailure(Exception e) {}
                            });
                        }
                    }
                }
                @Override public void onFailure(Exception e) {}
            });
        });
    }

    /**
     * Sobrescreve um baralho existente com os dados do backup.
     */
    public void sobrescreverBaralho(String userId, BaralhoComCartas bccDoBackup, Baralho baralhoParaSobrescrever) {
        executor.execute(() -> {
            // Pega o ID do baralho existente
            String idExistente = baralhoParaSobrescrever.getBaralhoId();

            // Pega o objeto do backup e ajusta seus IDs
            Baralho baralhoDoBackup = bccDoBackup.baralho;
            baralhoDoBackup.setBaralhoId(idExistente);
            baralhoDoBackup.setUsuarioId(userId);

            // Atualiza o baralho (Room e Firebase)
            baralhoService.updateBaralhos(baralhoDoBackup, new BaralhoService.OnCompleteListener<Baralho>() {
                @Override
                public void onSuccess(Baralho result) {
                    // Deleta todas as cartas antigas deste baralho
                    flashcardService.deletarTodasAsCartasDoBaralho(userId, idExistente, () -> {
                        // Após deletar, insere as novas cartas do backup
                        if (bccDoBackup.flashcards != null) {
                            for (Flashcard cartaDoBackup : bccDoBackup.flashcards) {
                                cartaDoBackup.setFlashcardId(randomUUID().toString());
                                cartaDoBackup.setDeckId(idExistente);
                                cartaDoBackup.setUserId(userId);
                                flashcardService.adicionarCarta(userId, idExistente, cartaDoBackup, new FlashcardService.OnCompleteListener<Flashcard>() {
                                    @Override public void onSuccess(Flashcard result) {}
                                    @Override public void onFailure(Exception e) {}
                                });
                            }
                        }
                    });
                }
                @Override public void onFailure(Exception e) {}
            });
        });
    }
}
