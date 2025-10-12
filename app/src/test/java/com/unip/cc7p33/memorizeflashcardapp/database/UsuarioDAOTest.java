package com.unip.cc7p33.memorizeflashcardapp.database;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE) // Se não precisar do manifesto
public class UsuarioDAOTest {

    private AppDatabase database;
    private UsuarioDAO usuarioDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries() // Para testes, permite consultas na thread principal
                .build();
        usuarioDao = database.usuarioDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void insertUserAndGetUserByUID_shouldReturnCorrectUser() {
        Usuario usuario = new Usuario("Test User", "test@example.com");usuario.setUid("test-uid-123");
        usuarioDao.insertUser(usuario);

        Usuario retrievedUser = usuarioDao.getUserByUID("test-uid-123");
        assertNotNull(retrievedUser);
        assertEquals(usuario.getUid(), retrievedUser.getUid());
        assertEquals(usuario.getNome(), retrievedUser.getNome());
        assertEquals(usuario.getEmail(), retrievedUser.getEmail());
    }

    @Test
    public void insertUserAndGetUserByEmail_shouldReturnCorrectUser() {
        Usuario usuario = new Usuario("Test User Email", "email@example.com");
        usuario.setUid("test-uid-email");
        usuarioDao.insertUser(usuario);

        Usuario retrievedUser = usuarioDao.getUserByEmail("email@example.com");
        assertNotNull(retrievedUser);
        assertEquals(usuario.getUid(), retrievedUser.getUid());
        assertEquals(usuario.getNome(), retrievedUser.getNome());
        assertEquals(usuario.getEmail(), retrievedUser.getEmail());
    }

    @Test
    public void getUserByUID_whenUserNotExists_shouldReturnNull() {
        Usuario retrievedUser = usuarioDao.getUserByUID("non-existent-uid");
        assertNull(retrievedUser);
    }

    @Test
    public void getUserByEmail_whenUserNotExists_shouldReturnNull() {
        Usuario retrievedUser = usuarioDao.getUserByEmail("non-existent@example.com");
        assertNull(retrievedUser);
    }

    @Test
    public void insertUser_withConflictReplace_shouldUpdateUser() {
        Usuario usuario1 = new Usuario("Initial Name", "conflict@example.com");
        usuario1.setUid("conflict-uid");
        usuarioDao.insertUser(usuario1);

        Usuario usuario2 = new Usuario("Updated Name", "conflict@example.com"); // Mesmo email e UID
        usuario2.setUid("conflict-uid");
        usuarioDao.insertUser(usuario2); // Deve substituir

        Usuario retrievedUser = usuarioDao.getUserByUID("conflict-uid");
        assertNotNull(retrievedUser);
        assertEquals("Updated Name", retrievedUser.getNome());
        assertEquals("conflict@example.com", retrievedUser.getEmail());
    }

    @Test
    public void deleteAllUsers_shouldRemoveAllUsers() {
        Usuario usuario1 = new Usuario("User1", "user1@example.com");
        usuario1.setUid("uid1");
        Usuario usuario2 = new Usuario("User2", "user2@example.com");
        usuario2.setUid("uid2");

        usuarioDao.insertUser(usuario1);
        usuarioDao.insertUser(usuario2);

        assertNotNull(usuarioDao.getUserByUID("uid1"));
        assertNotNull(usuarioDao.getUserByUID("uid2"));

        usuarioDao.deleteAllUsers();

        assertNull(usuarioDao.getUserByUID("uid1"));
        assertNull(usuarioDao.getUserByUID("uid2"));
    }
}
