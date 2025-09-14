package com.unip.cc7p33.memorizeflashcardapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

import java.util.List;

public class BaralhoAdapter extends RecyclerView.Adapter<BaralhoAdapter.BaralhoViewHolder> {

    private List<Baralho> baralhos;
    private OnItemClickListener listener; // <-- ADICIONADO

    // --- INÍCIO DO CÓDIGO NOVO ---
    // Interface que a Activity vai implementar para receber o evento de clique
    public interface OnItemClickListener {
        void onItemClick(Baralho baralho);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // --- FIM DO CÓDIGO NOVO ---


    public BaralhoAdapter(List<Baralho> baralhos) {
        this.baralhos = baralhos;
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
        holder.cardCount.setText(String.valueOf(baralho.getQuantidadeCartas()));

        // --- INÍCIO DA ALTERAÇÃO NO MÉTODO ---
        // Configura o clique no item da lista
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // Chama o método da interface, passando o objeto do baralho clicado
                listener.onItemClick(baralho);
            }
        });
        // --- FIM DA ALTERAÇÃO NO MÉTODO ---
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