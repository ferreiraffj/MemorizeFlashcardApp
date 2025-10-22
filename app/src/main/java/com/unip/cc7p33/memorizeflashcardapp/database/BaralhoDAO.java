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

    @Update
    void update(Baralho baralho);

    @Query("SELECT * FROM baralhos WHERE usuario_id = :userId")
    List<Baralho> getByUserId(String userId);

    @Query("SELECT * FROM baralhos WHERE baralhoId = :id")
    Baralho getById(String id);

    @Query("DELETE FROM baralhos WHERE baralhoId = :id")
    void deleteById(String id);
}