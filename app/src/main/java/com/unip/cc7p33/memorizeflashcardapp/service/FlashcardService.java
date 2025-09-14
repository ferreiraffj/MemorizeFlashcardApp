package com.unip.cc7p33.memorizeflashcardapp.service;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

public class FlashcardService {

    private final FirebaseFirestore db;

    public FlashcardService() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<DocumentReference> adicionarCarta(String userId, String deckId, Flashcard card) {
        return db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .add(card);
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
    public Task<Void> updateCarta(String userId, String deckId, Flashcard card) {
        return db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .document(card.getId())
                .set(card);
    }
    public Task<Void> deletarCarta(String userId, String deckId, String cardId) {
        return db.collection("users")
                .document(userId)
                .collection("decks")
                .document(deckId)
                .collection("cards")
                .document(cardId)
                .delete();
    }
}