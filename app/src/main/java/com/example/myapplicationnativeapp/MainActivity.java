package com.example.myapplicationnativeapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView message = findViewById(R.id.message);
        Button btnCopy = findViewById(R.id.btn_copy);

        // 1. Собираем базовые характеристики железа
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("--- СВЕДЕНИЯ О ЖЕЛЕЗЕ ---\n")
                .append("Модель: ").append(Build.MODEL).append("\n")
                .append("Производитель: ").append(Build.MANUFACTURER).append("\n")
                .append("Версия Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
                .append("Процессор: ").append(Build.HARDWARE).append("\n")
                .append("Плата: ").append(Build.BOARD).append("\n")
                .append("Архитектура: ").append(Build.SUPPORTED_ABIS[0]).append("\n\n");

        // 2. Получаем доступ к менеджеру датчиков и вытягиваем их список
        infoBuilder.append("--- ДОСТУПНЫЕ ДАТЧИКИ ---\n");
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
            if (sensorList != null && !sensorList.isEmpty()) {
                for (int i = 0; i < sensorList.size(); i++) {
                    Sensor s = sensorList.get(i);
                    infoBuilder.append(i + 1).append(". ").append(s.getName()).append(" (тип: ").append(s.getStringType()).append(")\n");
                }
            } else {
                infoBuilder.append("Датчики не найдены.\n");
            }
        } else {
            infoBuilder.append("Не удалось получить доступ к SensorManager.\n");
        }

        final String finalAllInfo = infoBuilder.toString();

        // Отображаем собранный текст на экране
        message.setText(finalAllInfo);

        // Динамическая логика копирования всей информации (включая датчики)
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Device and Sensor Info", finalAllInfo);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Вся информация скопирована!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
