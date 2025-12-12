package com.unip.cc7p33.memorizeflashcardapp.utils;

import android.os.Build;
import android.view.View;
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
    /**
     * Coloca a atividade em modo imersivo, ocultando a barra de status e a barra de navegação.
     * As barras podem ser reveladas temporariamente com um gesto de deslizar a partir das bordas.
     *
     * @param activity A atividade para colocar em modo imersivo.
     */
    public static void setImmersiveMode(AppCompatActivity activity) {
        if (activity == null || activity.getWindow() == null) {
            return;
        }

        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Para Android 11+ (API 30+)
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // Para versões anteriores ao Android 11, usando a API legada
            View decorView = window.getDecorView();
            //noinspection deprecation
            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Oculta a barra de navegação e a barra de status
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
}
