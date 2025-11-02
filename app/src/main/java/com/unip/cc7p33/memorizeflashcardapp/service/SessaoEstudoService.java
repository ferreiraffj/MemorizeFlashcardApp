package com.unip.cc7p33.memorizeflashcardapp.service;

import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

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
}