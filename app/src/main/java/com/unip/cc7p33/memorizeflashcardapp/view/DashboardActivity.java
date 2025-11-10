package com.unip.cc7p33.memorizeflashcardapp.view;import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.unip.cc7p33.memorizeflashcardapp.model.EstudoDiario;
import com.unip.cc7p33.memorizeflashcardapp.service.DashboardService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvDashboardOfensiva, tvDashboardCartasMaduras, tvDashboardMelhorBaralho, tvDashboardRetencao;
    private LineChart lineChart;
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
        lineChart = findViewById(R.id.line_chart_progresso); // << ADICIONAR

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
                    lineChart.clear();
                    lineChart.setNoDataText("Sem dados de estudo nos últimos 7 dias.");
                    lineChart.invalidate();
                    return;
                }
                setupLineChart(dados);
            }

            @Override
            public void onError(Exception e) {
                Log.e("DashboardActivity", "Erro ao carregar progresso de estudo", e);
                lineChart.setNoDataText("Erro ao carregar dados do gráfico.");
            }
        });
    }

    private void setupLineChart(List<EstudoDiario> dados) {
        ArrayList<Entry> entries = new ArrayList<>();
        final ArrayList<String> labels = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        long dataReferencia = dados.get(0).diaEstudo.getTime();

        for (EstudoDiario dia : dados) {
            long diasDesdeReferencia = TimeUnit.MILLISECONDS.toDays(dia.diaEstudo.getTime() - dataReferencia);
            entries.add(new Entry(diasDesdeReferencia, dia.contagem));
            labels.add(sdf.format(dia.diaEstudo));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Cartas Estudadas");
        dataSet.setColor(getResources().getColor(R.color.blueStripe));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));
        dataSet.setCircleColor(getResources().getColor(R.color.blueStripe));
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customização do eixo X (datas)
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < labels.size()) {
                    // Recalcula o índice para corresponder ao da lista de labels
                    long diasDesdeReferencia = TimeUnit.MILLISECONDS.toDays(dados.get(index).diaEstudo.getTime() - dataReferencia);
                    if(diasDesdeReferencia == value){
                        return labels.get(index);
                    }
                }
                return "";
            }
        });

        // Customização geral
        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setAxisMinimum(0f);
        lineChart.animateX(1000);
        lineChart.invalidate(); // Refresh o gráfico
    }
}
