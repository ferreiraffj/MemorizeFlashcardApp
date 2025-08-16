package com.unip.cc7p33.memorizeflashcardapp.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;

@Database(entities = {Usuario.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UsuarioDAO usuarioDao();
}