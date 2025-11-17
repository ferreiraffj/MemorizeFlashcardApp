package com.unip.cc7p33.memorizeflashcardapp.service;

import android.content.Context;import android.util.Pair;

import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.BaralhoDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.DeckStats;
import com.unip.cc7p33.memorizeflashcardapp.model.EstudoDiario;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;  // Adicionado
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

    public DashboardService(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.usuarioDAO = db.usuarioDAO();
        this.flashcardDAO = db.flashcardDAO();
        this.baralhoDAO = db.baralhoDAO();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface DashboardDataCallback<T> {
        void onDataLoaded(T data);
        void onError(Exception e);
    }

    public void getOfensiva(String userId, DashboardDataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
                Usuario usuario = usuarioDAO.getUserByUID(userId);
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

    public void getCartasMadurasCount(String userId, DashboardDataCallback<Integer> callback) {
        final int DIAS_PARA_SER_MADURA = 21;
        executor.execute(() -> {
            try {
                int count = flashcardDAO.getMatureCardsCount(userId, DIAS_PARA_SER_MADURA);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(count));
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    // 6. Buscar a lista de Cartas Maduras
    public void getMatureCards(String userId, DashboardDataCallback<List<Flashcard>> callback) {
        final int DIAS_PARA_SER_MADURA = 21;
        executor.execute(() -> {
            try {
                List<Flashcard> matureCards = flashcardDAO.getMatureCards(userId, DIAS_PARA_SER_MADURA);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(matureCards));
            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

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

                for (Baralho baralho : baralhos) {
                    DeckStats stats = flashcardDAO.getDeckStats(baralho.getBaralhoId());
                    int totalAcertos = (stats != null) ? stats.totalAcertos : 0;
                    int totalErros = (stats != null) ? stats.totalErros : 0;
                    int totalRespostas = totalAcertos + totalErros;

                    if (totalRespostas > 0) {
                        double taxaAtual = (double) totalAcertos / totalRespostas;
                        if (taxaAtual > maiorTaxa) {
                            maiorTaxa = taxaAtual;
                            melhorBaralho = baralho;
                        }
                    }
                }

                final String nomeMelhorBaralho = (melhorBaralho != null) ? melhorBaralho.getNome() : "Nenhum";
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onDataLoaded(nomeMelhorBaralho));

            } catch (Exception e) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    public void getTaxaRetencao(String userId, DashboardDataCallback<Integer> callback) {
        executor.execute(() -> {
            try {
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

    public void getProgressoEstudo(String userId, DashboardDataCallback<List<EstudoDiario>> callback) {
        executor.execute(() -> {
            try {
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
