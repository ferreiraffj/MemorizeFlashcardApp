package com.unip.cc7p33.memorizeflashcardapp.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.adapter.BaralhoAdapter;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.RankingInfo;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.BaralhoService;
import com.unip.cc7p33.memorizeflashcardapp.service.FlashcardService;
import com.unip.cc7p33.memorizeflashcardapp.service.SessaoEstudoService;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements BaralhoAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private BaralhoAdapter baralhoAdapter;
    private List<Baralho> listaDeBaralhos;
    private TextView noDecksMessage, tvOfensivaHeader, tvXpProgress;
    private ImageView ivOfensivaIcon, ivCurrentRankIcon, ivNextRankIcon;
    private ProgressBar progressBarXp;
    private FloatingActionButton fabAddDeck, fabCreateDeck, fabAddCard;
    private TextView createDeckLabel, addCardLabel;
    private boolean isFabMenuOpen = false;
    private SwipeRefreshLayout swipeRefreshLayout;

    private AuthService authService;
    private BaralhoService baralhoService;
    private FlashcardService flashcardService;

    // Firebase Auth Listener
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);

        SystemUIUtils.setImmersiveMode(this);

        // Inicialização dos serviços
        authService = new AuthService(this);
        baralhoService = new BaralhoService();
        baralhoService.setBaralhoDAO(AppDatabase.getInstance(this).baralhoDAO());
        flashcardService = new FlashcardService();
        flashcardService.setFlashcardDAO(AppDatabase.getInstance(this).flashcardDAO());

        // Inicialização do Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setup da UI
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setupViews();
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        setupNavigationDrawer(toolbar, drawerLayout);
        setupFabs();
        setupSwipeToRefresh();
        
        // Configura o AuthStateListener
        setupAuthListener();
    }

    private void setupAuthListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // Usuário está logado (online ou offline)
                Log.d("MainActivity", "onAuthStateChanged:signed_in:" + user.getUid());
                currentUserId = user.getUid();
                // Somente carregue os dados DEPOIS de confirmar o usuário
                loadInitialData();
            } else {
                // Usuário não está logado
                Log.d("MainActivity", "onAuthStateChanged:signed_out");
                // Redireciona para a tela de login
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        // Anexa o listener ao iniciar a activity
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove o listener ao parar a activity para evitar memory leaks
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupViews() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view_decks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noDecksMessage = findViewById(R.id.text_view_no_decks_message);
        tvOfensivaHeader = findViewById(R.id.tv_ofensiva_header);
        ivOfensivaIcon = findViewById(R.id.iv_ofensiva_icon);
        ivCurrentRankIcon = findViewById(R.id.iv_current_rank_icon);
        ivNextRankIcon = findViewById(R.id.iv_next_rank_icon);
        progressBarXp = findViewById(R.id.progress_bar_xp);
        tvXpProgress = findViewById(R.id.tv_xp_progress);

        listaDeBaralhos = new ArrayList<>();
        baralhoAdapter = new BaralhoAdapter(listaDeBaralhos, AppDatabase.getInstance(this).baralhoDAO());
        baralhoAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(baralhoAdapter);
    }

    private void loadInitialData() {
        if (currentUserId == null) return;
        updateOfensivaERanking();
        carregarBaralhos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // A carga de dados agora é iniciada pelo AuthStateListener.
        // Podemos manter chamadas aqui se quisermos que a tela sempre atualize ao voltar, 
        // mas garantindo que o `currentUserId` não seja nulo.
        if (currentUserId != null) {
            loadInitialData();
        }
    }

    private void setupNavigationDrawer(Toolbar toolbar, DrawerLayout drawerLayout) {
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_perfil) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else if (id == R.id.nav_dashboards) {
                startActivity(new Intent(this, DashboardActivity.class));
//            } else if (id == R.id.nav_ajuda) {
//                Toast.makeText(this, "Ajuda - Em breve!", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_baralhos){
                startActivity(new Intent(MainActivity.this, DeckManagementActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setColorSchemeResources(R.color.blueStripe, R.color.colorFab);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Log.d("MainActivity", "Refresh acionado. Sincronizando dados da nuvem.");
            forceSyncFromServer();
        });
    }

    private void forceSyncFromServer() {
        if (currentUserId == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        swipeRefreshLayout.setRefreshing(true);

        baralhoService.baixarBaralhosDaNuvem(currentUserId, new BaralhoService.OnCompleteListener<List<Baralho>>() {
            @Override
            public void onSuccess(List<Baralho> baralhos) {
                flashcardService.syncExistingDataToRoom(currentUserId, () -> {
                    Log.d("MainActivity", "Sincronização de cartões finalizada.");
                    runOnUiThread(() -> {
                        carregarBaralhos();
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(MainActivity.this, "Dados sincronizados!", Toast.LENGTH_SHORT).show();
                    });
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("MainActivity", "Erro ao forçar a sincronização.", e);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Erro ao sincronizar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarBaralhos() {
        if (currentUserId == null) return;
        baralhoService.getBaralhos(currentUserId, new BaralhoService.OnCompleteListener<List<Baralho>>() {
            @Override
            public void onSuccess(List<Baralho> baralhos) {
                listaDeBaralhos.clear();
                listaDeBaralhos.addAll(baralhos);
                baralhoAdapter.notifyDataSetChanged();
                updateNoDecksMessageVisibility();
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
                if (!deckName.isEmpty() && currentUserId != null) {
                    Baralho novoBaralho = new Baralho(deckName, 0, currentUserId);
                    baralhoService.criarBaralho(novoBaralho, currentUserId, new BaralhoService.OnCompleteListener<Baralho>() {
                        @Override
                        public void onSuccess(Baralho baralho) {
                            Toast.makeText(MainActivity.this, "Baralho '" + deckName + "' criado!", Toast.LENGTH_SHORT).show();
                            carregarBaralhos();
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
            authService.logout(); // O AuthStateListener cuidará do redirecionamento
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
            recyclerView.setVisibility(View.GONE);
        } else {
            noDecksMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Baralho baralho) {
        baralho.setVisitas(baralho.getVisitas() + 1);
        Executors.newSingleThreadExecutor().execute(() ->
                AppDatabase.getInstance(this).baralhoDAO().update(baralho)
        );

        Intent intent = new Intent(MainActivity.this, CardListActivity.class);
        intent.putExtra("DECK_ID", baralho.getBaralhoId());
        intent.putExtra("DECK_NAME", baralho.getNome());
        startActivity(intent);
    }

    private void updateOfensivaERanking() {
        if (currentUserId == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            UsuarioDAO usuarioDAO = AppDatabase.getInstance(this).usuarioDAO();
            Usuario usuario = usuarioDAO.getUserByUID(currentUserId);

            if (usuario != null) {
                final boolean estudouHoje = SessaoEstudoService.jaEstudouHoje(usuario.getUltimoEstudo());
                final RankingInfo rankingInfo = SessaoEstudoService.getRankingInfo(usuario.getXp());

                runOnUiThread(() -> {
                    tvOfensivaHeader.setText(String.valueOf(usuario.getOfensiva()));
                    ivOfensivaIcon.setImageResource(estudouHoje ? R.drawable.ic_fogo_aceso : R.drawable.ic_fogo_apagado);

                    ivCurrentRankIcon.setImageResource(getRankDrawableId(rankingInfo.getCurrentRankName()));
                    ivNextRankIcon.setImageResource(getRankDrawableId(rankingInfo.getNextRankName()));

                    progressBarXp.setProgress(rankingInfo.getProgressPercentage());

                    if (rankingInfo.getNextRankXp() <= rankingInfo.getCurrentRankXp()) {
                        tvXpProgress.setText("Nível Máximo");
                    } else {
                        String xpText = (usuario.getXp() - rankingInfo.getCurrentRankXp()) + " / " + (rankingInfo.getNextRankXp() - rankingInfo.getCurrentRankXp()) + " XP";
                        tvXpProgress.setText(xpText);
                    }
                });
            }
        });
    }

    private int getRankDrawableId(String rankName) {
        switch (rankName.toLowerCase()) {
            case "prata":
                return R.drawable.ic_rank_prata;
            case "ouro":
                return R.drawable.ic_rank_ouro;
            case "platina":
                return R.drawable.ic_rank_platina;
            case "diamante":
                return R.drawable.ic_rank_diamante;
            case "safira":
                return R.drawable.ic_rank_safira;
            case "rubi":
                return R.drawable.ic_rank_rubi;
            case "bronze":
            default:
                return R.drawable.ic_rank_bronze;
        }
    }
}
