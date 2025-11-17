package com.unip.cc7p33.memorizeflashcardapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.model.Flashcard;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private List<Flashcard> flashcards;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(Flashcard card);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Flashcard card, int position);
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteListener = listener;
    }

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

        // Lógica de exibição da próxima revisão
        if (card.getProximaRevisao() != null) {
            Date proximaRevisao = card.getProximaRevisao();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.textViewNextReview.setText("Próxima revisão: " + sdf.format(proximaRevisao));

            long diff = proximaRevisao.getTime() - new Date().getTime();
            long diasRestantes = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            if (diasRestantes >= 0) {
                holder.textViewDaysLeft.setText(String.format("(em %d dias)", diasRestantes));
            } else {
                holder.textViewDaysLeft.setText("(Vencido)");
            }
        } else {
            holder.textViewNextReview.setText("");
            holder.textViewDaysLeft.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(card);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(card, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView textViewFront;
        TextView textViewNextReview; // Novo
        TextView textViewDaysLeft;   // Novo
        ImageButton deleteButton;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewFront = itemView.findViewById(R.id.text_view_card_front);
            textViewNextReview = itemView.findViewById(R.id.text_view_next_review); // Novo
            textViewDaysLeft = itemView.findViewById(R.id.text_view_days_left);     // Novo
            deleteButton = itemView.findViewById(R.id.button_delete_card);
        }
    }
}
