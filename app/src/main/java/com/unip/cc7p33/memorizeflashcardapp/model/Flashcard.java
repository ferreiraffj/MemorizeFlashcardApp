package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Flashcard {

    @DocumentId
    @NonNull
    private String id;

    private String frente;
    private String verso;
    private String tipo;

    @ServerTimestamp
    private Date timestamp;

    public Flashcard() {
        this.id = "";
    }

    public Flashcard(String frente, String verso, String tipo) {
        this();
        this.frente = frente;
        this.verso = verso;
        this.tipo = tipo;
    }

    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getFrente() { return frente; }
    public void setFrente(String frente) { this.frente = frente; }
    public String getVerso() { return verso; }
    public void setVerso(String verso) { this.verso = verso; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}