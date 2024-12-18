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

    public TrackView getTrackView() {
        return trackView;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isRunning() {
        return isRunning;
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

            trackView.updateCars(cars.toArray(new Car[0]));

            isRunning = true;
            Log.d(TAG, "Simulação iniciada.");

            startDynamicPriorityAdjustment();
            monitorSimulation();
            triggerAperiodicEvents();

            File exportFile = createMetricsFile("simulation_metrics.csv");
            if (exportFile != null) {
                metricsCollector.exportMetrics(exportFile.getAbsolutePath());
                Log.d(TAG, "Métricas exportadas para: " + exportFile.getAbsolutePath());
            }
        }
    }

    public void pauseSimulation() {
        if (isRunning && !isPaused) {
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
        if (isRunning) {
            isRunning = false;
            isPaused = false;
            isFinished = true;

            for (Vehicle vehicle : vehicles) {
                vehicle.stopRace();
            }

            File exportFile = createMetricsFile("final_metrics.csv");
            if (exportFile != null) {
                try {
                    metricsCollector.exportMetrics(exportFile.getAbsolutePath());
                    Log.d(TAG, "Métricas finais exportadas.");
                } catch (IOException e) {
                    Log.e(TAG, "Erro ao exportar métricas finais.", e);
                }
            }

            vehicles.clear();
            cars.clear();
            Log.d(TAG, "Simulação finalizada.");
        }
    }

    private void startDynamicPriorityAdjustment() {
        new Thread(() -> {
            while (isRunning) {
                for (Car car : cars) {
                    scheduler.adjustTaskPriority(car.getName(), calculatePriority(car));
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void triggerAperiodicEvents() {
        new Thread(() -> {
            while (isRunning) {
                scheduler.scheduleTask("AperiodicEvent", System.currentTimeMillis() + 2000, 1, () -> {
                    Log.d(TAG, "[T4 - Evento Aperiódico] Iniciado.");
                    pauseVehiclesTemporarily();
                    Log.d(TAG, "[T4 - Evento Aperiódico] Concluído.");
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private void pauseVehiclesTemporarily() {
        for (Vehicle vehicle : vehicles) {
            vehicle.pauseRace();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (Vehicle vehicle : vehicles) {
            vehicle.resumeRace();
        }
    }

    private int calculatePriority(Car car) {
        long remainingTime = car.getDeadlineRemaining();
        int distance = car.getDistance();

        if (remainingTime < 3000) return Thread.MAX_PRIORITY;
        if (distance > 500) return Thread.NORM_PRIORITY + 2;
        return Thread.NORM_PRIORITY;
    }

    private void monitorSimulation() {
        new Thread(() -> {
            while (isRunning) {
                for (Car car : cars) {
                    long remainingTime = car.getDeadlineRemaining();
                    Log.d(TAG, String.format("%s - Tempo restante: %d ms - Distância: %d",
                            car.getName(), remainingTime, car.getDistance()));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    private File createMetricsFile(String fileName) {
        File dir = context.getExternalFilesDir(null);
        if (dir == null || (!dir.exists() && !dir.mkdirs())) {
            Log.e(TAG, "Erro ao acessar ou criar diretório.");
            return null;
        }

        File file = new File(dir, fileName);
        try {
            if (!file.exists() && !file.createNewFile()) {
                Log.e(TAG, "Falha ao criar o arquivo: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            Log.e(TAG, "Erro ao criar o arquivo.", e);
        }
        return file;
    }

    private void loadCarStatesAndInitialize(int vehicleCount) {
        vehicles.clear();
        cars.clear();

        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < vehicleCount; i++) {
            int carColor = carColors[i % carColors.length];
            Car car = new Car("Car" + (i + 1), startX, startY, carColor, cars, metricsCollector);
            car.setDeadline(currentTime + (i + 1) * 5000);
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
}
