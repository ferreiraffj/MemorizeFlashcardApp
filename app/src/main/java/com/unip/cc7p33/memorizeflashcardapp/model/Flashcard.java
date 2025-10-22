package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "flashcard")
public class Flashcard implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int flashcardId;

    private String frente;
    private String verso;
    private String deckId;  // Novo: associa carta ao deck


    // Campos para SM2
    private int repeticoes = 0;
    private double facilidade = 2.5;
    private int intervalo = 1;
    private Date proximaRevisao;

    // Campos para métricas básicas
    private int acertos = 0;
    private int erros = 0;

    // Construtores
    public Flashcard() {}

    @Ignore  // Adicionado para evitar aviso de múltiplos construtores
    public Flashcard(String frente, String verso, String deckId) {
        this.frente = frente;
        this.verso = verso;
        this.deckId = deckId;
        this.proximaRevisao = new Date();
    }

    // Getters e Setters (garanta que todos estejam presentes e corretos)
    public int getFlashcardId() { return flashcardId; }
    public void setFlashcardId(int flashcardId) { this.flashcardId = flashcardId; }

    public String getFrente() { return frente; }
    public void setFrente(String frente) { this.frente = frente; }

    public String getVerso() { return verso; }
    public void setVerso(String verso) { this.verso = verso; }

    public String getDeckId() { return deckId; }  // Novo
    public void setDeckId(String deckId) { this.deckId = deckId; }  // Novo

    public int getRepeticoes() { return repeticoes; }
    public void setRepeticoes(int repeticoes) { this.repeticoes = repeticoes; }

    public double getFacilidade() { return facilidade; }
    public void setFacilidade(double facilidade) { this.facilidade = facilidade; }

    public int getIntervalo() { return intervalo; }
    public void setIntervalo(int intervalo) { this.intervalo = intervalo; }

    public Date getProximaRevisao() { return proximaRevisao; }
    public void setProximaRevisao(Date proximaRevisao) { this.proximaRevisao = proximaRevisao; }

    public int getAcertos() { return acertos; }
    public void setAcertos(int acertos) { this.acertos = acertos; }

    public int getErros() { return erros; }
    public void setErros(int erros) { this.erros = erros; }
}