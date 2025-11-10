package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.room.ColumnInfo;

public class DeckStats {
    @ColumnInfo(name = "total_acertos")
    public int totalAcertos;

    @ColumnInfo(name = "total_erros")
    public int totalErros;
}
