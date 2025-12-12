package com.unip.cc7p33.memorizeflashcardapp.view;

import static com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils.setImmersiveMode;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.DeckStats;
import com.unip.cc7p33.memorizeflashcardapp.service.DashboardService;

public class RetentionDetailsActivity extends AppCompatActivity {

    private TextView totalStudied, correctAnswers, incorrectAnswers;
    private DashboardService dashboardService;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retention_details);
        setImmersiveMode(this);

        dashboardService = new DashboardService(this);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = user.getUid();

        setupViews();
        loadRetentionDetails();
    }

    private void setupViews() {
        totalStudied = findViewById(R.id.tv_total_studied);
        correctAnswers = findViewById(R.id.tv_correct_answers);
        incorrectAnswers = findViewById(R.id.tv_incorrect_answers);
    }

    private void loadRetentionDetails() {
        dashboardService.getGlobalStats(currentUserId, new DashboardService.DashboardDataCallback<DeckStats>() {
            @Override
            public void onDataLoaded(DeckStats stats) {
                int total = stats.totalAcertos + stats.totalErros;
                totalStudied.setText(String.valueOf(total));
                correctAnswers.setText(String.valueOf(stats.totalAcertos));
                incorrectAnswers.setText(String.valueOf(stats.totalErros));
            }

            @Override
            public void onError(Exception e) {
                Log.e("RetentionDetails", "Erro ao carregar detalhes de retenção", e);
                Toast.makeText(RetentionDetailsActivity.this, "Não foi possível carregar os detalhes.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
