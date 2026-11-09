package com.example.myapplicationnativeapp;

import android.app.ActivityManager;
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
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView message;
    private String memoryInfoStr = "";
    private String hardwareAndSensorInfo = "";
    private String batteryInfo = "";

    // Приемник системных событий для отслеживания батареи в реальном времени
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            
            int temperatureRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            double temperatureCelsius = temperatureRaw / 10.0;
            float batteryPct = (level / (float) scale) * 100;

            String statusText = "Не заряжается";
            if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                statusText = "Заряжается";
            } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                statusText = "Полный заряд";
            }

            batteryInfo = "--- СОСТОЯНИЕ БАТАРЕИ ---\n" +
                    "Заряд: " + (int) batteryPct + "%\n" +
                    "Температура: " + temperatureCelsius + "°C\n" +
                    "Статус: " + statusText + "\n\n";

            // При каждом изменении состояния батареи обновляем данные о памяти
            refreshMemoryInfo();
            updateScreenText();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        Button btnCopy = findViewById(R.id.btn_copy);

        // Безопасное определение архитектуры процессора (защита от вылета в эмуляторах)
        String architecture = (Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0) 
                ? Build.SUPPORTED_ABIS[0] 
                : "unknown";

        // 1. Собираем характеристики железа
        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("--- СВЕДЕНИЯ О ЖЕЛЕЗЕ ---\n")
                .append("Модель: ").append(Build.MODEL).append("\n")
                .append("Производитель: ").append(Build.MANUFACTURER).append("\n")
                .append("Версия Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
                .append("Процессор: ").append(Build.HARDWARE).append("\n")
                .append("Плата: ").append(Build.BOARD).append("\n")
                .append("Архитектура: ").append(architecture).append("\n\n");

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

        // Первичный сбор данных о памяти устройства
        refreshMemoryInfo();

        // 3. Динамическая логика копирования всей отображаемой информации
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String fullTextToCopy = batteryInfo + memoryInfoStr + hardwareAndSensorInfo;
                ClipData clip = ClipData.newPlainText("Device Diagnostic Info", fullTextToCopy);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Вся информация скопирована!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Метод получения актуальных данных об оперативной (RAM) и постоянной памяти (ROM)
    private void refreshMemoryInfo() {
        // ОЗУ (RAM)
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        double totalRam = 0;
        double availRam = 0;
        if (activityManager != null) {
            activityManager.getMemoryInfo(memoryInfo);
            totalRam = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
            availRam = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0);
        }

        // Встроенный диск (ROM)
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();

        double totalRom = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0);
        double availRom = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0);

        memoryInfoStr = "--- ПАМЯТЬ УСТРОЙСТВА ---\n" +
                String.format(Locale.US, "Оперативная память (RAM): %.2f ГБ свободно из %.2f ГБ\n", availRam, totalRam) +
                String.format(Locale.US, "Внутреннее хранилище (ROM): %.2f ГБ свободно из %.2f ГБ\n\n", availRom, totalRom);
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(batteryReceiver);
    }

    private void updateScreenText() {
        if (message != null) {
            String fullDisplayInfo = batteryInfo + memoryInfoStr + hardwareAndSensorInfo;
            message.setText(fullDisplayInfo);
        }
    }
}
