package com.unip.cc7p33.memorizeflashcardapp.utils;

import android.os.Build;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class SystemUIUtils {

    /**
     * Oculta a barra de status da tela, usando a abordagem correta
     * para a versão do Android do dispositivo.
     *
     * @param activity A atividade na qual a barra de status será ocultada.
     */
    public static void hideStatusBar(AppCompatActivity activity) {
        if (activity == null || activity.getWindow() == null) {
            return; // Evita NullPointerException
        }

        Window window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Para Android 11+ (API 30+), usa WindowInsetsController
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
            }
        } else {
            // Para versões anteriores ao Android 11
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }
    }
}
