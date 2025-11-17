package com.unip.cc7p33.memorizeflashcardapp.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
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
    private FlashcardService flashcardService;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public BaralhoService() {
        db = FirebaseFirestore.getInstance();
    }

    public void setBaralhoDAO(BaralhoDAO dao) {
        this.baralhoDAO = dao;
    }

    public void setFlashcardService(FlashcardService service) {
        this.flashcardService = service;
    }

    public void getBaralhos(String userId, OnCompleteListener<List<Baralho>> listener) {
        if (baralhoDAO == null) {
            listener.onFailure(new Exception("DAO não configurado. Não é possível operar offline."));
            return;
        }

        db.collection("users").document(userId).collection("baralhos").get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d("BaralhoService", "Sucesso ao buscar baralhos da nuvem.");
                List<Baralho> remotos = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    remotos.add(doc.toObject(Baralho.class));
                }
                executorService.execute(() -> {
                    baralhoDAO.insertAll(remotos);
                    Log.d("BaralhoService", remotos.size() + " baralhos sincronizados com o Room.");
                    mainHandler.post(() -> listener.onSuccess(remotos));
                });
            })
            .addOnFailureListener(e -> {
                Log.w("BaralhoService", "Falha ao buscar da nuvem. Carregando do cache local.", e);
                executorService.execute(() -> {
                    try {
                        List<Baralho> locais = baralhoDAO.getByUserId(userId);
                        Log.d("BaralhoService", "Carregados " + (locais != null ? locais.size() : 0) + " baralhos do cache.");
                        mainHandler.post(() -> listener.onSuccess(locais != null ? locais : new ArrayList<>()));
                    } catch (Exception dbEx) {
                        Log.e("BaralhoService", "Erro ao carregar do cache local.", dbEx);
                        mainHandler.post(() -> listener.onFailure(dbEx));
                    }
                });
            });
    }

    public void criarBaralho(Baralho baralho, String userId, OnCompleteListener<Baralho> listener) {
        baralho.setUsuarioId(userId);
        if (baralho.getBaralhoId().isEmpty()) {
            baralho.setBaralhoId(UUID.randomUUID().toString());
        }
        if (baralhoDAO != null) {
            executorService.execute(() -> {
                try {
                    baralhoDAO.insert(baralho);
                    mainHandler.post(() -> listener.onSuccess(baralho));
                    db.collection("users").document(userId).collection("baralhos")
                        .document(baralho.getBaralhoId()).set(baralho)
                        .addOnSuccessListener(aVoid -> Log.d("BaralhoService", "Baralho '" + baralho.getNome() + "' sincronizado com a nuvem."))
                        .addOnFailureListener(e -> Log.w("BaralhoService", "Falha ao sincronizar baralho '" + baralho.getNome() + "'. Será tentado na próxima inicialização.", e));
                } catch (Exception e) {
                    mainHandler.post(() -> listener.onFailure(e));
                }
            });
        } else {
            listener.onFailure(new Exception("DAO não configurado"));
        }
    }

    public void updateBaralhos(Baralho baralho, OnCompleteListener<Baralho> listener) {
        if (baralhoDAO == null) {
            mainHandler.post(() -> listener.onFailure(new IllegalStateException("DAO não configurado.")));
            return;
        }
        executorService.execute(() -> {
            try {
                baralhoDAO.update(baralho);
                mainHandler.post(() -> listener.onSuccess(baralho));
                db.collection("users").document(baralho.getUsuarioId()).collection("baralhos")
                    .document(baralho.getBaralhoId()).set(baralho)
                    .addOnSuccessListener(aVoid -> Log.d("BaralhoService", "Baralho '" + baralho.getNome() + "' atualizado na nuvem."))
                    .addOnFailureListener(e -> Log.w("BaralhoService", "Falha ao atualizar baralho na nuvem.", e));
            } catch (Exception e) {
                mainHandler.post(() -> listener.onFailure(e));
            }
        });
    }

    public void baixarBaralhosDaNuvem(String userId, OnCompleteListener<List<Baralho>> listener) {
        db.collection("users").document(userId).collection("baralhos").get()
            .addOnSuccessListener(querySnapshot -> {
                List<Baralho> remotos = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    remotos.add(doc.toObject(Baralho.class));
                }
                if (baralhoDAO != null && !remotos.isEmpty()) {
                    executorService.execute(() -> {
                        baralhoDAO.insertAll(remotos);
                        mainHandler.post(() -> listener.onSuccess(remotos));
                    });
                } else {
                    mainHandler.post(() -> listener.onSuccess(remotos));
                }
            })
            .addOnFailureListener(e -> {
                 Log.e("BaralhoService", "Falha ao baixar baralhos da nuvem.", e);
                 mainHandler.post(() -> listener.onFailure(e));
            });
    }

    public void deleteBaralho(String userId, Baralho baralho, OnCompleteListener<Void> listener) {
        if (flashcardService == null || baralhoDAO == null) {
            mainHandler.post(() -> listener.onFailure(new IllegalStateException("Serviços ou DAO não configurados.")));
            return;
        }
        flashcardService.deletarTodasAsCartasDoBaralho(userId, baralho.getBaralhoId(), () -> {
            db.collection("users").document(userId).collection("baralhos").document(baralho.getBaralhoId()).delete()
                .addOnSuccessListener(aVoid -> {
                    executorService.execute(() -> {
                        try {
                            baralhoDAO.deleteById(baralho.getBaralhoId());
                            mainHandler.post(() -> listener.onSuccess(null));
                        } catch (Exception e) {
                            mainHandler.post(() -> listener.onFailure(e));
                        }
                    });
                })
                .addOnFailureListener(e -> mainHandler.post(() -> listener.onFailure(e)));
        });
    }

    public void updateNomeBaralho(String userId, Baralho baralho, String novoNome, OnCompleteListener<Baralho> listener) {
        if (baralhoDAO == null) {
            mainHandler.post(() -> listener.onFailure(new IllegalStateException("DAO não configurado.")));
            return;
        }
        baralho.setNome(novoNome);
        db.collection("users").document(userId).collection("baralhos").document(baralho.getBaralhoId()).update("nome", novoNome)
            .addOnSuccessListener(aVoid -> {
                executorService.execute(() -> {
                    try {
                        baralhoDAO.update(baralho);
                        mainHandler.post(() -> listener.onSuccess(baralho));
                    } catch (Exception e) {
                        mainHandler.post(() -> listener.onFailure(e));
                    }
                });
            })
            .addOnFailureListener(e -> mainHandler.post(() -> listener.onFailure(e)));
    }

    public Task<Void> incrementarContagem(String userId, String deckId) {
        return db.collection("users").document(userId).collection("baralhos").document(deckId).update("quantidadeCartas", FieldValue.increment(1));
    }

    public Task<Void> decrementarContagem(String userId, String deckId) {
        final DocumentReference deckRef = db.collection("users").document(userId).collection("baralhos").document(deckId);
        return db.runTransaction(transaction -> {
            Baralho baralho = transaction.get(deckRef).toObject(Baralho.class);
            if (baralho != null) {
                long currentCount = baralho.getQuantidadeCartas();
                if (currentCount > 0) {
                    transaction.update(deckRef, "quantidadeCartas", currentCount - 1);
                }
            }
            return null;
        });
    }

    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}