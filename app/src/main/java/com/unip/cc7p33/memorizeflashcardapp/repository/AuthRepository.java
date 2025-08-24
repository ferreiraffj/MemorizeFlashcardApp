package com.unip.cc7p33.memorizeflashcardapp.repository;

import android.content.Context;
import android.os.AsyncTask;

import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

public class AuthRepository {

    private UsuarioDAO usuarioDao;

    public AuthRepository(Context context) {
        // Constrói a instância do banco de dados Room
        AppDatabase db = AppDatabase.getDatabase(context);
        usuarioDao = db.usuarioDao();
    }

    // Insere um usuário no banco de dados local
    public void insertUser(Usuario usuario) {
        new InsertUserAsyncTask(usuarioDao).execute(usuario);
    }

    // Busca um usuário por email no banco de dados local
    public Usuario getUserByEmail(String email) {
        return usuarioDao.getUserByEmail(email);
    }

    // Deleta todos os usuários do banco de dados local
    public void deleteAllUsers() {
        new DeleteAllUsersAsyncTask(usuarioDao).execute();
    }

    // AsyncTask para rodar a inserção em um thread separado
    private static class InsertUserAsyncTask extends AsyncTask<Usuario, Void, Void> {
        private UsuarioDAO dao;
        private InsertUserAsyncTask(UsuarioDAO dao) {
            this.dao = dao;
        }
        @Override
        protected Void doInBackground(Usuario... usuarios) {
            dao.insertUser(usuarios[0]);
            return null;
        }
    }

    // AsyncTask para rodar a exclusão em um thread separado
    private static class DeleteAllUsersAsyncTask extends AsyncTask<Void, Void, Void> {
        private UsuarioDAO dao;
        private DeleteAllUsersAsyncTask(UsuarioDAO dao) {
            this.dao = dao;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            dao.deleteAllUsers();
            return null;
        }
    }
}
