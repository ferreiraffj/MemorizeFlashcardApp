package com.unip.cc7p33.memorizeflashcardapp.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class UsuarioTest {

    @Test
    public void testaRetornosDosGetters() {
        String nome = "Test User";
        String email = "test@example.com";
        Usuario usuario = new Usuario(nome, email);

        assertEquals(nome, usuario.getNome());
        assertEquals(email, usuario.getEmail());
    }

    @Test
    public void testaUpdateDosSetters() {
        Usuario usuario = new Usuario();
        String uid = "test-uid";
        String nome = "Updated Name";
        String email = "updated@example.com";

        usuario.setUid(uid);
        usuario.setNome(nome);
        usuario.setEmail(email);

        assertEquals(uid, usuario.getUid());
        assertEquals(nome, usuario.getNome());
        assertEquals(email, usuario.getEmail());
    }

    @Test
    public void criaInstanciaUsuario() {
        Usuario usuario = new Usuario();
        assertNotNull(usuario); // Verifica se o objeto não é nulo
    }
}
