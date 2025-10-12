package com.unip.cc7p33.memorizeflashcardapp.repository;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

public class FirebaseAuthDataSource implements ICloudAuthDataSource {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public FirebaseAuthDataSource() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void registerUser(String email, String password, String nome, AuthResultCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            Usuario novoUsuario = new Usuario(nome, email);
                            novoUsuario.setUid(firebaseUser.getUid());

                            // Salva os dados do usuário no Firestore
                            db.collection("users").document(firebaseUser.getUid())
                                    .set(novoUsuario)
                                    .addOnSuccessListener(aVoid -> {
                                        // Reporta sucesso do registro e dos dados
                                        callback.onSuccess(firebaseUser, novoUsuario);
                                    })
                                    .addOnFailureListener(e -> {
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

    @Override
    public void loginUser(String email, String password, AuthResultCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Busca os dados do usuário no Firestore após o login
                            db.collection("users").document(firebaseUser.getUid())
                                    .get()
                                    .addOnSuccessListener(documentSnapshot -> {
                                        if (documentSnapshot.exists()) {
                                            Usuario usuario = documentSnapshot.toObject(Usuario.class);
                                            if (usuario != null) {
                                                usuario.setUid(firebaseUser.getUid());
                                            }
                                            // Reporta sucesso do login e dos dados
                                            callback.onSuccess(firebaseUser, usuario);
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

    @Override
    public void resetPassword(String email, AuthResultCallback callback) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        // Usamos null, pois não há usuário/dados para retornar
                        callback.onSuccess(null, null);
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Erro desconhecido ao resetar a senha");
                    }
                });
    }

    @Override
    public void logout() {
        mAuth.signOut();
    }

    @Override
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }
}
