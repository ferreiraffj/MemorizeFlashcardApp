package com.unip.cc7p33.memorizeflashcardapp.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardService {

    private final FirebaseFirestore db;
    private FlashcardDAO flashcardDAO;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public FlashcardService() {
        db = FirebaseFirestore.getInstance();
    }

    public void setFlashcardDAO(FlashcardDAO dao) {
        this.flashcardDAO = dao;
    }

    public void adicionarCarta(String userId, String deckId, Flashcard card, OnCompleteListener<Flashcard> listener) {
        if (flashcardDAO == null) {
            listener.onFailure(new Exception("DAO não configurado."));
            return;
        }

        // 1. Gera ID único localmente
        if (card.getFlashcardId() == null || card.getFlashcardId().isEmpty()) {
            card.setFlashcardId(UUID.randomUUID().toString());
        }
        card.setDeckId(deckId);
        card.setUserId(userId); // Garante que o ID do usuário está na carta

        executorService.execute(() -> {
            try {
                // 2. Salva no banco de dados local (Room)
                flashcardDAO.insert(card);
                Log.d("FlashcardService", "Carta salva no Room com ID: " + card.getFlashcardId());

                // 3. Notifica a UI IMEDIATAMENTE
                mainHandler.post(() -> listener.onSuccess(card));

                // 4. Tenta sincronizar com a nuvem em segundo plano
                db.collection("users").document(userId)
                  .collection("baralhos").document(deckId)
                  .collection("flashcards").document(card.getFlashcardId())
                  .set(card)
                  .addOnSuccessListener(aVoid -> Log.d("FlashcardService", "Carta '" + card.getFrente() + "' sincronizada com a nuvem."))
                  .addOnFailureListener(e -> Log.w("FlashcardService", "Falha ao sincronizar carta. Será tentado na próxima vez.", e));

            } catch (Exception e) {
                Log.e("FlashcardService", "Erro ao salvar carta no Room", e);
                mainHandler.post(() -> listener.onFailure(e));
            }
        });
    }

    public void updateCarta(String userId, String deckId, Flashcard card, OnCompleteListener<Flashcard> listener) {
        if (flashcardDAO != null) {
            executorService.execute(() -> flashcardDAO.update(card));
        }

        db.collection("users").document(userId)
                .collection("baralhos").document(deckId)
                .collection("flashcards").document(card.getFlashcardId())
                .set(card)
                .addOnSuccessListener(aVoid -> mainHandler.post(() -> listener.onSuccess(card)))
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro ao atualizar carta no Firestore", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void deletarCarta(String userId, String deckId, String cardId, OnCompleteListener<Void> listener) {
        if (flashcardDAO != null) {
             executorService.execute(() -> flashcardDAO.deleteByCardId(cardId)); // Assumindo que você tem ou criará este método no DAO
        }

        db.collection("users").document(userId)
                .collection("baralhos").document(deckId)
                .collection("flashcards").document(cardId)
                .delete()
                .addOnSuccessListener(aVoid -> mainHandler.post(() -> listener.onSuccess(null)))
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro ao deletar carta no Firestore", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void syncExistingDataToRoom(String userId, Runnable onComplete) {
        if (flashcardDAO == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        db.collection("users").document(userId).collection("baralhos").get()
            .addOnSuccessListener(decksSnapshot -> {
                if (decksSnapshot.isEmpty()) {
                    if (onComplete != null) onComplete.run();
                    return;
                }
                for (var deckDoc : decksSnapshot.getDocuments()) {
                    deckDoc.getReference().collection("flashcards").get()
                        .addOnSuccessListener(cardsSnapshot -> {
                            if (cardsSnapshot.isEmpty()) return;
                            executorService.execute(() -> {
                                List<Flashcard> cardsToInsert = new ArrayList<>();
                                for (var cardDoc : cardsSnapshot.getDocuments()) {
                                    Flashcard card = cardDoc.toObject(Flashcard.class);
                                    if (card != null && card.getFlashcardId() != null) {
                                        cardsToInsert.add(card);
                                    }
                                }
                                flashcardDAO.insertAll(cardsToInsert);
                            });
                        });
                }
                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FlashcardService", "Erro na sincronização de baralhos", e);
                if (onComplete != null) mainHandler.post(onComplete);
            });
    }

    public void fetchAndSaveCardsFromDeck(String userId, String deckId, OnCompleteListener<List<Flashcard>> listener) {
        if (flashcardDAO == null) {
            listener.onFailure(new IllegalStateException("FlashcardDAO não foi inicializado."));
            return;
        }

        db.collection("users").document(userId)
                .collection("baralhos").document(deckId)
                .collection("flashcards").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Flashcard> firestoreCards = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Flashcard card = doc.toObject(Flashcard.class);
                        if (card != null) {
                            firestoreCards.add(card);
                        }
                    }
                    executorService.execute(() -> {
                        flashcardDAO.insertAll(firestoreCards);
                        mainHandler.post(() -> listener.onSuccess(firestoreCards));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro ao buscar cartas do Firestore", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void deletarTodasAsCartasDoBaralho(String userId, String deckId, Runnable onComplete) {
        db.collection("users").document(userId)
                .collection("baralhos").document(deckId)
                .collection("flashcards").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    }
                });

        if (flashcardDAO != null) {
            executorService.execute(() -> {
                flashcardDAO.deleteByDeckId(deckId);
                mainHandler.post(onComplete);
            });
        } else {
            mainHandler.post(onComplete);
        }
    }

    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}