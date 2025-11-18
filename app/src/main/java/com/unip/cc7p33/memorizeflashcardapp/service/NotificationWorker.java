package com.unip.cc7p33.memorizeflashcardapp.service;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.unip.cc7p33.memorizeflashcardapp.R;
import com.unip.cc7p33.memorizeflashcardapp.database.AppDatabase;
import com.unip.cc7p33.memorizeflashcardapp.database.FlashcardDAO;
import com.unip.cc7p33.memorizeflashcardapp.view.DashboardActivity;
import java.util.Date;

public class NotificationWorker extends Worker {

    public static final String USER_ID_KEY = "user_id";
    private static final String CHANNEL_ID = "study_reminder_channel";
    private static final int NOTIFICATION_ID = 1;

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obter o userId dos dados de entrada
        String userId = getInputData().getString(USER_ID_KEY);
        if (userId == null || userId.isEmpty()) {
            // Não pode prosseguir sem um userId, falha silenciosa
            return Result.failure();
        }

        Context context = getApplicationContext();
        FlashcardDAO flashcardDAO = AppDatabase.getInstance(context).flashcardDAO();
        long today = new Date().getTime();

        int dueCardsCount = flashcardDAO.getDueCardsCount(userId, today);

        if (dueCardsCount > 0) {
            sendNotification(context, dueCardsCount);
        }

        return Result.success();
    }

    private void sendNotification(Context context, int cardCount) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Lembretes de Estudo";
            String description = "Canal para notificações de lembrete de estudo de flashcards";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("Hora de Revisar!")
                .setContentText("Você tem " + cardCount + " cartas para estudar hoje.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}