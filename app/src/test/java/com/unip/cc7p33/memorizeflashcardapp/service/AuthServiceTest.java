package com.unip.cc7p33.memorizeflashcardapp.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import com.unip.cc7p33.memorizeflashcardapp.repository.AuthRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AuthServiceTest {

    @Mock
    private Context mockContext;
    @Mock
    private FirebaseAuth mockFirebaseAuth;
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private AuthRepository mockAuthRepository;
    @Mock
    private ConnectivityManager mockConnectivityManager;
    @Mock
    private NetworkInfo mockNetworkInfo;
    @Mock
    private Task<AuthResult> mockAuthTask;
    @Mock
    private Task<DocumentSnapshot> mockFirestoreTask;
    @Mock
    private Task<Void> mockSetTask;
    @Mock
    private FirebaseUser mockFirebaseUser;
    @Mock
    private CollectionReference mockCollectionReference;
    @Mock
    private DocumentReference mockDocumentReference;
    @Mock
    private DocumentSnapshot mockDocumentSnapshot;
    @Mock
    private AuthService.AuthCallback mockCallback;

    private AuthService authService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula o contexto para que o ConnectivityManager funcione
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);

        authService = new AuthService(mockContext) {
            // Sobrescreve as instâncias para usar os mocks
            @Override
            public void registerUser(String email, String password, String nome, AuthCallback callback) {
                super.registerUser(email, password, nome, callback);
            }

            @Override
            public void loginUser(String email, String password, AuthCallback callback) {
                super.loginUser(email, password, callback);
            }
        };
    }

    // --- Testes para registerUser ---

    @Test
    public void registerUser_Success_CallbackSuccessCalled() {
        // Configura mocks para um cenário de sucesso
        when(mockFirebaseAuth.createUserWithEmailAndPassword(anyString(), anyString())).thenReturn(mockAuthTask);
        when(mockAuthTask.isSuccessful()).thenReturn(true);
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn("test-uid");
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.set(any(Usuario.class))).thenReturn(mockSetTask);
        when(mockSetTask.isSuccessful()).thenReturn(true);

        // Captura o listener de sucesso para simular a chamada
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        doAnswer(invocation -> {
            successCaptor.getValue().onSuccess(null);
            return mockSetTask;
        }).when(mockSetTask).addOnSuccessListener(successCaptor.capture());

        // Executa o método a ser testado
        authService.registerUser("test@example.com", "password123", "Test User", mockCallback);

        // Verifica as interações
        verify(mockAuthRepository).insertUser(any(Usuario.class));
        verify(mockCallback).onSuccess(any(Usuario.class));
        verify(mockCallback, never()).onFailure(anyString());
    }

    // --- Testes para loginUser ---

    @Test
    public void loginUser_Online_Success_CallbackSuccessCalled() {
        // Simula a conexão com a internet
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(true);

        // Configura mocks para login bem-sucedido
        when(mockFirebaseAuth.signInWithEmailAndPassword(anyString(), anyString())).thenReturn(mockAuthTask);
        when(mockAuthTask.isSuccessful()).thenReturn(true);
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn("test-uid");
        when(mockFirestore.collection(anyString())).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference);
        when(mockDocumentReference.get()).thenReturn(mockFirestoreTask);

        when(mockFirestoreTask.isSuccessful()).thenReturn(true);

        // Simula a resposta do Firestore
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        Usuario expectedUser = new Usuario("Test Login", "login@example.com");
        expectedUser.setUid("test-uid");
        when(mockDocumentSnapshot.toObject(Usuario.class)).thenReturn(expectedUser);

        // Captura o listener de sucesso do Firestore
        ArgumentCaptor<OnSuccessListener> successCaptor = ArgumentCaptor.forClass(OnSuccessListener.class);
        doAnswer(invocation -> {
            successCaptor.getValue().onSuccess(mockDocumentSnapshot);
            return mockFirestoreTask;
        }).when(mockFirestoreTask).addOnSuccessListener(successCaptor.capture());

        authService.loginUser("login@example.com", "password123", mockCallback);

        // Verifica se o repositório foi chamado e o callback de sucesso foi acionado
        verify(mockAuthRepository).insertUser(any(Usuario.class));
        verify(mockCallback).onSuccess(any(Usuario.class));
        verify(mockCallback, never()).onFailure(anyString());
    }

    @Test
    public void loginUser_Offline_LocalUserFound_CallbackSuccessCalled() {
        // Simula a falta de conexão
        when(mockNetworkInfo.isConnectedOrConnecting()).thenReturn(false);

        // Simula que o usuário foi encontrado no banco de dados local
        Usuario localUser = new Usuario("Local User", "local@example.com");
        when(mockAuthRepository.getUserByEmail(anyString())).thenReturn(localUser);

        authService.loginUser("local@example.com", "password123", mockCallback);

        // Verifica se a chamada ao Firebase foi ignorada e o callback local foi acionado
        verify(mockFirebaseAuth, never()).signInWithEmailAndPassword(anyString(), anyString());
        verify(mockAuthRepository).getUserByEmail("local@example.com");
        verify(mockCallback).onSuccess(localUser);
    }
}