package com.unip.cc7p33.memorizeflashcardapp.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.BaralhoDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.database.UsuarioDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;
import com.unip.cc7p33.memorizeflashcardapp.model.Usuario;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvOfensiva, tvCartasEstudadas, tvBaralhoTopAcerto, tvBaralhoTopErro, tvBaralhoMaisVisitado;
    private UsuarioDAO usuarioDAO;
    private BaralhoDAO baralhoDAO;
    private FlashcardDAO flashcardDAO;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        usuarioDAO = AppDatabase.getInstance(this).usuarioDAO();
        baralhoDAO = AppDatabase.getInstance(this).baralhoDAO();
        flashcardDAO = AppDatabase.getInstance(this).flashcardDAO();

        tvOfensiva = findViewById(R.id.tv_ofensiva);
        tvCartasEstudadas = findViewById(R.id.tv_cartas_estudadas);
        tvBaralhoTopAcerto = findViewById(R.id.tv_baralho_top_acerto);
        // Adicione os outros TextViews

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            loadMetrics();
        }
    }

    private void loadMetrics() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Contador de ofensiva
            Usuario usuario = usuarioDAO.getUserByUID(userId);
            int dias = (usuario != null) ? usuario.getDiasConsecutivos() : 0;

            // Cartas estudadas hoje (cartas com proximaRevisao atualizada hoje)
            long hoje = getStartOfDay();
            List<Flashcard> cartasHoje = flashcardDAO.getAll().stream()
                    .filter(c -> c.getProximaRevisao() != null && c.getProximaRevisao().getTime() >= hoje)
                    .collect(Collectors.toList());
            int totalEstudadas = cartasHoje.size();

            // Baralho com mais acertos (soma de acertos por baralho)
            List<Baralho> baralhos = baralhoDAO.getByUserId(userId);
            Baralho topAcerto = baralhos.stream()
                    .max(Comparator.comparingInt(b -> getTotalAcertos(b.getBaralhoId())))
                    .orElse(null);

            // Similar para erro e visitas
            Baralho topErro = baralhos.stream()
                    .max(Comparator.comparingInt(b -> getTotalErros(b.getBaralhoId())))
                    .orElse(null);
            Baralho maisVisitado = baralhos.stream()
                    .max(Comparator.comparingInt(Baralho::getVisitas))
                    .orElse(null);

            runOnUiThread(() -> {
                tvOfensiva.setText("Dias consecutivos: " + dias);
                tvCartasEstudadas.setText("Cartas estudadas hoje: " + totalEstudadas);
                tvBaralhoTopAcerto.setText("Baralho com mais acertos: " + (topAcerto != null ? topAcerto.getNome() : "Nenhum"));
                // Atualize os outros...
            });
        });
    }

    private int getTotalAcertos(String deckId) {
        return flashcardDAO.getByDeckId(deckId).stream().mapToInt(Flashcard::getAcertos).sum();
    }

    private int getTotalErros(String deckId) {
        return flashcardDAO.getByDeckId(deckId).stream().mapToInt(Flashcard::getErros).sum();
    }

    private long getStartOfDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        return cal.getTimeInMillis();
    }
}