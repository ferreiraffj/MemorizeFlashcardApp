package com.unip.cc7p33.memorizeflashcardapp.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FieldValue;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

public class BaralhoService {

    private final FirebaseFirestore db;

    public BaralhoService() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> criarBaralho(Baralho baralho) {
        return db.collection("users")
                .document(baralho.getUsuarioId())
                .collection("decks")
                .document()
                .set(baralho);
    }

    public Task<QuerySnapshot> getBaralhos(String userId) {
        return db.collection("users")
                .document(userId)
                .collection("decks")
                .get();
    }

    public Task<Void> incrementarContagem(String userId, String deckId) {
        return db.collection("users")
                .document(userId)
                .collection("decks")
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
                .collection("decks")
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
}