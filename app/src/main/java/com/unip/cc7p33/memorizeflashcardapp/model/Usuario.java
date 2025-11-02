package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.DocumentId;

import java.util.Date;

@Entity(tableName = "usuario")
public class Usuario {
    @PrimaryKey
    @DocumentId
    @NonNull
    private String uid; // ID do usuário, que será o mesmo do Firebase Auth
    private String nome;
    private String email;
    private int diasConsecutivos;
    private Date ultimoAcesso;

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
    public int getDiasConsecutivos() { return diasConsecutivos; }
    public void setDiasConsecutivos(int diasConsecutivos) { this.diasConsecutivos = diasConsecutivos; }
    public Date getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(Date ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }
}
