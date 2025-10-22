package com.unip.cc7p33.memorizeflashcardapp.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.List;

@Dao
public interface FlashcardDAO {

    @Insert
    void insert(Flashcard flashcard);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcard WHERE flashcardId = :id")
    Flashcard getById(int id);

    @Query("SELECT * FROM flashcard WHERE deckId = :deckId")  // Novo: filtra por deck
    List<Flashcard> getByDeckId(String deckId);

    @Query("SELECT * FROM flashcard")
    List<Flashcard> getAll();
}