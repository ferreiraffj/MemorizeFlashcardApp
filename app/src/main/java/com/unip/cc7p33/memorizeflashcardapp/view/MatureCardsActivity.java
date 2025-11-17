package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.adapter.FlashcardAdapter;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.service.DashboardService;

import java.util.ArrayList;
import java.util.List;

public class MatureCardsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FlashcardAdapter adapter;
    private List<Flashcard> matureCardsList;
    private DashboardService dashboardService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mature_cards);

        Toolbar toolbar = findViewById(R.id.toolbar_mature_cards);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        dashboardService = new DashboardService(this);

        recyclerView = findViewById(R.id.recycler_view_mature_cards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        matureCardsList = new ArrayList<>();
        adapter = new FlashcardAdapter(matureCardsList);
        recyclerView.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            loadMatureCards();
        } else {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadMatureCards() {
        dashboardService.getMatureCards(currentUserId, new DashboardService.DashboardDataCallback<List<Flashcard>>() {
            @Override
            public void onDataLoaded(List<Flashcard> data) {
                if (data != null && !data.isEmpty()) {
                    matureCardsList.clear();
                    matureCardsList.addAll(data);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MatureCardsActivity.this, "Nenhuma carta com conhecimento sólido encontrada.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("MatureCardsActivity", "Erro ao carregar cartas maduras", e);
                Toast.makeText(MatureCardsActivity.this, "Erro ao carregar as cartas.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
