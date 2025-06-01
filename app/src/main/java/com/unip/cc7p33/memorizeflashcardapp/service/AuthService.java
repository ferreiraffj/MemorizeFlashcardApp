package com.unip.cc7p33.memorizeflashcardapp.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    private FirebaseAuth mAuth;

    public interface AuthCallback{
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    } //

    public AuthService(){
        mAuth = FirebaseAuth.getInstance();
    }

    public void registerUser(String email, String password, String nome, AuthCallback callback){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       FirebaseUser user = mAuth.getCurrentUser();
                       if (user != null){
                           callback.onSuccess(user);
                       } else {
                           callback.onFailure("Usuário não encontrado após o registro.");
                       }
                   } else {
                       callback.onFailure(task.getException() != null ?
                               task.getException().getMessage() : "Erro desconhecido no registro");
                   }
                });
    }

    public void loginUser(String email, String password, AuthCallback callback){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       FirebaseUser user = mAuth.getCurrentUser();
                       callback.onSuccess(user);
                   } else {
                       callback.onFailure(task.getException() != null ?
                               task.getException().getMessage() : "Erro desconhecido no login.");
                   }
                });
    }

    public void resetPassword(String email, AuthCallback callback){
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        callback.onSuccess(null); //retorna apenas sucesso na operação
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Erro desconhecido ao resetar a senha");
                    }
                });
    }

    public void logout(){
        mAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    } // recupera usuário atual logado
}
