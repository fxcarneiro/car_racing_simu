package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.myapplication.interfaces.Vehicle;
import com.example.myapplication.models.Car;
import com.example.myapplication.models.SafetyCar;
import com.example.mylibrary.utils.CarStateRepository;
import com.example.mylibrary2.utils.MetricsCollector;
import com.example.mylibrary2.utils.RealTimeScheduler;
import com.example.mylibrary2.utils.ThreadManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimulationManager {

    private final CopyOnWriteArrayList<Vehicle> vehicles;
    private final CopyOnWriteArrayList<Car> cars;
    private TrackView trackView;
    private SafetyCar safetyCar;
    private boolean isRunning;
    private boolean isPaused;
    private boolean isFinished;
    private final float startX = 75;
    private final float startY = 400;
    private final int[] carColors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA};
    private static final String TAG = "SimulationManager";

    private final CarStateRepository carStateRepository = new CarStateRepository();
    private final RealTimeScheduler scheduler;
    private final MetricsCollector metricsCollector;
    private final Context context;

    public SimulationManager(Context context) {
        this.context = context;
        this.vehicles = new CopyOnWriteArrayList<>();
        this.cars = new CopyOnWriteArrayList<>();
        this.trackView = new TrackView(context, new Car[0]);
        this.scheduler = new RealTimeScheduler();
        this.metricsCollector = new MetricsCollector(context);

        resetSimulationState();
        initializeSafetyCar();
    }

    private void resetSimulationState() {
        isPaused = false;
        isFinished = false;
        isRunning = false;
    }

    public void startSimulation(int vehicleCount) throws IOException {
        if (vehicleCount <= 0) {
            Log.e(TAG, "Número inválido de veículos: " + vehicleCount);
            return;
        }

        if (!isRunning) {
            resetSimulationState();
            ThreadManager.configureProcessors(4);
            loadCarStatesAndInitialize(vehicleCount);

            // Atualiza o TrackView com os carros carregados
            trackView.updateCars(cars.toArray(new Car[0]));

            scheduler.executeTasks();

            if (!hasStoragePermission()) {
                Log.e(TAG, "Permissões de armazenamento não concedidas. Operação abortada.");
                return;
            }

            File exportFile = createMetricsFile("simulation_metrics.csv");
            if (exportFile == null || !exportFile.exists()) {
                Log.e(TAG, "Erro ao criar ou acessar o arquivo de métricas.");
                return;
            }

            metricsCollector.exportMetrics(exportFile.getAbsolutePath());
            Log.d(TAG, "Métricas exportadas para: " + exportFile.getAbsolutePath());
            isRunning = true; // Marca a simulação como em execução
        }
    }

    private File createMetricsFile(String fileName) {
        File dir = context.getExternalFilesDir(null); // Diretório externo privado
        if (dir == null) {
            Log.e(TAG, "Erro ao acessar o diretório externo.");
            return null;
        }

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Erro ao criar o diretório: " + dir.getAbsolutePath());
                return null;
            }
        }

        File file = new File(dir, fileName);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    Log.d(TAG, "Arquivo criado com sucesso: " + file.getAbsolutePath());
                } else {
                    Log.e(TAG, "Falha ao criar o arquivo: " + file.getAbsolutePath());
                }
            } else {
                Log.d(TAG, "Arquivo já existente: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao criar o arquivo: " + fileName, e);
            return null;
        }

        return file;
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int writePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int readPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
            return writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void loadCarStatesAndInitialize(int vehicleCount) {
        vehicles.clear();
        cars.clear();

        for (int i = 0; i < vehicleCount; i++) {
            int carColor = carColors[i % carColors.length];
            Car car = new Car("Car" + (i + 1), startX, startY, carColor, cars, metricsCollector);
            vehicles.add(car);
            cars.add(car);
        }
    }

    private void initializeSafetyCar() {
        if (safetyCar == null) {
            safetyCar = new SafetyCar("SafetyCar", startX, startY, Color.BLACK, metricsCollector);
        }
        vehicles.add(safetyCar);
        cars.add(safetyCar);
        safetyCar.setPosition(startX, startY);
    }

    public void pauseSimulation() {
        if (isRunning && !isFinished && !isPaused) {
            isPaused = true;
            for (Vehicle vehicle : vehicles) {
                vehicle.pauseRace();
            }
            Log.d(TAG, "Simulação pausada.");
        }
    }

    public void resumeSimulation() {
        if (isRunning && isPaused) {
            isPaused = false;
            for (Vehicle vehicle : vehicles) {
                vehicle.resumeRace();
            }
            Log.d(TAG, "Simulação retomada.");
        }
    }

    public void finishSimulation() {
        isRunning = false;
        isPaused = false;
        isFinished = true;

        for (Vehicle vehicle : vehicles) {
            vehicle.stopRace();
        }
        vehicles.clear();
        cars.clear();
        Log.d(TAG, "Simulação finalizada.");
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public TrackView getTrackView() {
        return trackView;
    }
}
