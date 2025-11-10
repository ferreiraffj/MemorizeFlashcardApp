package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.unip.cc7p33.memorizeflashcardapp.model.EstudoDiario;
import com.unip.cc7p33.memorizeflashcardapp.service.DashboardService;
import com.unip.cc7p33.memorizeflashcardapp.utils.SystemUIUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvDashboardOfensiva, tvDashboardCartasMaduras, tvDashboardMelhorBaralho, tvDashboardRetencao;
    private BarChart barChart;
    private DashboardService dashboardService;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SystemUIUtils.hideStatusBar(this);

        dashboardService = new DashboardService(this);

        tvDashboardOfensiva = findViewById(R.id.tv_dashboard_ofensiva);
        tvDashboardCartasMaduras = findViewById(R.id.tv_dashboard_cartas_maduras);
        tvDashboardMelhorBaralho = findViewById(R.id.tv_dashboard_melhor_baralho);
        tvDashboardRetencao = findViewById(R.id.tv_dashboard_retencao);
        barChart = findViewById(R.id.chart_progresso_estudo);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            Toast.makeText(this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null && !userId.isEmpty()) {
            loadAllDashboardMetrics();
        }
    }

    private void loadAllDashboardMetrics() {
        loadOfensiva();
        loadCartasMaduras();
        loadMelhorBaralho();
        loadTaxaRetencao();
        loadProgressoEstudo();
    }

    private void loadOfensiva() {
        dashboardService.getOfensiva(userId, new DashboardService.DashboardDataCallback<Integer>() {
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
        dashboardService.getCartasMadurasCount(userId, new DashboardService.DashboardDataCallback<Integer>() {
            @Override
            public void onDataLoaded(Integer data) {
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
        dashboardService.getMelhorBaralho(userId, new DashboardService.DashboardDataCallback<String>() {
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
        dashboardService.getTaxaRetencao(userId, new DashboardService.DashboardDataCallback<Integer>() {
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
        dashboardService.getProgressoEstudo(userId, new DashboardService.DashboardDataCallback<List<EstudoDiario>>() {
            @Override
            public void onDataLoaded(List<EstudoDiario> dados) {
                if (dados == null || dados.isEmpty()) {
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
        final String[] diasDaSemana = new String[]{"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        int[] contagemPorDia = new int[7];

        Calendar cal = Calendar.getInstance();

        for (EstudoDiario dia : dados) {
            cal.setTime(dia.diaEstudo);
            int diaIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (diaIndex >= 0 && diaIndex < 7) {
                contagemPorDia[diaIndex] = dia.contagem;
            }
        }

        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, contagemPorDia[i]));
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
                if (value >= 0 && value < diasDaSemana.length) {
                    return diasDaSemana[(int) value];
                }
                return "";
            }
        });

        barChart.animateY(1500);
        barChart.invalidate();
    }

}
