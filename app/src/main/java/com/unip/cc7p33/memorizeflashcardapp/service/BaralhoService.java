package com.unip.cc7p33.memorizeflashcardapp.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import com.unip.cc7p33.memorizeflashcardapp.database.BaralhoDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaralhoService {

    private final FirebaseFirestore db;
    private BaralhoDAO baralhoDAO;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());  // Para callbacks na UI thread

    public BaralhoService() {
        db = FirebaseFirestore.getInstance();
    }

    public void setBaralhoDAO(BaralhoDAO dao) {
        this.baralhoDAO = dao;
    }

    public void criarBaralho(Baralho baralho, String userId, OnCompleteListener<Baralho> listener) {
        baralho.setUsuarioId(userId);
        // Gere ID se vazio (antes do insert no Room)
        if (baralho.getBaralhoId().isEmpty()) {
            baralho.setBaralhoId(UUID.randomUUID().toString());  // Gera ID único
        }
        if (baralhoDAO != null) {
            executorService.execute(() -> {
                try {
                    baralhoDAO.insert(baralho);  // Salva no Room
                    sincronizarBaralhoComNuvem(baralho, listener);  // Sincroniza com Firestore
                } catch (Exception e) {
                    mainHandler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new Exception("DAO não configurado"));
        }
    }

    public void updateBaralhos(Baralho baralho, OnCompleteListener<Baralho> listener) {
        if (baralhoDAO != null) {
            executorService.execute(() -> {
                try {
                    baralhoDAO.update(baralho);  // Atualiza no Room
                    sincronizarBaralhoComNuvem(baralho, listener);  // Sincroniza com Firestore
                } catch (Exception e) {
                    mainHandler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new Exception("DAO não configurado"));
        }
    }

    public void getBaralhos(String userId, OnCompleteListener<List<Baralho>> listener) {
        if (baralhoDAO != null) {
            executorService.execute(() -> {
                try {
                    List<Baralho> locais = baralhoDAO.getByUserId(userId);
                    if (locais != null && !locais.isEmpty()) {
                        mainHandler.post(() -> listener.onSuccess(locais));  // Callback na UI thread
                    } else {
                        // Fallback: busca no Firestore
                        baixarBaralhosDaNuvem(userId, listener);
                    }
                } catch (Exception e) {
                    mainHandler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            baixarBaralhosDaNuvem(userId, listener);  // Fallback direto se DAO indisponível
        }
    }

    private void sincronizarBaralhoComNuvem(Baralho baralho, OnCompleteListener<Baralho> listener) {
        // Padronize para users/{userId}/decks
        db.collection("users")
                .document(baralho.getUsuarioId())
                .collection("baralhos") // <-- CORRIGIDO
                .document(baralho.getBaralhoId())
                .set(baralho)
                .addOnSuccessListener(aVoid -> {
                    mainHandler.post(() -> listener.onSuccess(baralho));
                })
                .addOnFailureListener(e -> {
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void baixarBaralhosDaNuvem(String userId, OnCompleteListener<List<Baralho>> listener) {
        Log.d("BaralhoService", "Iniciando download e sincronização de baralhos do Firestore.");
        db.collection("users")
                .document(userId)
                .collection("baralhos") // Caminho corrigido
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Baralho> remotos = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Baralho baralho = doc.toObject(Baralho.class);
                        remotos.add(baralho);
                    }

                    // Se encontrou baralhos na nuvem, salva TODOS no Room
                    if (baralhoDAO != null && !remotos.isEmpty()) {
                        executorService.execute(() -> {
                            // Este método deve ter @Insert(onConflict = OnConflictStrategy.REPLACE)
                            baralhoDAO.insertAll(remotos);
                            Log.d("BaralhoService", remotos.size() + " baralhos sincronizados com o Room.");
                            // Retorna a lista para a UI thread
                            mainHandler.post(() -> listener.onSuccess(remotos));
                        });
                    } else {
                        // Se não encontrou nada na nuvem, retorna a lista vazia
                        mainHandler.post(() -> listener.onSuccess(remotos));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BaralhoService", "Falha ao baixar baralhos da nuvem.", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public Task<Void> incrementarContagem(String userId, String deckId) {
    return db.collection("users")
            .document(userId)
            .collection("baralhos")
            .document(deckId)
            .update("quantidadeCartas", FieldValue.increment(1));
}

    /**
     * Decrementa em 1 a contagem de cartas de um baralho, usando uma transação
     * para garantir que o valor nunca seja menor que zero.
     */
    public Task<Void> decrementarContagem(String userId, String deckId) {
        final DocumentReference deckRef = db.collection("users")
                .document(userId)
                .collection("baralhos")
                .document(deckId);

        // Roda a operação como uma transação segura
        return db.runTransaction(transaction -> {
            // 1. Lê o estado atual do baralho
            Baralho baralho = transaction.get(deckRef).toObject(Baralho.class);
            if (baralho != null) {
                long currentCount = baralho.getQuantidadeCartas();

                // 2. Verifica se a contagem é maior que 0 antes de subtrair
                if (currentCount > 0) {
                    // 3. Se for, atualiza para o novo valor
                    transaction.update(deckRef, "quantidadeCartas", currentCount - 1);
                }
            }
            // Retorna null para indicar sucesso na transação
            return null;
        });
    }

    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}