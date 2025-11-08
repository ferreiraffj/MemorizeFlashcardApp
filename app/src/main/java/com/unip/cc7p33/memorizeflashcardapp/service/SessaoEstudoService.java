package com.unip.cc7p33.memorizeflashcardapp.service;

import android.content.Context;

import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.model.RankingInfo;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SessaoEstudoService {

    private List<Flashcard> cardList;
    private int currentCardIndex = 0;
    private int correctAnswersCount = 0;
    private FlashcardDAO flashcardDAO;  // Novo campo para DAO
    private ExecutorService executorService = Executors.newSingleThreadExecutor();  // Para operações assíncronas
    private boolean estudouHoje = false;
    private Context context;  // Novo: para acessar AppDatabase

    public void setContext(Context context) {
        this.context = context;
    }

    public void setFlashcardDAO(FlashcardDAO dao) {
        this.flashcardDAO = dao;
    }

    // Inicia a sessão com a lista de cartas (shuffle incluído)
    public void iniciarSessao(List<Flashcard> cards) {
        // Filtrar apenas cartas para estudo (novas ou vencidas)
        long now = new Date().getTime();
        this.cardList = cards.stream()
                .filter(card -> card.getRepeticoes() == 0 || (card.getProximaRevisao() != null && card.getProximaRevisao().getTime() <= now))
                .collect(Collectors.toList());

        if (cardList != null && !cardList.isEmpty()) {
            Collections.shuffle(cardList);  // Shuffle apenas as cartas do dia
            currentCardIndex = 0;
            correctAnswersCount = 0;
        }
    }

    // Retorna a carta atual (frente)
    public Flashcard obterCartaAtual() {
        if (cardList != null && currentCardIndex < cardList.size()) {
            return cardList.get(currentCardIndex);
        }
        return null; // Sessão finalizada
    }

    // Avança para a próxima carta, registrando se foi correta e salvando progresso
    public void avancarCarta(int quality) {
        Flashcard card = obterCartaAtual();
        if (card != null) {
            if (quality >= 3) {
                card.setAcertos(card.getAcertos() + 1);
                correctAnswersCount++;  // Para estatísticas
            } else {
                card.setErros(card.getErros() + 1);
            }
            SM2Algorithm.applySM2(card, quality);  // Aplica SM2 com quality
            estudouHoje = true;
            if (flashcardDAO != null) {
                executorService.execute(() -> flashcardDAO.update(card));
            }
        }
        currentCardIndex++;
    }

    // Verifica se a sessão terminou
    public boolean sessaoFinalizada() {
        return cardList == null || currentCardIndex >= cardList.size();
    }

    // Retorna estatísticas finais
    public String obterEstatisticas() {
        if (cardList != null) {
            return "ACERTOS: " + correctAnswersCount + "/" + cardList.size();
        }
        return "Nenhuma sessão iniciada.";
    }

    // Getters para UI
    public int getCurrentCardIndex() {
        return currentCardIndex;
    }

    public int getTotalCards() {
        return cardList != null ? cardList.size() : 0;
    }

//    public static String calcularRanking(int xp) {
//        if (xp < 100) return "Bronze";
//        if (xp < 200) return "Prata";
//        if (xp < 300) return "Ouro";
//        if (xp < 400) return "Platina";
//        if (xp < 500) return "Diamante";
//        if (xp < 600) return "Safira";
//        return "Rubi";
//    }
    public static RankingInfo getRankingInfo(int xp){
        String currentRank, nextRank;
        int currentRankXp, nextRankXp;

        if (xp < 100) {
            currentRank = "Bronze"; currentRankXp = 0;
            nextRank = "Prata"; nextRankXp = 100;
        } else if (xp < 200) {
            currentRank = "Prata"; currentRankXp = 100;
            nextRank = "Ouro"; nextRankXp = 200;
        } else if (xp < 300) {
            currentRank = "Ouro"; currentRankXp = 200;
            nextRank = "Platina"; nextRankXp = 300;
        } else if (xp < 400) {
            currentRank = "Platina"; currentRankXp = 300;
            nextRank = "Diamante"; nextRankXp = 400;
        } else if (xp < 500) {
            currentRank = "Diamante"; currentRankXp = 400;
            nextRank = "Safira"; nextRankXp = 500;
        } else if (xp < 600) {
            currentRank = "Safira"; currentRankXp = 500;
            nextRank = "Rubi"; nextRankXp = 600;
        } else {
            currentRank = "Rubi"; currentRankXp = 600;
            nextRank = "Rubi"; nextRankXp = 600; // Ranking máximo
        }
        return new RankingInfo(currentRank, nextRank, currentRankXp, nextRankXp, xp);
    }

    public static boolean jaEstudouHoje(Date ultimoEstudo) {
        if (ultimoEstudo == null) return false;

        // Configura o calendário para o início do dia de hoje (00:00:00)
        Calendar hojeInicioDoDia = Calendar.getInstance();
        hojeInicioDoDia.set(Calendar.HOUR_OF_DAY, 0);
        hojeInicioDoDia.set(Calendar.MINUTE, 0);
        hojeInicioDoDia.set(Calendar.SECOND, 0);
        hojeInicioDoDia.set(Calendar.MILLISECOND, 0);

        // Se a data do último estudo NÃO for anterior ao início de hoje,
        // significa que o estudo de hoje já foi registrado.
        // Ex: ultimoEstudo = hoje às 10h. `! (hoje_10h < hoje_00h)` -> `! (false)` -> true.
        return !ultimoEstudo.before(hojeInicioDoDia.getTime());
    }

    public void marcarEstudo(String userid, Runnable onComplete) {
        // Se a flag 'estudouHoje' não foi ativada (nenhum card avançou), não faz nada.
        // E se o contexto for nulo, também não podemos fazer nada.
        if (!estudouHoje || context == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        // +10xp por carta estudada
        int xpGanho = getTotalCards() * 10; // Total de cartas da sessão

        executorService.execute(() -> {
            UsuarioDAO usuarioDAO = AppDatabase.getInstance(context).usuarioDAO();
            Usuario usuario = usuarioDAO.getUserByUID(userid);

            if (usuario != null){
                usuario.setXp(usuario.getXp() + xpGanho);
                usuario.setRanking(SessaoEstudoService.getRankingInfo(usuario.getXp()).getCurrentRankName());

                // A verificação se a ofensiva deve ser incrementada
                if (!SessaoEstudoService.jaEstudouHoje(usuario.getUltimoEstudo())) {
                    usuario.setOfensiva(usuario.getOfensiva() + 1);
                }

                Calendar hojeInicioDoDia = Calendar.getInstance();
                hojeInicioDoDia.set(Calendar.HOUR_OF_DAY, 0);
                hojeInicioDoDia.set(Calendar.MINUTE, 0);
                hojeInicioDoDia.set(Calendar.SECOND, 0);
                hojeInicioDoDia.set(Calendar.MILLISECOND, 0);
                usuario.setUltimoEstudo(hojeInicioDoDia.getTime());

                // Pega a data do último estudo do usuário. Se nunca estudou, será null.
                Date ultimoEstudo = usuario.getUltimoEstudo();

                /*
                * Verifica se a ofensiva deve ser incrementada
                * Condições:
                * 1. O usuário NUNCA estudou antes (ultimoEstudo == null)
                * 2. O último estudo foi ANTES do início do dia de hoje
                * */
                if (ultimoEstudo == null || ultimoEstudo.before(hojeInicioDoDia.getTime())) {
                    // Este é o primeiro estudo de hoje, então incrementa a ofensiva.
                    usuario.setOfensiva(usuario.getOfensiva() + 1);
                }

                // Atualiza a data do último estudo para o início do dia de hoje.
                // Salvar a data zerada garante que qualquer estudo futuro no mesmo dia não incremente a ofensiva novamente.
                usuario.setUltimoEstudo(hojeInicioDoDia.getTime());

                usuarioDAO.update(usuario);
            }
            estudouHoje = false; // Reseta para a próxima sessão
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }
}