package com.example.myapplicationnativeapp;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private TextView message;
    private String memoryInfoStr = "";
    private String hardwareAndSensorInfo = "";
    private String batteryInfo = "--- СОСТОЯНИЕ БАТАРЕИ ---\nЗагрузка данных...\n\n";
    private boolean isReceiverRegistered = false;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            try {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                int temperatureRaw = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                
                double temperatureCelsius = temperatureRaw / 10.0;
                float batteryPct = (scale > 0) ? (level / (float) scale) * 100 : 0;

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

                updateScreenText();
            } catch (Exception ignored) {}
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        message = findViewById(R.id.message);
        Button btnCopy = findViewById(R.id.btn_copy);

        String architecture = "unknown";
        if (Build.SUPPORTED_ABIS != null && Build.SUPPORTED_ABIS.length > 0) {
            architecture = Build.SUPPORTED_ABIS[0];
        }

        StringBuilder infoBuilder = new StringBuilder();
        infoBuilder.append("--- СВЕДЕНИЯ О ЖЕЛЕЗЕ ---\n")
                .append("Модель (Build.MODEL): ").append(Build.MODEL != null ? Build.MODEL : "unknown").append("\n")
                .append("Производитель: ").append(Build.MANUFACTURER != null ? Build.MANUFACTURER : "unknown").append("\n")
                .append("Версия Android: ").append(Build.VERSION.RELEASE != null ? Build.VERSION.RELEASE : "unknown")
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
                .append("Процессор: ").append(Build.HARDWARE != null ? Build.HARDWARE : "unknown").append("\n")
                .append("Плата: ").append(Build.BOARD != null ? Build.BOARD : "unknown").append("\n")
                .append("Архитектура: ").append(architecture).append("\n\n");

        infoBuilder.append("--- ДОСТУПНЫЕ ДАТЧИКИ ---\n");
        try {
            SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
                if (sensorList != null && !sensorList.isEmpty()) {
                    int limit = Math.min(sensorList.size(), 25);
                    for (int i = 0; i < limit; i++) {
                        Sensor s = sensorList.get(i);
                        if (s != null && s.getName() != null) {
                            infoBuilder.append(i + 1).append(". ").append(s.getName()).append("\n");
                        }
                    }
                } else {
                    infoBuilder.append("Датчики не найдены.\n");
                }
            }
        } catch (Exception e) {
            infoBuilder.append("Не удалось загрузить датчики.\n");
        }
        
        hardwareAndSensorInfo = infoBuilder.toString();

        refreshMemoryInfo();
        updateScreenText();

        if (btnCopy != null) {
            btnCopy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                String fullTextToCopy = batteryInfo + memoryInfoStr + hardwareAndSensorInfo;
                ClipData clip = ClipData.newPlainText("Device Diagnostic Info", fullTextToCopy);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, "Информация скопирована в буфер!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void refreshMemoryInfo() {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            double totalRam = 0, availRam = 0;
            if (activityManager != null) {
                activityManager.getMemoryInfo(memoryInfo);
                totalRam = memoryInfo.totalMem / (1024.0 * 1024.0 * 1024.0);
                availRam = memoryInfo.availMem / (1024.0 * 1024.0 * 1024.0);
            }

            File path = Environment.getDataDirectory();
            double totalRom = 0, availRom = 0;
            if (path != null) {
                StatFs stat = new StatFs(path.getPath());
                long blockSize = stat.getBlockSizeLong();
                totalRom = (stat.getBlockCountLong() * blockSize) / (1024.0 * 1024.0 * 1024.0);
                availRom = (stat.getAvailableBlocksLong() * blockSize) / (1024.0 * 1024.0 * 1024.0);
            }

            memoryInfoStr = "--- ПАМЯТЬ УСТРОЙСТВА ---\n" +
                    String.format(Locale.US, "Оперативная память (RAM): %.2f ГБ свободно из %.2f ГБ\n", availRam, totalRam) +
                    String.format(Locale.US, "Внутреннее хранилище (ROM): %.2f ГБ свободно из %.2f ГБ\n\n", availRom, totalRom);
        } catch (Exception e) {
            memoryInfoStr = "--- ПАМЯТЬ УСТРОЙСТВА ---\nДанные памяти недоступны.\n\n";
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (!isReceiverRegistered) {
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(batteryReceiver, filter);
                isReceiverRegistered = true;
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (isReceiverRegistered) {
                unregisterReceiver(batteryReceiver);
                isReceiverRegistered = false;
            }
        } catch (Exception ignored) {}
    }

    private void updateScreenText() {
        if (message != null) {
            String fullDisplayInfo = batteryInfo + memoryInfoStr + hardwareAndSensorInfo;
            message.setText(fullDisplayInfo);
        }
    }
}
