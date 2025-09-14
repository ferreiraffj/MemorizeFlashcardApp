// adapter/BaralhoAdapter.java
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