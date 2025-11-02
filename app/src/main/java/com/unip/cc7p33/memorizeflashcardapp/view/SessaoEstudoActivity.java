package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.service.SessaoEstudoService;

import java.util.List;

public class SessaoEstudoActivity extends AppCompatActivity {

    private TextView textViewCardContent, textViewShowAnswer, textViewScore;
    private CardView cardViewFlashcard;
    private LinearLayout layoutAssessment, layoutSessionFinished;
    private SessaoEstudoService service;
    private long startTime;  // Novo: para medir tempo


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessao_estudo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        @SuppressWarnings("unchecked")
        List<Flashcard> receivedList = (List<Flashcard>) getIntent().getSerializableExtra("CARD_LIST");
        String deckName = getIntent().getStringExtra("DECK_NAME");

        if (receivedList == null || receivedList.isEmpty()) {
            Toast.makeText(this, "Erro: Nenhuma carta encontrada para estudar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        service = new SessaoEstudoService();
        service.setFlashcardDAO(AppDatabase.getInstance(this).flashcardDAO());
        service.iniciarSessao(receivedList);

        Toolbar toolbar = findViewById(R.id.toolbar_sessao_estudo);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("VOCÊ ESTÁ ESTUDANDO: " + (deckName != null ? deckName.toUpperCase() : ""));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }

        cardViewFlashcard = findViewById(R.id.card_view_flashcard);
        textViewCardContent = findViewById(R.id.text_view_card_content);
        textViewShowAnswer = findViewById(R.id.text_view_show_answer);
        layoutAssessment = findViewById(R.id.layout_assessment);
        TextView textViewEasy = findViewById(R.id.text_view_easy);
        TextView textViewMedium = findViewById(R.id.text_view_medium);  // Novo campo
        TextView textViewHard = findViewById(R.id.text_view_hard);
        layoutSessionFinished = findViewById(R.id.layout_session_finished);
        textViewScore = findViewById(R.id.text_view_score);
        Button buttonBack = findViewById(R.id.button_back);

        displayCurrentCard();

        textViewShowAnswer.setOnClickListener(v -> flipCard());
        textViewEasy.setOnClickListener(v -> nextCard(5));  // Fácil: qualidade 5
        textViewMedium.setOnClickListener(v -> nextCard(3));  // Médio: qualidade 3
        textViewHard.setOnClickListener(v -> nextCard(1));  // Difícil: qualidade 1
        buttonBack.setOnClickListener(v -> finish());

        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void displayCurrentCard() {
        Flashcard card = service.obterCartaAtual();
        if (card != null) {
            textViewCardContent.setText(card.getFrente());
            layoutAssessment.setVisibility(View.GONE);
            textViewShowAnswer.setVisibility(View.VISIBLE);
        } else {
            showEndScreen();
        }
    }

    private void flipCard() {
        Flashcard card = service.obterCartaAtual();
        if (card != null) {
            textViewCardContent.setText(card.getVerso());
            textViewShowAnswer.setVisibility(View.GONE);
            layoutAssessment.setVisibility(View.VISIBLE);
            startTime = System.currentTimeMillis();  // Inicia timer
        }
    }

    private void nextCard(int quality) {
        long responseTime = System.currentTimeMillis() - startTime;  // Calcula tempo de resposta
        Flashcard card = service.obterCartaAtual();
        if (card != null) {
            // Atualiza o tempo médio
            long currentAvg = card.getTempoRespostaMedio();
            card.setTempoRespostaMedio((currentAvg + responseTime) / 2); // Média simples;
        }
        service.avancarCarta(quality);
        displayCurrentCard();
    }

    private void showEndScreen() {
        cardViewFlashcard.setVisibility(View.GONE);
        layoutAssessment.setVisibility(View.GONE);
        textViewShowAnswer.setVisibility(View.GONE);
        layoutSessionFinished.setVisibility(View.VISIBLE);

        textViewScore.setText(service.obterEstatisticas());
    }
}