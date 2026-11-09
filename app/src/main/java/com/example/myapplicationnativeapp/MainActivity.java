package com.example.myapplicationnativeapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView message = findViewById(R.id.message);
        Button btnCopy = findViewById(R.id.btn_copy);

        // Объединяем оба ваших варианта в одну подробную сводку
        final String hardwareInfo = "--- Сведения об устройстве ---\n" +
                              "Модель: " + Build.MODEL + "\n" +
                              "Производитель: " + Build.MANUFACTURER + "\n" +
                              "Версия Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n" +
                              "Процессор: " + Build.HARDWARE + "\n" +
                              "Плата: " + Build.BOARD + "\n" +
                              "Архитектура: " + Build.SUPPORTED_ABIS[0];

        // Отображаем текст на экране
        message.setText(hardwareInfo);

        // Назначаем динамическую логику на нажатие кнопки
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обращаемся к системному сервису буфера обмена
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Hardware Info", hardwareInfo);
                
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    // Показываем пользователю всплывающее уведомление
                    Toast.makeText(MainActivity.this, "Характеристики скопированы!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
