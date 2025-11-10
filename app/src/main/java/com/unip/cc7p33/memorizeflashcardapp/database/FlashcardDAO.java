package com.unip.cc7p33.memorizeflashcardapp.database;

import androidx.core.util.Pair;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.unip.cc7p33.memorizeflashcardapp.model.DeckStats;
import com.unip.cc7p33.memorizeflashcardapp.model.EstudoDiario;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.List;

@Dao
public interface FlashcardDAO {
    // OnConflictStrategy.REPLACE faz com que a inserção de uma
    // carta com um ID já existente simplesmente a atualize.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Flashcard flashcard);

    // Permite inserir uma lista de cartas de uma vez
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Flashcard> flashcards);

    @Update
    void update(Flashcard flashcard);

    @Delete
    void delete(Flashcard flashcard);

    @Query("SELECT * FROM flashcard WHERE flashcardId = :id")
    Flashcard getById(String id); // Mudar o tipo do parâmetro para String

    @Query("SELECT * FROM flashcard WHERE deckId = :deckId")  // Novo: filtra por deck
    List<Flashcard> getByDeckId(String deckId);

    @Query("SELECT * FROM flashcard")
    List<Flashcard> getAll();

    @Query("SELECT * FROM flashcard WHERE deckId = :deckId AND (repeticoes = 0 OR proximaRevisao <= :now)")
    List<Flashcard> getCardsForStudy(String deckId, long now);

    // Adicione esta anotação e metodo para contar cartas maduras
    @Query("SELECT COUNT(*) FROM flashcard WHERE userId = :userId AND facilidade > :diasParaSerMadura")
    int getMatureCardsCount(String userId, int diasParaSerMadura);

    // Adicione este metodo para pegar estatísticas (acertos, erros) de um baralho
    @Query("SELECT SUM(acertos) as total_acertos, SUM(erros) as total_erros FROM flashcard WHERE deckId = :deckId")
    DeckStats getDeckStats(String deckId);
    // Adicione este método para pegar estatísticas de cartas já revisadas
    @Query("SELECT SUM(acertos) as total_acertos, SUM(erros) as total_erros FROM flashcard WHERE userId = :userId AND repeticoes > 1")
    DeckStats getReviewedCardsStats(String userId);

    /**
     * Retorna a contagem de cartas estudadas por dia nos últimos 'N' dias.
     * Usa a data de 'proximaRevisao' como indicador de quando uma carta foi estudada.
     * ATENÇÃO: A data é armazenada como um timestamp (Long), então filtramos por ele.
     */
    @Query("SELECT date(proximaRevisao / 1000, 'unixepoch') as dia_estudo, COUNT(*) as contagem " +
            "FROM flashcard " +
            "WHERE userId = :userId AND proximaRevisao >= :dataInicio " +
            "GROUP BY dia_estudo " +
            "ORDER BY dia_estudo ASC")
    List<EstudoDiario> getContagemEstudoDiario(String userId, long dataInicio);
}