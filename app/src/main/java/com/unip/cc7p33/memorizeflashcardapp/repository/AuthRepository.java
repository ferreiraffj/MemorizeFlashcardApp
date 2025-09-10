package com.unip.cc7p33.memorizeflashcardapp.repository;

import android.content.Context;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {

    private UsuarioDAO usuarioDao;
    private final ExecutorService executor;

    public interface GetUserCallback {
        void onUserFound(Usuario usuario);
        void onUserNotFound();
    }

    public AuthRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        usuarioDao = db.usuarioDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // Insere um usuário no banco de dados local
    public void insertUser(Usuario usuario) {
        executor.execute(() -> usuarioDao.insertUser(usuario));
    }

    // Busca um usuário por e-mail de forma assíncrona
    public void getUserByEmail(String email, GetUserCallback callback) {
        executor.execute(() -> {
            Usuario usuario = usuarioDao.getUserByEmail(email);
            if (usuario != null) {
                callback.onUserFound(usuario);
            } else {
                callback.onUserNotFound();
            }
        });
    }

    // Deleta todos os usuários do banco de dados local
    public void deleteAllUsers() {
        executor.execute(() -> usuarioDao.deleteAllUsers());
    }
}