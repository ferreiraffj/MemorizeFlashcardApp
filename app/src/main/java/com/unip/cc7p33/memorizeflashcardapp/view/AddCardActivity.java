package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.service.AuthService;
import com.unip.cc7p33.memorizeflashcardapp.service.BaralhoService;
import com.unip.cc7p33.memorizeflashcardapp.service.FlashcardService;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddCardActivity extends AppCompatActivity {

    private Spinner spinnerDecks;
    private EditText editTextFront;
    private EditText editTextBack;
    private List<String> deckIds;

    private FlashcardService flashcardService;
    private AuthService authService;
    private BaralhoService baralhoService; // Adicionado para incrementar contagem

    private boolean isEditMode = false;
    private String editingCardId;
    private String currentDeckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        SystemUIUtils.hideStatusBar(this);

        flashcardService = new FlashcardService();
        flashcardService.setFlashcardDAO(AppDatabase.getInstance(this).flashcardDAO());
        authService = new AuthService(this);
        baralhoService = new BaralhoService(); // Inicializa o serviço
        baralhoService.setBaralhoDAO(AppDatabase.getInstance(this).baralhoDAO());

        Toolbar toolbar = findViewById(R.id.toolbar_add_card);
        setSupportActionBar(toolbar);

        spinnerDecks = findViewById(R.id.spinner_decks);
        Spinner spinnerCardType = findViewById(R.id.spinner_card_type);
        editTextFront = findViewById(R.id.edit_text_front);
        editTextBack = findViewById(R.id.edit_text_back);

        if (getIntent().hasExtra("EDIT_MODE")) {
            isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
            editingCardId = getIntent().getStringExtra("CARD_ID");
            currentDeckId = getIntent().getStringExtra("DECK_ID");
            editTextFront.setText(getIntent().getStringExtra("CARD_FRONT"));
            editTextBack.setText(getIntent().getStringExtra("CARD_BACK"));
            String deckName = getIntent().getStringExtra("DECK_NAME");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList(deckName));
            spinnerDecks.setAdapter(adapter);
            spinnerDecks.setEnabled(false);
        } else {
            ArrayList<String> deckNames = getIntent().getStringArrayListExtra("DECK_NAMES");
            deckIds = getIntent().getStringArrayListExtra("DECK_IDS");
            if (deckNames == null || deckNames.isEmpty()) {
                deckNames = new ArrayList<>();
                deckNames.add("Crie um baralho primeiro");
                spinnerDecks.setEnabled(false);
            }
            ArrayAdapter<String> decksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deckNames);
            decksAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDecks.setAdapter(decksAdapter);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "EDITAR CARTA" : "ADICIONAR CARTAS");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        ArrayAdapter<String> cardTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Collections.singletonList("Padrão"));
        cardTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCardType.setAdapter(cardTypeAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_card_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_save_card) {
            saveOrUpdateCard();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveOrUpdateCard() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Erro: Usuário não encontrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        String frontText = editTextFront.getText().toString().trim();
        String backText = editTextBack.getText().toString().trim();

        if (frontText.isEmpty() || backText.isEmpty()) {
            Toast.makeText(this, "Preencha a frente e o verso do card.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        if (isEditMode) {
            Flashcard card = new Flashcard(frontText, backText, currentDeckId, userId);
            card.setFlashcardId(editingCardId);
            card.setDeckId(currentDeckId);
            flashcardService.updateCarta(userId, currentDeckId, card, new FlashcardService.OnCompleteListener<Flashcard>() {
                @Override
                public void onSuccess(Flashcard result) {
                    Toast.makeText(AddCardActivity.this, "Carta atualizada com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AddCardActivity.this, "Erro ao atualizar a carta.", Toast.LENGTH_SHORT).show();
                    Log.e("AddCardActivity", "Erro ao atualizar carta", e);
                }
            });
        } else {
            if (deckIds == null || deckIds.isEmpty()) {
                Toast.makeText(this, "Você precisa criar um baralho antes.", Toast.LENGTH_LONG).show();
                return;
            }
            int selectedDeckPosition = spinnerDecks.getSelectedItemPosition();
            String selectedDeckId = deckIds.get(selectedDeckPosition);

            Flashcard card = new Flashcard(frontText, backText, selectedDeckId, userId);

            flashcardService.adicionarCarta(userId, selectedDeckId, card, new FlashcardService.OnCompleteListener<Flashcard>() {
                @Override
                public void onSuccess(Flashcard result) {
                    // A UI é atualizada IMEDIATAMENTE após o sucesso local.
                    Toast.makeText(AddCardActivity.this, "Carta salva com sucesso!", Toast.LENGTH_SHORT).show();
                    editTextFront.setText("");
                    editTextBack.setText("");
                    editTextFront.requestFocus();

                    // A sincronização da contagem online acontece em segundo plano.
                    baralhoService.incrementarContagem(userId, selectedDeckId);
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(AddCardActivity.this, "Erro ao salvar a carta.", Toast.LENGTH_SHORT).show();
                    Log.e("AddCardActivity", "Erro ao salvar carta", e);
                }
            });
        }
    }
}