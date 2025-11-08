package com.unip.cc7p33.memorizeflashcardapp.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository implements IAuthRepository {

    private UsuarioDAO usuarioDao;
    private final ExecutorService executor;

    public AuthRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);  // Correção: de getDatabase para getInstance
        usuarioDao = db.usuarioDAO();  // Correção: de usuarioDao para usuarioDAO
        executor = Executors.newSingleThreadExecutor();
    }

    // Insere um usuário no banco de dados local com callback
    public void insertUser(Usuario usuario, InsertUserCallback callback) {
        Log.d("AuthRepository", "Inserindo usuário no banco local: " + usuario.getEmail());
        executor.execute(() -> {
            try {
                usuarioDao.insertUser(usuario);
                Log.d("AuthRepository", "Usuário inserido com sucesso: " + usuario.getEmail());
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onInsertComplete();
                });
            } catch (Exception e){
                Log.e("AuthRepository", "Erro ao inserir usuário: " + e.getMessage());
            }
        });
    }

    // Busca um usuário por e-mail de forma assíncrona
    public void getUserByEmail(String email, GetUserCallback callback) {
        executor.execute(() -> {
            Usuario usuario = usuarioDao.getUserByEmail(email);
            new Handler(Looper.getMainLooper()).post(()->{
                if (usuario != null) {
                    callback.onUserFound(usuario);
                } else {
                    callback.onUserNotFound();
                }
            });
        });
    }

    // Deleta todos os usuários do banco de dados local
    public void deleteAllUsers() {
        executor.execute(() -> usuarioDao.deleteAllUsers());
    }
}
