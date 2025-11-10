package com.unip.cc7p33.memorizeflashcardapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Baralho;

import java.util.List;

public class DeckManagementAdapter extends RecyclerView.Adapter<DeckManagementAdapter.DeckManagementViewHolder> {

    private List<Baralho> deckList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public DeckManagementAdapter(List<Baralho> deckList, OnItemClickListener listener) {
        this.deckList = deckList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeckManagementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deck_management, parent, false);
        return new DeckManagementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckManagementViewHolder holder, int position) {
        Baralho currentDeck = deckList.get(position);
        holder.deckName.setText(currentDeck.getNome());

        holder.editButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(position);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return deckList.size();
    }

    static class DeckManagementViewHolder extends RecyclerView.ViewHolder {
        TextView deckName;
        ImageView editButton;
        ImageView deleteButton;

        public DeckManagementViewHolder(@NonNull View itemView) {
            super(itemView);
            deckName = itemView.findViewById(R.id.tv_deck_name_management);
            editButton = itemView.findViewById(R.id.iv_edit_deck);
            deleteButton = itemView.findViewById(R.id.iv_delete_deck);
        }
    }
}
