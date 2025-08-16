package com.unip.cc7p33.memorizeflashcardapp.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

@Dao
public interface UsuarioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(Usuario usuario);

    @Query("SELECT * FROM usuario WHERE uid = :uid LIMIT 1")
    Usuario getUserByUID(String uid);

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    Usuario getUserByEmail(String email);

    @Query("DELETE from usuario")
    void deleteAllUser();
}
