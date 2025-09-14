package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.DocumentId;

@Entity(tableName = "baralhos",
        foreignKeys = @ForeignKey(entity = Usuario.class,
                parentColumns = "uid",
                childColumns = "usuario_id",
                onDelete = ForeignKey.CASCADE))
public class Baralho {

    @PrimaryKey
    @DocumentId
    @NonNull
    private String id;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "quantidade_cartas")
    private int quantidadeCartas;

    @ColumnInfo(name = "usuario_id", index = true)
    private String usuarioId;


    public Baralho() {

        this.id = "";
    }

    @Ignore
    public Baralho(String nome, int quantidadeCartas, String usuarioId) {
        this();
        this.nome = nome;
        this.quantidadeCartas = quantidadeCartas;
        this.usuarioId = usuarioId;
    }


    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getQuantidadeCartas() { return quantidadeCartas; }
    public void setQuantidadeCartas(int quantidadeCartas) { this.quantidadeCartas = quantidadeCartas; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}