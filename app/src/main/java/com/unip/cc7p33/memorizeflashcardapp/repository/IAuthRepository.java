package com.unip.cc7p33.memorizeflashcardapp.repository;

import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

public interface IAuthRepository {

    interface GetUserCallback {
        void onUserFound(Usuario usuario);
        void onUserNotFound();
    }

    interface InsertUserCallback {
        void onInsertComplete();
    }

    // Métodos para o AuthService consumir:
    void insertUser(Usuario usuario, InsertUserCallback callback);
    void getUserByEmail(String email, GetUserCallback callback);
    void deleteAllUsers();
}
