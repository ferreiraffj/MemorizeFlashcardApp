package com.unip.cc7p33.memorizeflashcardapp.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardService {

    private final FirebaseFirestore db;
    private FlashcardDAO flashcardDAO;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());  // Para callbacks na UI

    public FlashcardService() {
        db = FirebaseFirestore.getInstance();
    }

    public void setFlashcardDAO(FlashcardDAO dao) {
        this.flashcardDAO = dao;
    }

    public void adicionarCarta(String userId, String deckId, Flashcard card, OnCompleteListener<Flashcard> listener) {
        // Gere ID único se vazio
        if (card.getFlashcardId() == 0) {
            card.setFlashcardId(UUID.randomUUID().hashCode());  // Gera int único
        }
        card.setDeckId(deckId);  // Adicione deckId ao card (assumindo campo em Flashcard)

        if (flashcardDAO != null) {
            executorService.execute(() -> {
                try {
                    // Verifique duplicata por deck/conteúdo
                    List<Flashcard> existing = flashcardDAO.getByDeckId(deckId);
                    boolean isDuplicate = existing.stream().anyMatch(c -> c.getFrente().equals(card.getFrente()) && c.getVerso().equals(card.getVerso()));
                    if (!isDuplicate) {
                        flashcardDAO.insert(card);
                        Log.d("FlashcardService", "Carta inserida no Room: " + card.getFlashcardId());
                    }
                } catch (Exception e) {
                    Log.e("FlashcardService", "Erro ao inserir no Room", e);
                    mainHandler.post(() -> listener.onFailure(e));
                    return;
                }
            });
        }

        // Salve no Firebase
        db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .document(String.valueOf(card.getFlashcardId()))
                .set(card)
                .addOnSuccessListener(aVoid -> {
                    mainHandler.post(() -> listener.onSuccess(card));
                })
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro no Firebase", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public Task<QuerySnapshot> getCartasDoBaralho(String userId, String deckId) {
        return db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();
    }

    public void updateCarta(String userId, String deckId, Flashcard card, OnCompleteListener<Flashcard> listener) {
        if (flashcardDAO != null) {
            executorService.execute(() -> {
                try {
                    flashcardDAO.update(card);
                } catch (Exception e) {
                    Log.e("FlashcardService", "Erro ao atualizar no Room", e);
                    mainHandler.post(() -> listener.onFailure(e));
                    return;
                }
            });
        }

        db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .document(String.valueOf(card.getFlashcardId()))
                .set(card)
                .addOnSuccessListener(aVoid -> mainHandler.post(() -> listener.onSuccess(card)))
                .addOnFailureListener(e -> mainHandler.post(() -> listener.onFailure(e)));
    }

    public void deletarCarta(String userId, String deckId, String cardId, OnCompleteListener<Void> listener) {
        if (flashcardDAO != null) {
            executorService.execute(() -> {
                try {
                    Flashcard card = flashcardDAO.getById(Integer.parseInt(cardId));
                    if (card != null) {
                        flashcardDAO.delete(card);
                    }
                } catch (Exception e) {
                    Log.e("FlashcardService", "Erro ao deletar no Room", e);
                    mainHandler.post(() -> listener.onFailure(e));
                    return;
                }
            });
        }

        db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .document(cardId)
                .delete()
                .addOnSuccessListener(aVoid -> mainHandler.post(() -> listener.onSuccess(null)))
                .addOnFailureListener(e -> mainHandler.post(() -> listener.onFailure(e)));
    }

    // Melhorado: sincroniza com merge inteligente
    public void syncExistingDataToRoom(String userId, Runnable onComplete) {
        if (flashcardDAO == null) return;

        db.collection("users").document(userId).collection("decks").get()
                .addOnSuccessListener(decksSnapshot -> {
                    for (var deckDoc : decksSnapshot.getDocuments()) {
                        String deckId = deckDoc.getId();
                        db.collection("users").document(userId).collection("decks").document(deckId).collection("cards").get()
                                .addOnSuccessListener(cardsSnapshot -> {
                                    executorService.execute(() -> {
                                        for (var cardDoc : cardsSnapshot.getDocuments()) {
                                            Flashcard card = cardDoc.toObject(Flashcard.class);
                                            if (card != null) {
                                                card.setDeckId(deckId);  // Adicione deckId
                                                List<Flashcard> existing = flashcardDAO.getByDeckId(deckId);
                                                boolean isDuplicate = existing.stream().anyMatch(c -> c.getFrente().equals(card.getFrente()) && c.getVerso().equals(card.getVerso()));
                                                if (!isDuplicate) {
                                                    flashcardDAO.insert(card);
                                                }
                                            }
                                        }
                                    });
                                });
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> Log.e("FlashcardService", "Erro na sincronização", e));
    }

    public interface OnCompleteListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
}