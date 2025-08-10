package com.unip.cc7p33.memorizeflashcardapp.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public interface AuthCallback{
        void onSuccess(Usuario usuario);
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
                       FirebaseUser firebaseUser = mAuth.getCurrentUser(); // atribui a variavel user o usuário autenticado
                       if (firebaseUser != null){
                           Usuario novoUsuario = new Usuario(nome, email); // Cria um novo objeto Usuario

                           db.collection("users").document(firebaseUser.getUid()) // cria ou utiliza a coleção "users" e define que o id do documento será o UID
                                   .set(novoUsuario) // Salva o objeto Usuario diretamente no Firestore
                                   .addOnSuccessListener(aVoid ->{ // listener de sucesso
                                       callback.onSuccess(novoUsuario);
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
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Recupera os dados do usuário do Firestore
                            db.collection("users").document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Usuario usuario = documentSnapshot.toObject(Usuario.class); // Converte o documento para um objeto Usuario
                                            callback.onSuccess(usuario);
                                        } else {
                                            callback.onFailure("Dados do usuário não encontrados.");
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        callback.onFailure("Erro ao buscar dados do usuário: " + e.getMessage());
                                    });
                        } else {
                            callback.onFailure("Usuário não encontrado após o login.");
                        }
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
