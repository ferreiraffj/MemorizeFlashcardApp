package com.unip.cc7p33.memorizeflashcardapp.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

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
    private Handler mainHandler = new Handler(Looper.getMainLooper());  // Para callbacks na UI

    public FlashcardService() {
        db = FirebaseFirestore.getInstance();
    }

    public void setFlashcardDAO(FlashcardDAO dao) {
        this.flashcardDAO = dao;
    }

    public void adicionarCarta(String userId, String deckId, Flashcard card, OnCompleteListener<Flashcard> listener) {
        // 1. Pega uma referência para um novo documento no Firestore.
        com.google.firebase.firestore.DocumentReference newCardRef = db.collection("users").document(userId)
                .collection("baralhos").document(deckId)
                .collection("flashcards").document();

        // 2. Define o ID gerado pelo Firebase no seu objeto Flashcard.
        card.setFlashcardId(newCardRef.getId());
        // Garante que o deckId também está no objeto
        card.setDeckId(deckId);

        // 3. Salva o objeto COMPLETO (já com o ID) no Firebase.
        newCardRef.set(card)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FlashcardService", "Carta salva no Firebase com ID: " + card.getFlashcardId());

                    // 4. Se o Firebase teve sucesso, salva a MESMA CARTA no Room.
                    if (flashcardDAO != null) {
                        executorService.execute(() -> {
                            try {
                                flashcardDAO.insert(card);
                                Log.d("FlashcardService", "Carta salva no Room com ID: " + card.getFlashcardId());
                                // 5. Notifica a UI que tudo deu certo.
                                mainHandler.post(() -> listener.onSuccess(card));
                            } catch (Exception e) {
                                Log.e("FlashcardService", "Erro ao inserir no Room após sucesso no Firebase", e);
                                mainHandler.post(() -> listener.onFailure(e));
                            }
                        });
                    } else {
                        mainHandler.post(() -> listener.onSuccess(card));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro ao salvar no Firebase", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void updateCarta(String userId, String deckId, Flashcard card, OnCompleteListener<Flashcard> listener) {
        // 1. Atualiza no banco de dados local (Room)
        if (flashcardDAO != null) {
            executorService.execute(() -> {
                flashcardDAO.update(card);
            });
        }

        // 2. Atualiza no Firestore usando o caminho correto
        db.collection("users").document(userId)
                // ----- CORREÇÃO AQUI -----
                .collection("baralhos").document(deckId)
                .collection("flashcards").document(card.getFlashcardId()) // Usa o ID (String) do objeto
                // -------------------------
                .set(card)
                .addOnSuccessListener(aVoid -> mainHandler.post(() -> listener.onSuccess(card)))
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro ao atualizar carta no Firestore", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void deletarCarta(String userId, String deckId, String cardId, OnCompleteListener<Void> listener) {
        // 1. Deleta do banco de dados local (Room)
        if (flashcardDAO != null) {
            executorService.execute(() -> {
                // Busca o objeto pelo ID (String) para deletar
                // ATENÇÃO: getById(int) não funciona mais. Precisamos de um novo método no DAO.
                // Por enquanto, vamos focar na correção do Firestore. A deleção local precisará de ajuste.
            });
        }

        // 2. Deleta do Firestore usando o caminho correto
        db.collection("users").document(userId)
                // ----- CORREÇÃO AQUI -----
                .collection("baralhos").document(deckId)
                .collection("flashcards").document(cardId) // cardId já é a String correta
                // -------------------------
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
                // -------------------------
                .addOnSuccessListener(decksSnapshot -> {
                    if (decksSnapshot.isEmpty()) {
                        if (onComplete != null) onComplete.run();
                        return;
                    }
                    for (var deckDoc : decksSnapshot.getDocuments()) {
                        String deckId = deckDoc.getId();
                        // ----- CORREÇÃO AQUI -----
                        deckDoc.getReference().collection("flashcards").get()
                                // -------------------------
                                .addOnSuccessListener(cardsSnapshot -> {
                                    if (cardsSnapshot.isEmpty()) return;
                                    executorService.execute(() -> {
                                        for (var cardDoc : cardsSnapshot.getDocuments()) {
                                            Flashcard card = cardDoc.toObject(Flashcard.class);
                                            if (card != null && card.getFlashcardId() != null) {
                                                // A lógica de verificação de duplicatas pode ser melhorada,
                                                // mas por enquanto a correção do caminho é o principal.
                                                flashcardDAO.insert(card); // Idealmente, usar insert com OnConflictStrategy.REPLACE
                                            }
                                        }
                                    });
                                });
                    }
                    if (onComplete != null) {
                        // Executa o onComplete após iniciar as buscas, não espera a conclusão.
                        mainHandler.post(onComplete);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro na sincronização de baralhos", e);
                    if (onComplete != null) mainHandler.post(onComplete);
                });
    }

    // ##### NOVO METODO PARA SINCRONIZAÇÃO INTELIGENTE #####
    /**
     * Busca os flashcards de um baralho específico no Firestore,
     * salva-os no banco de dados local (Room) e retorna a lista.
     */
    public void fetchAndSaveCardsFromDeck(String userId, String deckId, OnCompleteListener<List<Flashcard>> listener) {
        if (flashcardDAO == null) {
            listener.onFailure(new IllegalStateException("FlashcardDAO não foi inicializado."));
            return;
        }

        // 1. Busca os flashcards do baralho específico no Firestore
        db.collection("users").document(userId)
                .collection("baralhos").document(deckId) // Caminho CORRETO
                .collection("flashcards").get() // Caminho CORRETO
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 2. Converte os documentos em objetos Flashcard
                    List<Flashcard> firestoreCards = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Flashcard card = doc.toObject(Flashcard.class);
                        if (card != null) {
                            firestoreCards.add(card);
                        }
                    }

                    // 3. Salva a lista inteira no Room em uma thread de background
                    executorService.execute(() -> {
                        // O metodo insert do DAO agora aceita uma lista, o que é mais eficiente
                        flashcardDAO.insertAll(firestoreCards);
                        Log.d("FlashcardService", "Sincronizadas " + firestoreCards.size() + " cartas do Firestore para o Room.");

                        // 4. Retorna a lista para a UI na thread principal
                        mainHandler.post(() -> listener.onSuccess(firestoreCards));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("FlashcardService", "Erro ao buscar cartas do Firestore", e);
                    mainHandler.post(() -> listener.onFailure(e));
                });
    }

    public void deletarTodasAsCartasDoBaralho(String userId, String deckId, Runnable onComplete) {
        // Deleta do Firestore
        db.collection("users").document(userId)
                .collection("baralhos").document(deckId)
                .collection("flashcards").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            document.getReference().delete();
                        }
                    }
                });

        // Deleta do Room
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