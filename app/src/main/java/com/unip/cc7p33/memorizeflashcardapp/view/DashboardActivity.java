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

        // 3. Inicializa o serviço que contém toda a lógica
        dashboardService = new DashboardService(this);

        // 4. Conecta os TextViews do layout do dashboard
        tvDashboardOfensiva = findViewById(R.id.tv_dashboard_ofensiva);
        tvDashboardCartasMaduras = findViewById(R.id.tv_dashboard_cartas_maduras);
        tvDashboardMelhorBaralho = findViewById(R.id.tv_dashboard_melhor_baralho);
        tvDashboardRetencao = findViewById(R.id.tv_dashboard_retencao);
        barChart = findViewById(R.id.chart_progresso_estudo);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            loadAllDashboardMetrics();
        } else {
            Toast.makeText(this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            finish(); // Fecha a activity se não houver usuário logado
        }
    }

    /**
     * Orquestra o carregamento de todas as métricas do dashboard,
     * chamando os métodos correspondentes da DashboardService.
     */
    private void loadAllDashboardMetrics() {
        // Carrega cada métrica individualmente
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
                // Formata o texto para exibição
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
        // 1. Preparar os dados para o gráfico
        ArrayList<BarEntry> entries = new ArrayList<>();
        final String[] diasDaSemana = new String[]{"Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"};
        int[] contagemPorDia = new int[7]; // Array para guardar a contagem de cada dia da semana

        Calendar cal = Calendar.getInstance();

        // Preenche o array de contagem com os dados do banco
        for (EstudoDiario dia : dados) {
            cal.setTime(dia.diaEstudo);
            // Calendar.DAY_OF_WEEK é 1-indexado (Dom=1, Seg=2, ...), então subtraímos 1
            int diaIndex = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (diaIndex >= 0 && diaIndex < 7) {
                contagemPorDia[diaIndex] = dia.contagem;
            }
        }

        // Cria as entradas (barras) para cada dia da semana
        for (int i = 0; i < 7; i++) {
            entries.add(new BarEntry(i, contagemPorDia[i]));
        }

        // 2. Criar e customizar o DataSet (a aparência das barras)
        BarDataSet dataSet = new BarDataSet(entries, "Cartas Estudadas");
        dataSet.setColor(getResources().getColor(R.color.blueStripe)); // Cor das barras
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setValueTextSize(12f);

        // 3. Colocar os dados no gráfico
        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f); // Largura das barras (0.0 a 1.0)
        barChart.setData(barData);

        // 4. Customizar a aparência do gráfico
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false); // Remove o eixo Y da direita
        barChart.getLegend().setEnabled(false); // Remove a legenda colorida abaixo do gráfico
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setScaleEnabled(false); // Impede o zoom no gráfico
        barChart.setExtraBottomOffset(10f); // Adiciona um pequeno espaço na parte inferior

        // Customizar eixo Y (Esquerdo)
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGranularity(1.0f); // Mostra apenas números inteiros (1, 2, 3...)
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setTextSize(12f);
        barChart.getAxisLeft().setAxisLineColor(getResources().getColor(android.R.color.darker_gray));
        barChart.getAxisLeft().setTextColor(getResources().getColor(android.R.color.darker_gray));

        // Customizar eixo X (Inferior)
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

        // 5. Animar e atualizar o gráfico
        barChart.animateY(1500); // Animação vertical das barras
        barChart.invalidate(); // Atualiza o gráfico na tela
    }

}
