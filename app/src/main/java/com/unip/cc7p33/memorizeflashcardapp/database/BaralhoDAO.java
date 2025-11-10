package com.unip.cc7p33.memorizeflashcardapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

import java.util.List;

@Dao
public interface BaralhoDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Baralho baralho);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Baralho> baralhos);

    @Query("DELETE FROM baralhos")
    void deleteAll();

    @Update
    void update(Baralho baralho);

    @Query("SELECT * FROM baralhos WHERE usuario_id = :userId")
    List<Baralho> getByUserId(String userId);

    @Query("SELECT * FROM baralhos WHERE baralhoId = :id")
    Baralho getById(String id);

    @Query("DELETE FROM baralhos WHERE baralhoId = :id")
    void deleteById(String id);

    @Query("SELECT COUNT(*) FROM flashcard WHERE deckId = :deckId AND erros > 0")
    int getErradasCount(String deckId);

    @Query("SELECT COUNT(*) FROM flashcard WHERE deckId = :deckId AND proximaRevisao IS NOT NULL AND proximaRevisao < :now")
    int getProximasCount(String deckId, long now);  // Só vencidas

    @Query("SELECT COUNT(*) FROM flashcard WHERE deckId = :deckId AND erros = 0 AND (proximaRevisao IS NULL OR proximaRevisao >= :now)")
    int getNovasCount(String deckId, long now);  // Sem erros e não vencidas

    @Query("SELECT COUNT(*) FROM flashcard WHERE deckId = :deckId AND repeticoes = 0")
    int getNovasCount(String deckId);  // Apenas novas (nunca revisadas)

    // Adicione este método para buscar todos os baralhos de forma síncrona (para uso em threads de background)
    @Query("SELECT * FROM baralhos WHERE usuario_id = :userId")
    List<Baralho> getAllDecksSync(String userId);

}