// view/MainActivity.java
package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.adapter.BaralhoAdapter;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BaralhoAdapter baralhoAdapter;
    private List<Baralho> listaDeBaralhos;
    private TextView noDecksMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        // REMOVE a linha abaixo, pois o título agora está no XML
        // getSupportActionBar().setTitle("MEMORIZE APP");
        // E removemos o display do título padrão para não sobrepor nosso TextView
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        recyclerView = findViewById(R.id.recycler_view_decks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        noDecksMessage = findViewById(R.id.text_view_no_decks_message);

        listaDeBaralhos = new ArrayList<>();

        baralhoAdapter = new BaralhoAdapter(listaDeBaralhos);
        recyclerView.setAdapter(baralhoAdapter);

        updateNoDecksMessageVisibility();

        FloatingActionButton fab = findViewById(R.id.fab_add_deck);
        fab.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Adicionar novo baralho clicado!", Toast.LENGTH_SHORT).show();
        });

        toolbar.setNavigationOnClickListener(v -> {
            Toast.makeText(this, "Ícone de Menu clicado!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            Toast.makeText(this, "Logout clicado!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNoDecksMessageVisibility() {
        if (listaDeBaralhos.isEmpty()) {
            noDecksMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noDecksMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}