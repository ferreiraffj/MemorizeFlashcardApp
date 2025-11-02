package com.unip.cc7p33.memorizeflashcardapp.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class UsuarioDAOTest {

    private UsuarioDAO usuarioDao;
    private AppDatabase db;

    @Before
    public void createDb(){
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        usuarioDao = db.usuarioDAO();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void insertAndGetUserByEmail(){
        Usuario usuario = new Usuario("Test user", "test@example.com");
        usuario.setUid("12345");

        // Insere o usuário
        usuarioDao.insertUser(usuario);

        // Busca o usuário pelo email
        Usuario found = usuarioDao.getUserByEmail("test@example.com");

        // Verifica se o usuário foi encontrado e se os dados estão corretos
        assertNotNull(found);
        assertEquals(found.getEmail(), usuario.getEmail());
    }

    @Test
    public void insertAndGetUserByUID() {
        Usuario usuario = new Usuario("Another User", "another@example.com");
        usuario.setUid("67890");

        // Insere o usuário
        usuarioDao.insertUser(usuario);

        // Busca o usuário pelo UID
        Usuario found = usuarioDao.getUserByUID("67890");

        // Verifica se o usuário foi encontrado e se os dados estão corretos
        assertNotNull(found);
        assertEquals(found.getUid(), usuario.getUid());
    }

    @Test
    public void deleteAllUsers() {
        Usuario usuario1 = new Usuario("User One", "user1@example.com");
        usuario1.setUid("uid1");
        Usuario usuario2 = new Usuario("User Two", "user2@example.com");
        usuario2.setUid("uid2");

        usuarioDao.insertUser(usuario1);
        usuarioDao.insertUser(usuario2);

        // Deleta todos os usuários
        usuarioDao.deleteAllUsers();

        // Tenta buscar um dos usuários, deve retornar nulo
        Usuario found1 = usuarioDao.getUserByEmail("user1@example.com");
        assertNull(found1);
    }
}
