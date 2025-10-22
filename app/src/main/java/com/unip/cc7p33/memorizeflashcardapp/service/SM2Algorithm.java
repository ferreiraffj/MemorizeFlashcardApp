package com.unip.cc7p33.memorizeflashcardapp.service;

import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.Calendar;
import java.util.Date;

public class SM2Algorithm {
    /**
     * Aplica o algoritmo SM2 ao flashcard baseado na qualidade da resposta.
     * @param card O flashcard a ser atualizado.
     * @param quality Qualidade da resposta: 0 (difícil, erro), 1-2 (difícil), 3 (correto), 4-5 (fácil).
     */

    public static void applySM2(Flashcard card, int quality) {
        if (card == null) return;

        double facilidade = card.getFacilidade();
        int repeticoes = card.getRepeticoes();

        // Ajusta facilidade baseado na qualidade (mínimo 1.3)
        facilidade = Math.max(1.3, facilidade +(0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02)));

        // Se qualidade >= 3 (correto), incrementa repetições e calcula intervalo
        if (quality >= 3){
            repeticoes++;
            int intervalo;
            if (repeticoes == 1){
                intervalo = 1; // Primeiro acerto: 1 dia
            } else if (repeticoes == 2){
                intervalo = 6; // Segundo: 6 dias
            } else {
                intervalo = (int) Math.round(card.getIntervalo() * facilidade); // Próximos: intervalo * facilidade
            }
            card.setIntervalo(intervalo);
            
            // Calcula próxima revisão
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date()); // Data atual
            cal.add(Calendar.DAY_OF_MONTH, intervalo);
            card.setProximaRevisao(cal.getTime());
        } else {
            // Se incorreta, reseta repetições e intervalo
            repeticoes = 0;
            card.setIntervalo(1);
            card.setProximaRevisao(new Date()); // Hoje
        }

        // Atualiza campos no card
        card.setFacilidade(facilidade);
        card.setRepeticoes(repeticoes);
    }
}
