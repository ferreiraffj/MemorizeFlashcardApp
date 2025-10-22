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
    private String baralhoId;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "quantidade_cartas")
    private int quantidadeCartas;

    @ColumnInfo(name = "usuario_id", index = true)
    private String usuarioId;


    public Baralho() {

        this.baralhoId = "";
    }

    @Ignore
    public Baralho(String nome, int quantidadeCartas, String usuarioId) {
        this();
        this.nome = nome;
        this.quantidadeCartas = quantidadeCartas;
        this.usuarioId = usuarioId;
    }


    @NonNull
    public String getBaralhoId() { return baralhoId; }  // Correção: de getId para getBaralhoId
    public void setBaralhoId(@NonNull String baralhoId) { this.baralhoId = baralhoId; }  // Correção: de setId para setBaralhoId
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public int getQuantidadeCartas() { return quantidadeCartas; }
    public void setQuantidadeCartas(int quantidadeCartas) { this.quantidadeCartas = quantidadeCartas; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}