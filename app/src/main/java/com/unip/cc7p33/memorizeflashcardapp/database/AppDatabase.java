package com.unip.cc7p33.memorizeflashcardapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

@Database(entities = {Usuario.class, Flashcard.class, Baralho.class}, version = 4, exportSchema = false)  // Versão incrementada para 4 (adiciona deckId)
@TypeConverters({Converters.class})  // Adicionado para converter Date
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "memorize_flashcard_db")
                    .fallbackToDestructiveMigration()  // Para MVP: recria banco se versão mudar
                    .build();
        }
        return instance;
    }

    public abstract UsuarioDAO usuarioDAO();
    public abstract FlashcardDAO flashcardDAO();
    public abstract BaralhoDAO baralhoDAO();
}
