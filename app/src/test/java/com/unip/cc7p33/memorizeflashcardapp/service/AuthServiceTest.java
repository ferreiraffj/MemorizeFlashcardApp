package com.unip.cc7p33.memorizeflashcardapp.service;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.repository.IAuthRepository;
import com.unip.cc7p33.memorizeflashcardapp.repository.ICloudAuthDataSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    // Dependências Mockadas (Simuladas)
    @Mock ICloudAuthDataSource mockCloudDataSource;
    @Mock IAuthRepository mockRepository;
    @Mock ConnectivityManager mockConnectivityManager;
    @Mock AuthService.AuthCallback mockCallback;
    @Mock NetworkInfo mockNetworkInfo;
    @Mock FirebaseUser mockFirebaseUser;

    private AuthService authService;

    // Captura os argumentos passados para os callbacks
    @Captor ArgumentCaptor<ICloudAuthDataSource.AuthResultCallback> cloudCallbackCaptor;
    @Captor ArgumentCaptor<IAuthRepository.InsertUserCallback> repositoryCallbackCaptor;


    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // Cria a instância de AuthService com os mocks injetados
        authService = new AuthService(mockCloudDataSource, mockRepository, mockConnectivityManager);
    }

    // --- CENÁRIOS ONLINE ---

    @Test
    public void testLogin_Online_Success() {
        Usuario testUser = new Usuario("Teste", "test@online.com");

        // 1. Configura Mocks de Ambiente: Simula que há conexão
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(true);

        // Ação: Inicia o login
        authService.loginUser(testUser.getEmail(), "senha123", mockCallback);

        // 2. Captura o Callback do Cloud (Firebase) e Simula Sucesso
        // Verifica se o login foi chamado e captura o callback
        verify(mockCloudDataSource).loginUser(eq(testUser.getEmail()), eq("senha123"), cloudCallbackCaptor.capture());

        // Simula o SUCESSO do Firebase (Passo GREEN do Cloud):
        ICloudAuthDataSource.AuthResultCallback cloudCallback = cloudCallbackCaptor.getValue();
        cloudCallback.onSuccess(mockFirebaseUser, testUser);

        // 3. Captura o Callback do Repositório (SQLite) e Simula Sucesso
        // Verifica se a inserção local foi chamada após o sucesso do Cloud
        verify(mockRepository).insertUser(eq(testUser), repositoryCallbackCaptor.capture());

        // Simula o SUCESSO do SQLite (Passo GREEN do Repositório):
        IAuthRepository.InsertUserCallback repoCallback = repositoryCallbackCaptor.getValue();
        repoCallback.onInsertComplete();

        // 4. Verifica o Resultado Final
        // O callback final deve ser chamado com sucesso, confirmando que o AuthService coordenou tudo
        verify(mockCallback).onSuccess(testUser);
        verify(mockCallback, never()).onFailure(anyString()); // Garante que a falha NÃO foi chamada
    }

    @Test
    public void testLogin_Online_CloudFailure() {
        // ... (Configuração de conexão, similar ao anterior) ...
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(true);

        authService.loginUser("falha@nuvem.com", "senha123", mockCallback);

        verify(mockCloudDataSource).loginUser(anyString(), anyString(), cloudCallbackCaptor.capture());

        // Simula a FALHA do Cloud (Firebase)
        cloudCallbackCaptor.getValue().onFailure("Credenciais inválidas.");

        // Verifica o Resultado Final: a falha do Cloud deve ser repassada
        verify(mockCallback).onFailure(eq("Credenciais inválidas."));
        verify(mockCallback, never()).onSuccess(any());

        // Garante que o Repositório local nunca foi chamado
        verify(mockRepository, never()).insertUser(any(), any());
    }

    // --- CENÁRIOS OFFLINE ---

    @Test
    public void testLogin_Offline_UserFoundLocally() {
        Usuario localUser = new Usuario("Local", "local@teste.com");

        // 1. Configura Mocks de Ambiente: Simula que NÃO há conexão
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(null);

        authService.loginUser(localUser.getEmail(), "qualquer_senha", mockCallback);

        // 2. Captura o Callback do Repositório (SQLite) e Simula Sucesso
        verify(mockRepository).getUserByEmail(eq(localUser.getEmail()), any(IAuthRepository.GetUserCallback.class));

        // Simula o SUCESSO do Repositório (Passo GREEN do SQLite):
        // Neste ponto, você precisa simular que o repositório encontrou o usuário
        ArgumentCaptor<IAuthRepository.GetUserCallback> repoGetCallbackCaptor = ArgumentCaptor.forClass(IAuthRepository.GetUserCallback.class);
        verify(mockRepository).getUserByEmail(anyString(), repoGetCallbackCaptor.capture());
        repoGetCallbackCaptor.getValue().onUserFound(localUser);

        // 3. Verifica o Resultado Final
        // O callback final deve ser sucesso
        verify(mockCallback).onSuccess(localUser);
        verify(mockCallback, never()).onFailure(anyString());

        // Garante que o Cloud NUNCA foi chamado
        verify(mockCloudDataSource, never()).loginUser(anyString(), anyString(), any());
    }
}