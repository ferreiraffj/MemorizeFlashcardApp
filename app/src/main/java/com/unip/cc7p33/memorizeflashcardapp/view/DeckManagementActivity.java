package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.adapter.DeckManagementAdapter;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.BaralhoService;
import com.unip.cc7p33.memorizeflashcardapp.service.FlashcardService;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.util.ArrayList;
import java.util.List;

public class DeckManagementActivity extends AppCompatActivity implements DeckManagementAdapter.OnItemClickListener {

    private DeckManagementAdapter adapter;
    private List<Baralho> deckList;
    private BaralhoService baralhoService;
    private AuthService authService;
    private FlashcardService flashcardService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_management);

        SystemUIUtils.hideStatusBar(this);

        authService = new AuthService(this);
        baralhoService = new BaralhoService();
        flashcardService = new FlashcardService();

        // Injeta as dependências
        baralhoService.setBaralhoDAO(AppDatabase.getInstance(this).baralhoDAO());
        flashcardService.setFlashcardDAO(AppDatabase.getInstance(this).flashcardDAO());
        baralhoService.setFlashcardService(flashcardService);

        Toolbar toolbar = findViewById(R.id.toolbar_deck_management);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view_deck_management);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deckList = new ArrayList<>();
        adapter = new DeckManagementAdapter(deckList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarBaralhos();
    }

    private void carregarBaralhos() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        baralhoService.getBaralhos(currentUser.getUid(), new BaralhoService.OnCompleteListener<List<Baralho>>() {
            @Override
            public void onSuccess(List<Baralho> baralhos) {
                deckList.clear();
                deckList.addAll(baralhos);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("DeckManagementActivity", "Erro ao buscar baralhos.", e);
                Toast.makeText(DeckManagementActivity.this, "Erro ao buscar baralhos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onEditClick(int position) {
        Baralho deckToEdit = deckList.get(position);
        showEditDeckDialog(position, deckToEdit);
    }

    private void showEditDeckDialog(int position, Baralho deck) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Nome do Baralho");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(deck.getNome());
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (newName.isEmpty()) {
                Toast.makeText(this, "O nome do baralho não pode ser vazio.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_SHORT).show();
                return;
            }

            updateDeckName(currentUser.getUid(), deck, newName, position);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateDeckName(String userId, Baralho deck, String newName, int position) {
        baralhoService.updateNomeBaralho(userId, deck, newName, new BaralhoService.OnCompleteListener<Baralho>() {
            @Override
            public void onSuccess(Baralho updatedDeck) {
                Toast.makeText(DeckManagementActivity.this, "Nome do baralho atualizado.", Toast.LENGTH_SHORT).show();
                deckList.set(position, updatedDeck);
                adapter.notifyItemChanged(position);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("DeckManagementActivity", "Erro ao atualizar nome do baralho.", e);
                Toast.makeText(DeckManagementActivity.this, "Erro ao atualizar nome.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        Baralho deckToDelete = deckList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Excluir Baralho")
                .setMessage("Tem certeza que deseja excluir o baralho '" + deckToDelete.getNome() + "'? Todos os cartões associados também serão excluídos.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    FirebaseUser currentUser = authService.getCurrentUser();
                    if (currentUser == null) {
                        Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    deleteDeck(currentUser.getUid(), deckToDelete, position);
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteDeck(String userId, Baralho deck, int position) {
        baralhoService.deleteBaralho(userId, deck, new BaralhoService.OnCompleteListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(DeckManagementActivity.this, "Baralho '" + deck.getNome() + "' excluído.", Toast.LENGTH_SHORT).show();
                deckList.remove(position);
                adapter.notifyItemRemoved(position);
                adapter.notifyItemRangeChanged(position, deckList.size());
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("DeckManagementActivity", "Erro ao excluir baralho.", e);
                Toast.makeText(DeckManagementActivity.this, "Erro ao excluir baralho.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
