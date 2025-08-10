package com.unip.cc7p33.memorizeflashcardapp.model;

import com.google.firebase.firestore.DocumentId;

public class Usuario {
    @DocumentId
    private String uid; // ID do usuário, que será o mesmo do Firebase Auth
    private String nome;
    private String email;

    public Usuario() {
        // Construtor padrão necessário para o Firestore
    }

    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
