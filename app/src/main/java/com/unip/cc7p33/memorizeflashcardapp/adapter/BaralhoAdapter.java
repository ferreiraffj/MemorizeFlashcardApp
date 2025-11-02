package com.unip.cc7p33.memorizeflashcardapp.adapter;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.BaralhoDAO;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BaralhoAdapter extends RecyclerView.Adapter<BaralhoAdapter.BaralhoViewHolder> {

    private List<Baralho> baralhos;
    private OnItemClickListener listener;
    private BaralhoDAO baralhoDAO;  // Novo: para calcular contadores

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    // Interface que a Activity vai implementar para receber o evento de clique
    public interface OnItemClickListener {
        void onItemClick(Baralho baralho);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public BaralhoAdapter(List<Baralho> baralhos, BaralhoDAO baralhoDAO) {
        this.baralhos = baralhos;
        this.baralhoDAO = baralhoDAO;
    }

    @NonNull
    @Override
    public BaralhoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baralho, parent, false);
        return new BaralhoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BaralhoViewHolder holder, int position) {
        Baralho baralho = baralhos.get(position);
        holder.deckName.setText(baralho.getNome());

        executorService.execute(() ->{
            // Calculo de contadores por status
            long now = new Date().getTime();
            int erradas = baralhoDAO.getErradasCount(baralho.getBaralhoId());
            int proximas = baralhoDAO.getProximasCount(baralho.getBaralhoId(), now);
            int novas = baralhoDAO.getNovasCount(baralho.getBaralhoId());

            // Atualiza UI na main thread
            holder.itemView.post(() ->{
                String text = erradas + "   " + proximas + "   " + novas;
                SpannableString spannable = new SpannableString(text);
                spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, String.valueOf(erradas).length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                int startProximas = String.valueOf(erradas).length() + 3;  // Ajuste para espaços
                int endProximas = startProximas + String.valueOf(proximas).length();
                spannable.setSpan(new ForegroundColorSpan(Color.YELLOW), startProximas, endProximas, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable.setSpan(new ForegroundColorSpan(Color.GREEN), text.length() - String.valueOf(novas).length(), text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.cardCount.setText(spannable);
            });
        });

        // Clique
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Chama o método da interface, passando o objeto do baralho clicado
                listener.onItemClick(baralho);
            }
        });
    }

    @Override
    public int getItemCount() {
        return baralhos.size();
    }

    static class BaralhoViewHolder extends RecyclerView.ViewHolder {
        TextView deckName;
        TextView cardCount;

        public BaralhoViewHolder(@NonNull View itemView) {
            super(itemView);
            deckName = itemView.findViewById(R.id.text_view_deck_name);
            cardCount = itemView.findViewById(R.id.text_view_card_count);
        }
    }
}