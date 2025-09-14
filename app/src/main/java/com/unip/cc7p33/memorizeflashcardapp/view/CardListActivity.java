package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import necessário
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.adapter.FlashcardAdapter;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.BaralhoService; // Import necessário
import com.unip.cc7p33.memorizeflashcardapp.service.FlashcardService;

import java.util.ArrayList;
import java.util.List;

// ALTERAÇÃO 1: Implementa a nova interface de clique para deletar
public class CardListActivity extends AppCompatActivity implements FlashcardAdapter.OnItemClickListener, FlashcardAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private FlashcardAdapter adapter;
    private List<Flashcard> cardList;
    private FlashcardService flashcardService;
    private AuthService authService;
    private String deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        deckId = getIntent().getStringExtra("DECK_ID");
        String deckName = getIntent().getStringExtra("DECK_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar_card_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(deckName != null ? deckName : "Cartas");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        flashcardService = new FlashcardService();
        authService = new AuthService(this);

        recyclerView = findViewById(R.id.recycler_view_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardList = new ArrayList<>();
        adapter = new FlashcardAdapter(cardList);
        adapter.setOnItemClickListener(this);
        adapter.setOnDeleteClickListener(this); // <-- ALTERAÇÃO 2: Registra o listener de exclusão
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deckId != null) {
            loadCardsFromFirestore(deckId);
        }
    }

    private void loadCardsFromFirestore(String deckId) {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) return;

        flashcardService.getCartasDoBaralho(currentUser.getUid(), deckId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cardList.clear();
                        cardList.addAll(task.getResult().toObjects(Flashcard.class));
                        adapter.notifyDataSetChanged();
                        if (cardList.isEmpty()) {
                            Toast.makeText(this, "Nenhuma carta neste baralho ainda.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("CardListActivity", "Erro ao buscar cartas.", task.getException());
                        Toast.makeText(this, "Erro ao buscar cartas.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Flashcard card) {
        Intent intent = new Intent(this, AddCardActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("DECK_ID", getIntent().getStringExtra("DECK_ID"));
        intent.putExtra("DECK_NAME", getIntent().getStringExtra("DECK_NAME"));
        intent.putExtra("CARD_ID", card.getId());
        intent.putExtra("CARD_FRONT", card.getFrente());
        intent.putExtra("CARD_BACK", card.getVerso());
        startActivity(intent);
    }

    // --- ALTERAÇÃO 3: NOVO MÉTODO PARA LIDAR COM O CLIQUE DE EXCLUSÃO ---
    @Override
    public void onDeleteClick(Flashcard card, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir Carta")
                .setMessage("Você tem certeza que deseja excluir esta carta? Esta ação não pode ser desfeita.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    FirebaseUser currentUser = authService.getCurrentUser();
                    if (currentUser == null) return;
                    String userId = currentUser.getUid();
                    String currentDeckId = getIntent().getStringExtra("DECK_ID");

                    // Deleta a carta do Firestore
                    flashcardService.deletarCarta(userId, currentDeckId, card.getId())
                            .addOnSuccessListener(aVoid -> {
                                // Decrementa o contador do baralho
                                new BaralhoService().decrementarContagem(userId, currentDeckId);

                                // Remove da lista local e notifica o adapter
                                cardList.remove(position);
                                adapter.notifyItemRemoved(position);
                                Toast.makeText(this, "Carta excluída.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Erro ao excluir a carta.", Toast.LENGTH_SHORT).show();
                                Log.e("CardListActivity", "Erro ao excluir carta", e);
                            });
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }
}