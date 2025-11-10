package com.unip.cc7p33.memorizeflashcardapp.service;

import android.content.Context;import android.util.Pair;

import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.BaralhoDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.DeckStats;
import com.unip.cc7p33.memorizeflashcardapp.model.EstudoDiario;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardService {

    private final UsuarioDAO usuarioDAO;
    private final FlashcardDAO flashcardDAO;
    private final BaralhoDAO baralhoDAO;
    private final ExecutorService executor;

    // Construtor que recebe as dependências (DAOs)
    public DashboardService(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.usuarioDAO = db.usuarioDAO();
        this.flashcardDAO = db.flashcardDAO();
        this.baralhoDAO = db.baralhoDAO();
        this.executor = Executors.newSingleThreadExecutor();
    }

    // Interface para o callback que retornará o resultado das buscas
    public interface DashboardDataCallback<T> {
        void onDataLoaded(T data);
        void onError(Exception e);
    }

    // --- MÉTODOS PARA BUSCAR AS MÉTRICAS ---

    // 1. Buscar a Ofensiva (simples, busca o usuário)
    public void getOfensiva(String userId, DashboardDataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                Usuario usuario = usuarioDAO.getUserByUID(userId);
                // Posta o resultado na thread principal
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    if (usuario != null) {
                        callback.onDataLoaded(usuario.getOfensiva());
                    } else {
                        callback.onDataLoaded(0);
                    }
                });
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // 2. Calcular Cartas Maduras
    public void getCartasMadurasCount(String userId, DashboardDataCallback<Integer> callback) {
        final int DIAS_PARA_SER_MADURA = 21; // Definimos que uma carta é "madura" após 21 dias de intervalo
        executor.execute(() -> {
            try {
                int count = flashcardDAO.getMatureCardsCount(userId, DIAS_PARA_SER_MADURA);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(count));
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // 3. Encontrar o Melhor Baralho (maior taxa de acertos)
    public void getMelhorBaralho(String userId, DashboardDataCallback<String> callback) {
        executor.execute(() -> {
            try {
                List<Baralho> baralhos = baralhoDAO.getAllDecksSync(userId);
                if (baralhos == null || baralhos.isEmpty()) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded("Nenhum"));
                    return;
                }

                Baralho melhorBaralho = null;
                double maiorTaxa = -1.0;

                // ##### INÍCIO DA CORREÇÃO #####
                for (Baralho baralho : baralhos) {
                    DeckStats stats = flashcardDAO.getDeckStats(baralho.getBaralhoId());
                    int totalAcertos = (stats != null) ? stats.totalAcertos : 0;
                    int totalErros = (stats != null) ? stats.totalErros : 0;
                    int totalRespostas = totalAcertos + totalErros;

                    // Adiciona a verificação para evitar divisão por zero
                    if (totalRespostas > 0) {
                        double taxaAtual = (double) totalAcertos / totalRespostas;
                        if (taxaAtual > maiorTaxa) {
                            maiorTaxa = taxaAtual;
                            melhorBaralho = baralho;
                        }
                    }
                } // A chave do 'for' agora fecha no lugar certo.

                final String nomeMelhorBaralho = (melhorBaralho != null) ? melhorBaralho.getNome() : "Nenhum";
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(nomeMelhorBaralho));
                // ##### FIM DA CORREÇÃO #####

            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // 4. Calcular Taxa de Retenção Estimada
    public void getTaxaRetencao(String userId, DashboardDataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                // Estatísticas de cartas que já foram revisadas pelo menos uma vez
                DeckStats stats = flashcardDAO.getReviewedCardsStats(userId);
                int acertosRevisao = (stats != null) ? stats.totalAcertos : 0;
                int errosRevisao = (stats != null) ? stats.totalErros : 0;
                int totalRevisoes = acertosRevisao + errosRevisao;

                int taxaRetencao = 0;
                if (totalRevisoes > 0) {
                    taxaRetencao = (int) (((double) acertosRevisao / totalRevisoes) * 100);
                }

                final int finalTaxaRetencao = taxaRetencao;
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(finalTaxaRetencao));
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // 5. Buscar dados para o gráfico de progresso
    public void getProgressoEstudo(String userId, DashboardDataCallback<List<EstudoDiario>> callback) {
        executor.execute(() -> {
            try {
                // Define a data de início para 7 dias atrás
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -7);
                long dataInicio = calendar.getTimeInMillis();

                List<EstudoDiario> dados = flashcardDAO.getContagemEstudoDiario(userId, dataInicio);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(dados));

            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }
}
