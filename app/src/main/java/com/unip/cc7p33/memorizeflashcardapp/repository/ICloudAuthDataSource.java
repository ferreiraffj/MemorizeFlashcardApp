package com.unip.cc7p33.memorizeflashcardapp.repository;

import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

public interface ICloudAuthDataSource {
    interface AuthResultCallback {
        // Agora retorna tanto o FirebaseUser quanto os dados de Usuario (Firestore)
        void onSuccess(FirebaseUser user, Usuario userData);
        void onFailure(String errorMessage);
    }

    // Contratos de métodos de autenticação:
    void registerUser(String email, String password, String nome, AuthResultCallback callback);
    void loginUser(String email, String password, AuthResultCallback callback);
    void resetPassword(String email, AuthResultCallback callback);

    // Métodos utilitários
    FirebaseUser getCurrentUser();
    void logout();
}
