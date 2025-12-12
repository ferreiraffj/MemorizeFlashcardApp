package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.adapter.FlashcardAdapter;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.BaralhoService;
import com.unip.cc7p33.memorizeflashcardapp.service.FlashcardService;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

public class CardListActivity extends AppCompatActivity implements FlashcardAdapter.OnItemClickListener, FlashcardAdapter.OnDeleteClickListener {

    private RecyclerView recyclerView;
    private FlashcardAdapter adapter;
    private List<Flashcard> cardList;
    private FlashcardService flashcardService;
    private AuthService authService;
    private FlashcardDAO flashcardDAO;  // Novo: DAO para Room
    private String deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        SystemUIUtils.setImmersiveMode(this);

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
        flashcardDAO = AppDatabase.getInstance(this).flashcardDAO();  // Novo: Inicializa DAO

        recyclerView = findViewById(R.id.recycler_view_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cardList = new ArrayList<>();
        adapter = new FlashcardAdapter(cardList);
        adapter.setOnItemClickListener(this);
        adapter.setOnDeleteClickListener(this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabStartStudy = findViewById(R.id.fab_start_study);
        fabStartStudy.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                long now = new Date().getTime();
                List<Flashcard> cardsForStudy = flashcardDAO.getCardsForStudy(deckId, now);  // Nova query
                runOnUiThread(() -> {
                    if (cardsForStudy != null && !cardsForStudy.isEmpty()) {
                        Intent intent = new Intent(CardListActivity.this, SessaoEstudoActivity.class);
                        intent.putExtra("CARD_LIST", (Serializable) cardsForStudy);  // Envia apenas as filtradas
                        intent.putExtra("DECK_NAME", getIntent().getStringExtra("DECK_NAME"));
                        startActivity(intent);
                    } else {
                        Toast.makeText(CardListActivity.this, "Nenhuma carta para estudar hoje! Todas estão em dia ou o baralho está vazio.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deckId != null) {
            loadCards(deckId);  // Mudança: Chama loadCards em vez de loadCardsFromFirestore
        }
    }

    // Novo: Carrega cartas do Room primeiro, sincroniza do Firestore se vazio
    private void loadCards(String deckId) {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Flashcard> localCards = flashcardDAO.getByDeckId(deckId);  // Filtra por deck
            runOnUiThread(() -> {
                if (localCards != null && !localCards.isEmpty()) {
                    cardList.clear();
                    cardList.addAll(localCards);
                    adapter.notifyDataSetChanged();
                } else {
                    syncCardsFromFirestore(deckId);
                }
            });
        });
    }


    // Renomeado e ajustado: Sincroniza do Firestore e salva no Room
    private void syncCardsFromFirestore(String deckId) {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) return;

        // Usa o novo método do serviço que já cuida de tudo
        flashcardService.fetchAndSaveCardsFromDeck(currentUser.getUid(), deckId, new FlashcardService.OnCompleteListener<List<Flashcard>>() {
            @Override
            public void onSuccess(List<Flashcard> syncedCards) {
                // Atualiza a UI com as cartas recém-sincronizadas
                cardList.clear();
                cardList.addAll(syncedCards);
                adapter.notifyDataSetChanged();

                if (syncedCards.isEmpty()) {
                    Toast.makeText(CardListActivity.this, "Nenhuma carta neste baralho ainda.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("CardListActivity", "Erro ao sincronizar cartas.", e);
                // Verifica se a lista local de cartas está vazia
                if (cardList.isEmpty()) {
                    Toast.makeText(CardListActivity.this, "Nenhuma carta neste baralho ainda.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CardListActivity.this, "Erro ao buscar cartas.", Toast.LENGTH_SHORT).show();
                }
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
        intent.putExtra("CARD_ID", card.getFlashcardId());
        intent.putExtra("CARD_FRONT", card.getFrente());
        intent.putExtra("CARD_BACK", card.getVerso());
        startActivity(intent);
    }

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

                    flashcardService.deletarCarta(userId, currentDeckId, String.valueOf(card.getFlashcardId()), new FlashcardService.OnCompleteListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Executors.newSingleThreadExecutor().execute(() -> flashcardDAO.delete(card));
                            new BaralhoService().decrementarContagem(userId, currentDeckId);
                            cardList.remove(position);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(CardListActivity.this, "Carta excluída.", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(CardListActivity.this, "Erro ao excluir a carta.", Toast.LENGTH_SHORT).show();
                            Log.e("CardListActivity", "Erro ao excluir carta", e);
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }
}
