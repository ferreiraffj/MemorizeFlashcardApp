// model/Baralho.java
package com.unip.cc7p33.memorizeflashcardapp.model;

public class Baralho {
    private String nome;
    private int quantidadeCartas;

    // Construtor
    public Baralho(String nome, int quantidadeCartas) {
        this.nome = nome;
        this.quantidadeCartas = quantidadeCartas;
    }

    // Getters e Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantidadeCartas() {
        return quantidadeCartas;
    }

    public void setQuantidadeCartas(int quantidadeCartas) {
        this.quantidadeCartas = quantidadeCartas;
    }
}