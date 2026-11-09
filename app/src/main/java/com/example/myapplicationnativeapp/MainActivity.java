package com.example.myapplicationnativeapp;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView message = findViewById(R.id.message);

        // Формируем детальную информацию о железе устройства
        String hardwareInfo = "Модель: " + Build.MODEL + "\n" +
                              "Процессор: " + Build.HARDWARE + "\n" +
                              "Плата: " + Build.BOARD + "\n" +
                              "Архитектура: " + Build.SUPPORTED_ABIS[0];

        // Выводим текст на экран
        message.setText(hardwareInfo);
    }
}
