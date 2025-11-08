package com.unip.cc7p33.memorizeflashcardapp.repository;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

import java.util.Date;
import java.util.concurrent.Executors;

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
                            Usuario novoUsuario = new Usuario();
                            novoUsuario.setUid(firebaseUser.getUid());
                            novoUsuario.setNome(nome);
                            novoUsuario.setEmail(email);
                            novoUsuario.setDiasConsecutivos(0);
                            novoUsuario.setUltimoAcesso(new Date());
                            novoUsuario.setXp(0);
                            novoUsuario.setRanking("Bronze");
                            novoUsuario.setOfensiva(0);
                            novoUsuario.setUltimoEstudo(null);

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
                            callback.onFailure("Usuário não encontrado após o registro bem-sucedido.");
                        }
                    } else {
                        String errorMessage;
                        try {
                            // Lança a exceção para ser capturada pelos blocos catch
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            // Captura erro de senha fraca
                            errorMessage = "A senha deve ter no mínimo 6 caracteres.";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            // Captura erro de e-mail inválido
                            errorMessage = "O formato do e-mail é inválido.";
                        } catch (FirebaseAuthUserCollisionException e) {
                            // Captura erro de e-mail já existente
                            errorMessage = "Este e-mail já está em uso por outra conta.";
                        } catch (Exception e) {
                            // Para qualquer outro erro
                            Log.e("FirebaseAuthDataSource", "Erro não tratado no registro: ", e);
                            errorMessage = "Ocorreu um erro ao registrar. Tente novamente.";
                        }
                        callback.onFailure(errorMessage);
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
                        String errorMessage;
                        try {
                            // Lança a exceção para ser capturada pelos blocos catch
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            // ESTA EXCEÇÃO AGORA COBRE:
                            // 1. E-mail não encontrado
                            // 2. Senha incorreta
                            // 3. E-mail mal formatado
                            // Portanto, usamos uma mensagem genérica.
                            errorMessage = "E-mail ou senha inválidos. Por favor, tente novamente.";
                        } catch (Exception e) {
                            // Para qualquer outro erro (problema de rede, etc.)
                            Log.e("FirebaseAuthDataSource", "Erro não tratado no login: ", e);
                            errorMessage = "Ocorreu um erro ao fazer login. Tente novamente.";
                        }
                        callback.onFailure(errorMessage);
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
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseAuthInvalidCredentialsException){
                            callback.onFailure("O formato do e-mail é inválido.");
                        } else {
                            callback.onSuccess(null, null);
                        }
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
