package com.unip.cc7p33.memorizeflashcardapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Adicione este import
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Flashcard> flashcards;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener; // <-- ADICIONADO para o clique de deletar

    // Interface para o clique de edição (já existente)
    public interface OnItemClickListener {
        void onItemClick(Flashcard card);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // --- INÍCIO DO CÓDIGO NOVO ---
    // Interface para o clique de exclusão
    public interface OnDeleteClickListener {
        void onDeleteClick(Flashcard card, int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }
    // --- FIM DO CÓDIGO NOVO ---

    public FlashcardAdapter(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard card = flashcards.get(position);
        holder.textViewFront.setText(card.getFrente());

        // Clique no item inteiro para editar (já existente)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(card);
            }
        });

        // --- INÍCIO DA ALTERAÇÃO NO MÉTODO ---
        // Clique apenas no botão de lixeira para deletar
        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(card, position);
            }
        });
        // --- FIM DA ALTERAÇÃO NO MÉTODO ---
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFront;
        ImageButton deleteButton; // <-- ADICIONADO

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFront = itemView.findViewById(R.id.text_view_card_front);
            deleteButton = itemView.findViewById(R.id.button_delete_card); // <-- ADICIONADO
        }
    }
}