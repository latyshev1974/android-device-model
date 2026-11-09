package com.example.myapplicationnativeapp;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView message;
    private String hardwareAndSensorInfo = "";
    private String batteryInfo = "";

    // Приемник системных событий для отслеживания батареи в реальном времени
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Получаем уровень заряда
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            
            // Получаем температуру (передается в десятых долях градуса Цельсия)
            int temperatureRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            double temperatureCelsius = temperatureRaw / 10.0;

            float batteryPct = (level / (float) scale) * 100;

            // Определяем статус зарядки
            String statusText = "Не заряжается";
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                statusText = "Заряжается";
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                statusText = "Полный заряд";
            }

            // Формируем блок информации о батарее
            batteryInfo = "--- СОСТОЯНИЕ БАТАРЕИ ---\n" +
                    "Заряд: " + (int) batteryPct + "%\n" +
                    "Температура: " + temperatureCelsius + "°C\n" +
                    "Статус: " + statusText + "\n\n";

            // Обновляем текст на экране, соединяя батарею, железо и датчики
            updateScreenText();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        Button btnCopy = findViewById(R.id.btn_copy);

        // 1. Собираем статические характеристики железа
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("--- СВЕДЕНИЯ О ЖЕЛЕЗЕ ---\n")
                .append("Модель: ").append(Build.MODEL).append("\n")
                .append("Производитель: ").append(Build.MANUFACTURER).append("\n")
                .append("Версия Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
                .append("Процессор: ").append(Build.HARDWARE).append("\n")
                .append("Плата: ").append(Build.BOARD).append("\n")
                .append("Архитектура: ").append(Build.SUPPORTED_ABIS[0]).append("\n\n");

        // 2. Получаем список всех датчиков
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
        }
        
        hardwareAndSensorInfo = infoBuilder.toString();

        // 3. Динамическая логика копирования всей отображаемой информации
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String fullTextToCopy = batteryInfo + hardwareAndSensorInfo;
                ClipData clip = ClipData.newPlainText("Device Diagnostic Info", fullTextToCopy);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Вся информация скопирована!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Регистрируем приемник при запуске активности
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    // Незабываем отписать приемник при закрытии приложения, чтобы не тратить ресурсы
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryReceiver);
    }

    // Метод для склейки блоков информации и обновления UI
    private void updateScreenText() {
        if (message != null) {
            String fullDisplayInfo = batteryInfo + hardwareAndSensorInfo;
            message.setText(fullDisplayInfo);
        }
    }
}
