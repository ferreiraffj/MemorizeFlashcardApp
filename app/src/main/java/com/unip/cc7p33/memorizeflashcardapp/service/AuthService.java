package com.unip.cc7p33.memorizeflashcardapp.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.repository.IAuthRepository;
import com.unip.cc7p33.memorizeflashcardapp.repository.ICloudAuthDataSource;

import java.util.List;

public class AuthService {

    private final ICloudAuthDataSource cloudAuthDataSource;
    private final IAuthRepository authRepository;
    private final ConnectivityManager connectivityManager;
    private final FlashcardService flashcardService;
    private final BaralhoService baralhoService;

    public interface AuthCallback {
        void onSuccess(Usuario usuario);
        void onFailure(String errorMessage);
    }

    // NOVO CONSTRUTOR: Recebe todas as dependências como interfaces
    // O Context é mantido APENAS para o serviço do ConnectivityManager
    public AuthService(ICloudAuthDataSource cloudAuthDataSource, IAuthRepository authRepository, ConnectivityManager connectivityManager, FlashcardService flashcardService, BaralhoService baralhoService) {
        this.cloudAuthDataSource = cloudAuthDataSource;
        this.authRepository = authRepository;
        this.connectivityManager = connectivityManager;
        this.flashcardService = flashcardService;
        this.baralhoService = baralhoService;
    }

    // Construtor auxiliar da Activity (para manter a compatibilidade no código de produção)
    public AuthService(Context context) {
        this.cloudAuthDataSource = new com.unip.cc7p33.memorizeflashcardapp.repository.FirebaseAuthDataSource();
        this.authRepository = new com.unip.cc7p33.memorizeflashcardapp.repository.AuthRepository(context);
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.flashcardService = new FlashcardService();
        this.flashcardService.setFlashcardDAO(AppDatabase.getInstance(context).flashcardDAO());
        this.baralhoService = new BaralhoService();
        this.baralhoService.setBaralhoDAO(AppDatabase.getInstance(context).baralhoDAO());
    }

    public void registerUser(String email, String password, String nome, AuthCallback callback) {
        // Chama o data source Cloud
        cloudAuthDataSource.registerUser(email, password, nome, new ICloudAuthDataSource.AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user, Usuario novoUsuario) {
                // Se o Cloud teve sucesso, salva localmente
                authRepository.insertUser(novoUsuario, () -> {
                    callback.onSuccess(novoUsuario);
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        if (isConnectedToInternet()) {
            cloudAuthDataSource.loginUser(email, password, new ICloudAuthDataSource.AuthResultCallback() {
                @Override
                public void onSuccess(FirebaseUser user, Usuario usuario) {
                    authRepository.insertUser(usuario, () -> {
                        // ##### 3. LÓGICA DE SINCRONIZAÇÃO CORRIGIDA #####
                        Log.d("AuthService", "Iniciando sincronização de BARALHOS após login...");
                        baralhoService.baixarBaralhosDaNuvem(user.getUid(), new BaralhoService.OnCompleteListener<List<Baralho>>() {
                            @Override
                            public void onSuccess(List<Baralho> result) {
                                Log.d("AuthService", "Sincronização de BARALHOS concluída. Iniciando de FLASHCARDS...");
                                flashcardService.syncExistingDataToRoom(user.getUid(), () -> {
                                    Log.d("AuthService", "Sincronização de FLASHCARDS concluída.");
                                    // Notifica a UI que TUDO terminou
                                    callback.onSuccess(usuario);
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("AuthService", "Falha na sincronização de baralhos. A sincronização de flashcards ainda será tentada.", e);
                                // Mesmo com falha, tentamos sincronizar o resto e notificamos o usuário.
                                flashcardService.syncExistingDataToRoom(user.getUid(), () -> {
                                    callback.onSuccess(usuario);
                                });
                            }
                        });
                        // ##### FIM DA LÓGICA DE SINCRONIZAÇÃO CORRIGIDA #####
                    });
                }

                @Override
                public void onFailure(String errorMessage) {
                    callback.onFailure(errorMessage);
                }
            });
        } else {
            // Lógica offline, utilizando a nova interface (IAuthRepository)
            authRepository.getUserByEmail(email, new IAuthRepository.GetUserCallback() {
                @Override
                public void onUserFound(Usuario usuario) {
                    callback.onSuccess(usuario);
                }

                @Override
                public void onUserNotFound() {
                    callback.onFailure("Sem conexão com a internet. E-mail não encontrado no banco de dados local.");
                }
            });
        }
    }

    public void resetPassword(String email, AuthCallback callback) {
        cloudAuthDataSource.resetPassword(email, new ICloudAuthDataSource.AuthResultCallback() {
            @Override
            public void onSuccess(FirebaseUser user, Usuario userData) {
                callback.onSuccess(null); // Retorna apenas sucesso na operação
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    public void logout() {
        cloudAuthDataSource.logout();
    }

    public void clearLocalData() {
        authRepository.deleteAllUsers();
    }

    public FirebaseUser getCurrentUser() {
        return cloudAuthDataSource.getCurrentUser();
    }

    private boolean isConnectedToInternet() {
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
