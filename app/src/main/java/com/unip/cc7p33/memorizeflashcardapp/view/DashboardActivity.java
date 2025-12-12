package com.unip.cc7p33.memorizeflashcardapp.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.EstudoDiario;
import com.unip.cc7p33.memorizeflashcardapp.service.DashboardService;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvDashboardOfensiva, tvDashboardCartasMaduras, tvDashboardMelhorBaralho, tvDashboardRetencao;
    private BarChart barChart;
    private CardView cardConhecimentoSolido, cardGraficoProgresso, cardRetention;
    private DashboardService dashboardService;
    private String currentUserId;
    private int matureCardsCount = 0; // Variável para armazenar a contagem

    // Firebase Auth Listener
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SystemUIUtils.setImmersiveMode(this);

        dashboardService = new DashboardService(this);
        mAuth = FirebaseAuth.getInstance();

        setupViews();
        setupClickListeners(); // Novo método para os cliques
        setupAuthListener();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupAuthListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.d("DashboardActivity", "AuthState: signed_in:" + user.getUid());
                currentUserId = user.getUid();
                loadAllDashboardMetrics();
            } else {
                Log.d("DashboardActivity", "AuthState: signed_out");
                Toast.makeText(this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };
    }

    private void setupViews() {
        tvDashboardOfensiva = findViewById(R.id.tv_dashboard_ofensiva);
        tvDashboardCartasMaduras = findViewById(R.id.tv_dashboard_cartas_maduras);
        tvDashboardMelhorBaralho = findViewById(R.id.tv_dashboard_melhor_baralho);
        tvDashboardRetencao = findViewById(R.id.tv_dashboard_retencao);
        barChart = findViewById(R.id.chart_progresso_estudo);
        cardConhecimentoSolido = findViewById(R.id.card_conhecimento_solido);
        cardGraficoProgresso = findViewById(R.id.card_grafico_progresso);
        cardRetention = findViewById(R.id.card_retention);

        cardGraficoProgresso.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        cardConhecimentoSolido.setOnClickListener(v -> {
            if (matureCardsCount > 0) {
                Intent intent = new Intent(DashboardActivity.this, MatureCardsActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Você ainda não tem cartas com conhecimento sólido.", Toast.LENGTH_SHORT).show();
            }
        });

        cardRetention.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, RetentionDetailsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            loadAllDashboardMetrics();
        }
    }

    private void loadAllDashboardMetrics() {
        if (currentUserId == null) return;
        loadOfensiva();
        loadCartasMaduras();
        loadMelhorBaralho();
        loadTaxaRetencao();
        loadProgressoEstudo();
    }

    private void loadOfensiva() {
        dashboardService.getOfensiva(currentUserId, new DashboardService.DashboardDataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
                tvDashboardOfensiva.setText(String.format("%d dias", data));
            }
            @Override
            public void onError(Exception e) {
                Log.e("DashboardActivity", "Erro ao carregar ofensiva", e);
                tvDashboardOfensiva.setText("Erro");
            }
        });
    }

    private void loadCartasMaduras() {
        dashboardService.getCartasMadurasCount(currentUserId, new DashboardService.DashboardDataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
                matureCardsCount = data; // Armazena a contagem
                tvDashboardCartasMaduras.setText(String.format("%d cartas", data));
            }
            @Override
            public void onError(Exception e) {
                Log.e("DashboardActivity", "Erro ao carregar cartas maduras", e);
                tvDashboardCartasMaduras.setText("Erro");
            }
        });
    }

    private void loadMelhorBaralho() {
        dashboardService.getMelhorBaralho(currentUserId, new DashboardService.DashboardDataCallback<String>() {
            @Override
            public void onDataLoaded(String data) {
                tvDashboardMelhorBaralho.setText(data);
            }
            @Override
            public void onError(Exception e) {
                Log.e("DashboardActivity", "Erro ao carregar melhor baralho", e);
                tvDashboardMelhorBaralho.setText("Erro");
            }
        });
    }

    private void loadTaxaRetencao() {
        dashboardService.getTaxaRetencao(currentUserId, new DashboardService.DashboardDataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
                tvDashboardRetencao.setText(String.format("%d%%", data));
            }
            @Override
            public void onError(Exception e) {
                Log.e("DashboardActivity", "Erro ao carregar taxa de retenção", e);
                tvDashboardRetencao.setText("Erro");
            }
        });
    }

    private void loadProgressoEstudo() {
        dashboardService.getProgressoEstudo(currentUserId, new DashboardService.DashboardDataCallback<List<EstudoDiario>>() {
            @Override
            public void onDataLoaded(List<EstudoDiario> dados) {
                if (dados == null) {
                    barChart.clear();
                    barChart.setNoDataText("Sem dados de estudo nos últimos 7 dias.");
                    barChart.invalidate();
                    return;
                }
                setupBarChart(dados);
            }

            @Override
            public void onError(Exception e) {
                Log.e("DashboardActivity", "Erro ao carregar progresso de estudo", e);
                barChart.setNoDataText("Erro ao carregar dados do gráfico.");
            }
        });
    }

    private void setupBarChart(List<EstudoDiario> dados) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        final String[] diasDaSemanaNomes = new String[]{"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        final String[] labels = new String[7];

        Map<String, Integer> contagemPorDia = new HashMap<>();
        Calendar cal = Calendar.getInstance();

        for (EstudoDiario dia : dados) {
            cal.setTime(dia.diaEstudo);
            String chave = cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
            contagemPorDia.put(chave, dia.contagem);
        }

        Calendar diaCorrente = Calendar.getInstance();
        diaCorrente.add(Calendar.DAY_OF_YEAR, -6);

        for (int i = 0; i < 7; i++) {
            String chave = diaCorrente.get(Calendar.YEAR) + "-" + diaCorrente.get(Calendar.DAY_OF_YEAR);
            int contagem = contagemPorDia.getOrDefault(chave, 0);
            entries.add(new BarEntry(i, contagem));

            int diaDaSemanaIndex = diaCorrente.get(Calendar.DAY_OF_WEEK) - 1;
            labels[i] = diasDaSemanaNomes[diaDaSemanaIndex];

            diaCorrente.add(Calendar.DAY_OF_YEAR, 1);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Cartas Estudadas");
        dataSet.setColor(getResources().getColor(R.color.blueStripe));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(false);
        barChart.setExtraBottomOffset(10f);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGranularity(1.0f);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setTextSize(12f);
        barChart.getAxisLeft().setAxisLineColor(getResources().getColor(android.R.color.darker_gray));
        barChart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.darker_gray));

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextSize(12f);
        xAxis.setAxisLineColor(getResources().getColor(android.R.color.darker_gray));
        xAxis.setTextColor(getResources().getColor(android.R.color.darker_gray));

        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.length) {
                    return labels[(int) value];
                }
                return "";
            }
        });

        barChart.animateY(1500);
        barChart.invalidate();
    }
}
