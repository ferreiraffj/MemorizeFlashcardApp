package com.unip.cc7p33.memorizeflashcardapp.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

@Database(entities = {Usuario.class, Flashcard.class, Baralho.class}, version = 6, exportSchema = false)  // Versão incrementada para 4 (adiciona deckId)
@TypeConverters({Converters.class})  // Adicionado para converter Date
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    // Migração destrutiva para recriar tabelas (perde dados, mas funciona para MVP)
    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Recria tabelas com novos campos (destrutivo)
            database.execSQL("DROP TABLE IF EXISTS usuarios");
            database.execSQL("DROP TABLE IF EXISTS flashcards");
            database.execSQL("DROP TABLE IF EXISTS baralhos");
            // Room recriará automaticamente as tabelas com o novo schema
        }
    };
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "memorize_flashcard_db")
                    .addMigrations(MIGRATION_6_7)  // Adiciona a migração
                    .build();
        }
        return instance;
    }
    public abstract UsuarioDAO usuarioDAO();
    public abstract FlashcardDAO flashcardDAO();
    public abstract BaralhoDAO baralhoDAO();
}
