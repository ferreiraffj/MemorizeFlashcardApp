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
    private int xp;
    private String ranking;
    private Date ultimoEstudo;  // Último dia em que estudou cartas
    private int ofensiva;

    // Construtores, getters e setters...

    public Usuario() {
        // Construtor padrão necessário para o Firestore
    }

    public Usuario(String uid, String email, int diasConsecutivos, Date ultimoAcesso) {
        this.uid = uid;
        this.email = email;
        this.diasConsecutivos = diasConsecutivos;
        this.ultimoAcesso = ultimoAcesso;
        this.ofensiva = 0;  // Inicializa ofensiva
        this.ultimoEstudo = null;  // Inicializa
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
    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }
    public String getRanking() { return ranking; }
    public void setRanking(String ranking) { this.ranking = ranking; }
    public Date getUltimoEstudo() { return ultimoEstudo; }
    public void setUltimoEstudo(Date ultimoEstudo) { this.ultimoEstudo = ultimoEstudo; }
    public int getOfensiva() { return ofensiva; }
    public void setOfensiva(int ofensiva) { this.ofensiva = ofensiva; }
}
