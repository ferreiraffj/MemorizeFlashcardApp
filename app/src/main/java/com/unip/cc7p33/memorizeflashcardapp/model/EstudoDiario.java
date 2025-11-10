package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.room.ColumnInfo;

import java.util.Date;

public class EstudoDiario {
    @ColumnInfo(name = "dia_estudo")
    public Date diaEstudo; // O dia do estudo

    @ColumnInfo(name = "contagem")
    public int contagem; // Quantas cartas foram estudadas naquele dia
}
