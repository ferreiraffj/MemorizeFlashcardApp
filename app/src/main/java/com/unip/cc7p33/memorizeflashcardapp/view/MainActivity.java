package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.unip.cc7p33.memorizeflashcardapp.adapter.BaralhoAdapter;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.BaralhoService;
import com.unip.cc7p33.memorizeflashcardapp.service.FlashcardService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BaralhoAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private BaralhoAdapter baralhoAdapter;
    private List<Baralho> listaDeBaralhos;
    private TextView noDecksMessage;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddDeck, fabCreateDeck, fabAddCard;
    private TextView createDeckLabel, addCardLabel;
    private boolean isFabMenuOpen = false;

    private AuthService authService;
    private BaralhoService baralhoService;
    private FlashcardService flashcardService;  // Adicionado: para sincronização


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Para Android 11+ (API 30+): Usa WindowInsetsController para ocultar a barra de status
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        authService = new AuthService(this);
        baralhoService = new BaralhoService();
        baralhoService.setBaralhoDAO(AppDatabase.getInstance(this).baralhoDAO());
        flashcardService = new FlashcardService();
        flashcardService.setFlashcardDAO(AppDatabase.getInstance(this).flashcardDAO());

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        recyclerView = findViewById(R.id.recycler_view_decks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noDecksMessage = findViewById(R.id.text_view_no_decks_message);

        listaDeBaralhos = new ArrayList<>();
        baralhoAdapter = new BaralhoAdapter(listaDeBaralhos);
        baralhoAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(baralhoAdapter);

        // A chamada para carregar os dados foi MOVIDA daqui para o onResume()

        setupFabs();

        toolbar.setNavigationOnClickListener(v -> {
            Toast.makeText(this, "Ícone de Menu clicado!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Carrega os dados do Firestore sempre que a tela se torna visível/ativa
        carregarBaralhos();
    }

    private void carregarBaralhos() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        noDecksMessage.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        baralhoService.getBaralhos(currentUser.getUid(), new BaralhoService.OnCompleteListener<List<Baralho>>() {
            @Override
            public void onSuccess(List<Baralho> baralhos) {
                listaDeBaralhos.clear();
                listaDeBaralhos.addAll(baralhos);
                baralhoAdapter.notifyDataSetChanged();
                updateNoDecksMessageVisibility();
                flashcardService.syncExistingDataToRoom(currentUser.getUid(), () -> {
                    Log.d("MainActivity", "Sincronização de dados antigos concluída.");
                });
            }
            @Override
            public void onFailure(Exception e) {
                Log.e("MainActivity", "Erro ao buscar baralhos.", e);
                Toast.makeText(MainActivity.this, "Erro ao buscar baralhos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateDeckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_create_deck, null);
        final EditText editTextDeckName = dialogView.findViewById(R.id.edit_text_deck_name);

        builder.setView(dialogView)
                .setPositiveButton("Ok", (dialog, id) -> {
                String deckName = editTextDeckName.getText().toString().trim();
                FirebaseUser currentUser = authService.getCurrentUser();
                if (!deckName.isEmpty() && currentUser != null) {
                    Baralho novoBaralho = new Baralho(deckName, 0, currentUser.getUid());
                    // Use o listener da BaralhoService
                    baralhoService.criarBaralho(novoBaralho, currentUser.getUid(), new BaralhoService.OnCompleteListener<Baralho>() {
                        @Override
                        public void onSuccess(Baralho baralho) {
                            Toast.makeText(MainActivity.this, "Baralho '" + deckName + "' criado!", Toast.LENGTH_SHORT).show();
                            carregarBaralhos();  // Recarrega após sucesso
                        }
                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(MainActivity.this, "Erro ao criar baralho.", Toast.LENGTH_SHORT).show();
                            Log.e("MainActivity", "Erro ao criar baralho", e);
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "O nome do baralho não pode ser vazio.", Toast.LENGTH_SHORT).show();
                }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            authService.logout();
            Toast.makeText(this, "Você saiu da sua conta.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupFabs() {
        fabAddDeck = findViewById(R.id.fab_add_deck);
        fabCreateDeck = findViewById(R.id.fab_create_deck);
        fabAddCard = findViewById(R.id.fab_add_card);
        createDeckLabel = findViewById(R.id.text_view_create_deck_label);
        addCardLabel = findViewById(R.id.text_view_add_card_label);

        fabAddDeck.setOnClickListener(view -> {
            if (!isFabMenuOpen) {
                showFabMenu();
            } else {
                closeFabMenu();
            }
        });

        fabCreateDeck.setOnClickListener(v -> {
            showCreateDeckDialog();
            closeFabMenu();
        });

        fabAddCard.setOnClickListener(v -> {
            closeFabMenu();
            ArrayList<String> deckNames = new ArrayList<>();
            ArrayList<String> deckIds = new ArrayList<>();
            for (Baralho baralho : listaDeBaralhos) {
                deckNames.add(baralho.getNome());
                deckIds.add(baralho.getBaralhoId());
            }
            Intent intent = new Intent(MainActivity.this, AddCardActivity.class);
            intent.putStringArrayListExtra("DECK_NAMES", deckNames);
            intent.putStringArrayListExtra("DECK_IDS", deckIds);
            startActivity(intent);
        });
    }

    private void showFabMenu() {
        isFabMenuOpen = true;
        fabAddDeck.setImageResource(R.drawable.ic_close);
        fabCreateDeck.setVisibility(View.VISIBLE);
        createDeckLabel.setVisibility(View.VISIBLE);
        fabAddCard.setVisibility(View.VISIBLE);
        addCardLabel.setVisibility(View.VISIBLE);
    }

    private void closeFabMenu() {
        isFabMenuOpen = false;
        fabAddDeck.setImageResource(R.drawable.ic_add);
        fabCreateDeck.setVisibility(View.INVISIBLE);
        createDeckLabel.setVisibility(View.INVISIBLE);
        fabAddCard.setVisibility(View.INVISIBLE);
        addCardLabel.setVisibility(View.INVISIBLE);
    }

    private void updateNoDecksMessageVisibility() {
        if (listaDeBaralhos.isEmpty()) {
            noDecksMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE); // Corrigido para VISIBLE para evitar sobreposição
        } else {
            noDecksMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Baralho baralho) {
        Intent intent = new Intent(MainActivity.this, CardListActivity.class);
        intent.putExtra("DECK_ID", baralho.getBaralhoId());
        intent.putExtra("DECK_NAME", baralho.getNome());
        startActivity(intent);
    }
}