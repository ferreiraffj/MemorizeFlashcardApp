package com.unip.cc7p33.memorizeflashcardapp.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public interface AuthCallback{
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    } //

    public AuthService(){
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, String nome, AuthCallback callback){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()){
                       FirebaseUser user = mAuth.getCurrentUser(); // atribui a variavel user o usuário autenticado
                       if (user != null){
                           Map<String, Object> userData = new HashMap<>(); // cria uma estrutura de dados do tipo chave-valor
                           userData.put("nome", nome); // adiciona o nome do usuário ao Map
                           userData.put("email", email); // adiciona o e-mail do usuário ao Map

                           db.collection("users").document(user.getUid()) // cria ou utiliza a coleção "users" e define que o id do documento será o UID
                                   .set(userData) // envia os dados do Map ao firestore
                                   .addOnSuccessListener(aVoid ->{ // listener de sucesso
                                       callback.onSuccess(user);
                                   })
                                   .addOnFailureListener(e ->{ // listener de exceção
                                        callback.onFailure("Erro ao salvar os dados do usuário: " + e.getMessage());
                                   });
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
