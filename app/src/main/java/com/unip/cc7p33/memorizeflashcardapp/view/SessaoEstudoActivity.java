package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.Collections;
import java.util.List;

public class SessaoEstudoActivity extends AppCompatActivity {

    private TextView textViewCardContent;
    private TextView textViewShowAnswer;
    private CardView cardViewFlashcard;
    private LinearLayout layoutAssessment, layoutSessionFinished;

    private List<Flashcard> cardList;
    private int currentCardIndex = 0;
    private int correctAnswersCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessao_estudo);

        // Suprime o aviso de cast, pois sabemos que o tipo está correto.
        // Isso resolve o aviso 'Unchecked cast'.
        @SuppressWarnings("unchecked")
        List<Flashcard> receivedList = (List<Flashcard>) getIntent().getSerializableExtra("CARD_LIST");
        cardList = receivedList;

        String deckName = getIntent().getStringExtra("DECK_NAME");

        Toolbar toolbar = findViewById(R.id.toolbar_sessao_estudo);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("VOCÊ ESTÁ ESTUDANDO: " + (deckName != null ? deckName.toUpperCase() : ""));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        if (cardList == null || cardList.isEmpty()) {
            Toast.makeText(this, "Erro: Nenhuma carta encontrada para estudar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Collections.shuffle(cardList);

        // As variáveis das Views agora são locais, pois só são usadas aqui.
        // Isso resolve o aviso 'Field can be converted to a local variable'.
        cardViewFlashcard = findViewById(R.id.card_view_flashcard);
        textViewCardContent = findViewById(R.id.text_view_card_content);
        textViewShowAnswer = findViewById(R.id.text_view_show_answer);
        layoutAssessment = findViewById(R.id.layout_assessment);
        TextView textViewEasy = findViewById(R.id.text_view_easy);
        TextView textViewHard = findViewById(R.id.text_view_hard);
        layoutSessionFinished = findViewById(R.id.layout_session_finished);
        TextView textViewScore = findViewById(R.id.text_view_score);
        Button buttonBack = findViewById(R.id.button_back);

        displayCurrentCard();

        textViewShowAnswer.setOnClickListener(v -> flipCard());
        textViewEasy.setOnClickListener(v -> nextCard(true, textViewScore));
        textViewHard.setOnClickListener(v -> nextCard(false, textViewScore));
        buttonBack.setOnClickListener(v -> finish());

        // Lida com o clique na seta de voltar da Toolbar (abordagem moderna)
        // Isso resolve o aviso 'onBackPressed()' is deprecated'.
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void displayCurrentCard() {
        if (currentCardIndex < cardList.size()) {
            textViewCardContent.setText(cardList.get(currentCardIndex).getFrente());
            layoutAssessment.setVisibility(View.GONE);
            textViewShowAnswer.setVisibility(View.VISIBLE);
        } else {
            showEndScreen(findViewById(R.id.text_view_score));
        }
    }

    private void flipCard() {
        if (currentCardIndex >= cardList.size()) return;
        textViewCardContent.setText(cardList.get(currentCardIndex).getVerso());
        textViewShowAnswer.setVisibility(View.GONE);
        layoutAssessment.setVisibility(View.VISIBLE);
    }

    private void nextCard(boolean wasCorrect, TextView textViewScore) {
        if (wasCorrect) {
            correctAnswersCount++;
        }
        currentCardIndex++;
        // Passa a referência do textViewScore para o displayCurrentCard
        if (currentCardIndex < cardList.size()) {
            displayCurrentCard();
        } else {
            showEndScreen(textViewScore);
        }
    }

    private void showEndScreen(TextView textViewScore) {
        cardViewFlashcard.setVisibility(View.GONE);
        layoutAssessment.setVisibility(View.GONE);
        textViewShowAnswer.setVisibility(View.GONE);
        layoutSessionFinished.setVisibility(View.VISIBLE);

        textViewScore.setText("ACERTOS: " + correctAnswersCount + "/" + cardList.size());
    }
}