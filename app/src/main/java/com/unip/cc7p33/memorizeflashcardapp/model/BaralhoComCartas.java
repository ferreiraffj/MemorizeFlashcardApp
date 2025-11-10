package com.unip.cc7p33.memorizeflashcardapp.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

// Esta classe não é uma entidade, mas um DTO para juntar dados.
public class BaralhoComCartas {

    @Embedded
    public Baralho baralho;

    @Relation(
            parentColumn = "baralhoId",
            entityColumn = "deckId"
    )
    public List<Flashcard> flashcards;
}
