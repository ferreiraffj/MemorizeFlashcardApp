package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
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

import java.io.Serializable;
import java.util.ArrayList;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

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
            if (cardList != null && !cardList.isEmpty()) {
                Intent intent = new Intent(CardListActivity.this, SessaoEstudoActivity.class);
                intent.putExtra("CARD_LIST", (Serializable) cardList);
                intent.putExtra("DECK_NAME", getIntent().getStringExtra("DECK_NAME"));
                startActivity(intent);
            } else {
                Toast.makeText(this, "Não há cartas para estudar neste baralho.", Toast.LENGTH_SHORT).show();
            }
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
        flashcardService.getCartasDoBaralho(currentUser.getUid(), deckId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Flashcard> syncedCards = task.getResult().toObjects(Flashcard.class);
                        cardList.clear();
                        cardList.addAll(syncedCards);
                        adapter.notifyDataSetChanged();
                        Executors.newSingleThreadExecutor().execute(() -> {
                            for (Flashcard card : syncedCards) {
                                card.setDeckId(deckId);  // Adicione deckId
                                List<Flashcard> existing = flashcardDAO.getByDeckId(deckId);
                                boolean isDuplicate = existing.stream().anyMatch(c -> c.getFrente().equals(card.getFrente()) && c.getVerso().equals(card.getVerso()));
                                if (!isDuplicate) {
                                    flashcardDAO.insert(card);
                                }
                            }
                        });
                        if (syncedCards.isEmpty()) {
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
