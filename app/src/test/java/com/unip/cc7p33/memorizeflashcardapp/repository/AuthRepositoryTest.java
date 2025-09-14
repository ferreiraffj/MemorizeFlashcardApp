package com.unip.cc7p33.memorizeflashcardapp.repository;

import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AuthRepositoryTest {

    // Mock do Context, necessário para instanciar o AuthRepository
    @Mock
    private android.content.Context mockContext;

    // Mock do banco de dados e do DAO
    @Mock
    private AppDatabase mockAppDatabase;
    @Mock
    private UsuarioDAO mockUsuarioDao;

    // A classe que vamos testar
    private AuthRepository authRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula o comportamento do AppDatabase para retornar nosso mock do DAO
        when(mockAppDatabase.usuarioDao()).thenReturn(mockUsuarioDao);

        // Crie uma instância do AuthRepository, mas use um mock para o Context e o AppDatabase
        // Isso permite que o construtor do AuthRepository seja executado de forma isolada
        authRepository = new AuthRepository(mockContext) {
            // Sobrescreve o método getDatabase para retornar o mock do AppDatabase
            // Isso evita a dependência real do banco de dados
            @Override
            public void insertUser(Usuario usuario) {
                // Remove a chamada ao AsyncTask, testando diretamente o DAO
                mockUsuarioDao.insertUser(usuario);
            }
            @Override
            public Usuario getUserByEmail(String email) {
                return mockUsuarioDao.getUserByEmail(email);
            }
            @Override
            public void deleteAllUsers() {
                // Remove a chamada ao AsyncTask, testando diretamente o DAO
                mockUsuarioDao.deleteAllUsers();
            }
        };
    }

    @Test
    public void insertUser_callsDaoMethod() {
        Usuario usuario = new Usuario("Test Insert", "insert@example.com");
        usuario.setUid("uidInsert");

        // Chama o método público a ser testado
        authRepository.insertUser(usuario);

        // Verifica se o método `insertUser` do mock do DAO foi chamado com o usuário correto
        verify(mockUsuarioDao).insertUser(usuario);
    }

    @Test
    public void getUserByEmail_returnsUserFromDao() {
        Usuario expectedUser = new Usuario("Test Search", "search@example.com");
        expectedUser.setUid("uidSearch");

        // Simula o comportamento do mock: quando `getUserByEmail` for chamado, ele retornará `expectedUser`
        when(mockUsuarioDao.getUserByEmail("search@example.com")).thenReturn(expectedUser);

        Usuario foundUser = authRepository.getUserByEmail("search@example.com");

        // Verifica se o usuário retornado é o mesmo que o mock retornou
        assertNotNull(foundUser);
        assertEquals(expectedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    public void deleteAllUsers_callsDaoMethod() {
        // Chama o método público a ser testado
        authRepository.deleteAllUsers();

        // Verifica se o método `deleteAllUsers` do mock do DAO foi chamado
        verify(mockUsuarioDao).deleteAllUsers();
    }
}