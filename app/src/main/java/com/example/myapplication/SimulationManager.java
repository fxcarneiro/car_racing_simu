package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

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

/**
 * SimulationManager class handles the management of a racing simulation, including vehicle creation,
 * state control (start, pause, resume, and finish), and collision monitoring.
 */
public class SimulationManager {

    private final CopyOnWriteArrayList<Vehicle> vehicles; // Thread-safe list for all vehicles
    private final CopyOnWriteArrayList<Car> cars;         // Thread-safe list for cars only
    private TrackView trackView;                         // View for rendering the track
    private SafetyCar safetyCar;                         // Instance of the safety car
    private boolean isRunning;                           // Indicates if the simulation is running
    private boolean isPaused;                            // Indicates if the simulation is paused
    private boolean isFinished;                          // Indicates if the simulation has finished
    private final float startX = 75;                     // Initial X-coordinate for cars
    private final float startY = 400;                    // Initial Y-coordinate for cars
    private final int[] carColors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA}; // Car colors
    private static final String TAG = "SimulationManager";

    private final CarStateRepository carStateRepository = new CarStateRepository(); // Repository for car states
    private final RealTimeScheduler scheduler;         // Task scheduler
    private final MetricsCollector metricsCollector;   // Metrics collector
    private final Context context;                     // Application context

    /**
     * Constructor initializes the simulation and sets the initial state.
     *
     * @param context Application context for accessing resources and files.
     */
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

    /**
     * Resets the simulation state variables.
     */
    private void resetSimulationState() {
        isPaused = false;
        isFinished = false;
        isRunning = false;
    }

    /**
     * Starts the simulation with the specified number of vehicles.
     *
     * @param vehicleCount Number of vehicles to simulate.
     * @throws IOException If metrics cannot be exported.
     */
    public void startSimulation(int vehicleCount) throws IOException {
        if (vehicleCount <= 0) {
            Log.e(TAG, "Invalid number of vehicles: " + vehicleCount);
            return;
        }

        if (!isRunning) {
            resetSimulationState();
            ThreadManager.configureProcessors(4); // Configure 4 cores for execution
            loadCarStatesAndInitialize(vehicleCount);

            // Execute scheduled tasks
            scheduler.executeTasks();

            // Ensure metrics file and directory exist
            File exportFile = createMetricsFile("simulation_metrics.csv");
            if (exportFile == null) {
                Log.e(TAG, "Failed to create metrics file.");
                return;
            }

            metricsCollector.exportMetrics(exportFile.getAbsolutePath());
            Log.d(TAG, "Metrics exported to: " + exportFile.getAbsolutePath());
        }
    }

    /**
     * Creates a metrics file in the application's private storage.
     *
     * @param fileName Name of the file to create.
     * @return The File object for the created file, or null if creation fails.
     */
    private File createMetricsFile(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + parentDir.getAbsolutePath());
                return null;
            }
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                Log.e(TAG, "Failed to create file: " + file.getAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error creating file: " + file.getAbsolutePath(), e);
            return null;
        }
        return file;
    }

    /**
     * Initializes car states and creates the specified number of vehicles.
     *
     * @param vehicleCount Number of vehicles to create.
     */
    private void loadCarStatesAndInitialize(int vehicleCount) {
        vehicles.clear();
        cars.clear();

        MetricsCollector metricsCollector = new MetricsCollector(context); // Single instance

        for (int i = 0; i < vehicleCount; i++) {
            int carColor = carColors[i % carColors.length];
            Car car = new Car("Car" + (i + 1), startX, startY, carColor, cars, metricsCollector); // Pass the instance
            vehicles.add(car);
            cars.add(car);
        }
    }

    /**
     * Initializes the SafetyCar and sets its position on the track.
     */
    private void initializeSafetyCar() {
        if (safetyCar == null) {
            safetyCar = new SafetyCar("SafetyCar", startX, startY, Color.BLACK, metricsCollector);
        }
        vehicles.add(safetyCar);
        cars.add(safetyCar);
        safetyCar.setPosition(startX, startY);
    }

    /**
     * Pauses the simulation.
     */
    public void pauseSimulation() {
        if (isRunning && !isFinished && !isPaused) {
            isPaused = true;
            for (Vehicle vehicle : vehicles) {
                vehicle.pauseRace();
            }
            Log.d(TAG, "Simulation paused.");
        }
    }

    /**
     * Resumes the simulation.
     */
    public void resumeSimulation() {
        if (isRunning && isPaused) {
            isPaused = false;
            for (Vehicle vehicle : vehicles) {
                vehicle.resumeRace();
            }
            Log.d(TAG, "Simulation resumed.");
        }
    }

    /**
     * Stops the simulation and clears all vehicles.
     */
    public void finishSimulation() {
        isRunning = false;
        isPaused = false;
        isFinished = true;

        for (Vehicle vehicle : vehicles) {
            vehicle.stopRace();
        }
        vehicles.clear();
        cars.clear();
        Log.d(TAG, "Simulation finished.");
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
