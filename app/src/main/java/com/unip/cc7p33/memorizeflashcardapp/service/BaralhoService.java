package com.unip.cc7p33.memorizeflashcardapp.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.DocumentReference;

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
    public Task<Void> decrementarContagem(String userId, String deckId) {
        final DocumentReference deckRef = db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId);

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
}