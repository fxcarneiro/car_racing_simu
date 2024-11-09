package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import com.example.myapplication.interfaces.Vehicle;
import com.example.myapplication.models.Car;
import com.example.myapplication.models.SafetyCar;
import com.example.myapplication.utils.CarStateRepository;

import java.util.ArrayList;
import java.util.List;

public class SimulationManager {

    private List<Vehicle> vehicles;
    private List<Car> cars;
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

    public SimulationManager(Context context) {
        try {
            vehicles = new ArrayList<>();
            cars = new ArrayList<>();
            trackView = new TrackView(context, new Car[0]);
            resetSimulationState();
            initializeSafetyCar();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar SimulationManager", e);
        }
    }

    private void resetSimulationState() {
        isPaused = false;
        isFinished = false;
        isRunning = false;
    }

    public void startSimulation(int vehicleCount) {
        try {
            if (vehicleCount <= 0) {
                Log.e(TAG, "Número de veículos inválido: " + vehicleCount);
                return;
            }

            if (!isRunning) {
                resetSimulationState();
                loadCarStatesAndInitialize(vehicleCount);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar a simulação", e);
        }
    }

    private void loadCarStatesAndInitialize(int vehicleCount) {
        vehicles.clear();
        cars.clear();

        List<Car> loadedCars = new ArrayList<>();

        for (int i = 0; i < vehicleCount; i++) {
            int carColor = carColors[i % carColors.length];
            Car car = new Car("Car" + (i + 1), startX, startY, carColor, cars);
            carStateRepository.loadCarState(car, loadedCar -> {
                if (loadedCar != null) {
                    vehicles.add(loadedCar);
                    cars.add(loadedCar);
                } else {
                    vehicles.add(car);
                    cars.add(car);
                }
                loadedCars.add(car);

                // Quando todos os carros estiverem carregados, inclua o Safety Car e atualize o TrackView
                if (loadedCars.size() == vehicleCount) {
                    initializeSafetyCar();
                    trackView.updateCars(cars.toArray(new Car[0]));
                    trackView.invalidate();  // Atualiza o TrackView
                    startVehiclesSequentially(); // Inicia a simulação
                    isRunning = true;
                    isFinished = false;
                    Log.d(TAG, "Simulação iniciada com " + vehicleCount + " veículos.");
                }
            });
        }
    }

    private void initializeSafetyCar() {
        try {
            if (safetyCar == null) {
                safetyCar = new SafetyCar("SafetyCar", startX, startY, Color.BLACK);
                carStateRepository.loadCarState(safetyCar, loadedSafetyCar -> {
                    if (loadedSafetyCar != null) {
                        safetyCar = (SafetyCar) loadedSafetyCar;
                    }
                    vehicles.add(safetyCar);
                    cars.add(safetyCar);
                    carStateRepository.saveCarState(safetyCar); // Garante que o Safety Car seja salvo no Firestore
                    trackView.updateCars(cars.toArray(new Car[0]));
                    trackView.invalidate();
                });
            } else {
                vehicles.add(safetyCar);
                cars.add(safetyCar);
            }
            safetyCar.setPosition(startX, startY);
            safetyCar.setRunning(false);
            safetyCar.resetFuel();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar Safety Car", e);
        }
    }

    private void startVehiclesSequentially() {
        try {
            if (trackView.getTrackBitmap() != null) {
                isRunning = true;
                isPaused = false;

                new Thread(() -> {
                    for (Vehicle vehicle : vehicles) {
                        try {
                            vehicle.startRace(trackView.getTrackBitmap(), trackView.getWidth(), trackView.getHeight());
                            if (vehicle instanceof SafetyCar) {
                                ((SafetyCar) vehicle).setRunning(true);
                            }
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            Log.e(TAG, "Thread interrompida ao iniciar veículos", e);
                        } catch (Exception e) {
                            Log.e(TAG, "Erro ao iniciar veículo " + vehicle, e);
                        }
                    }
                }).start();

                monitorCollisions();
            } else {
                Log.e(TAG, "Bitmap da pista não está carregado, não é possível iniciar a simulação.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar veículos sequencialmente", e);
        }
    }

    private void monitorCollisions() {
        try {
            new Thread(() -> {
                while (isRunning && !isFinished && !Thread.currentThread().isInterrupted()) {
                    try {
                        checkCollisions();
                        Thread.sleep(100); // Intervalo para checar colisões
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Log.e(TAG, "Thread interrompida durante a monitoração de colisões", e);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro durante a monitoração de colisões", e);
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao iniciar monitoração de colisões", e);
        }
    }

    private void checkCollisions() {
        try {
            for (int i = 0; i < cars.size(); i++) {
                Car carA = cars.get(i);
                for (int j = i + 1; j < cars.size(); j++) {
                    Car carB = cars.get(j);
                    try {
                        if (carA.checkCollision(carB)) {
                            Log.d(TAG, "Colisão detectada entre " + carA.getName() + " e " + carB.getName());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao verificar colisão entre " + carA.getName() + " e " + carB.getName(), e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar colisões", e);
        }
    }

    public void pauseSimulation() {
        try {
            if (isRunning && !isFinished && !isPaused) {
                isPaused = true;
                for (Vehicle vehicle : vehicles) {
                    try {
                        vehicle.pauseRace();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao pausar veículo " + vehicle, e);
                    }
                }

                for (Car car : cars) {
                    carStateRepository.saveCarState(car);
                }
                Log.d(TAG, "Simulação pausada.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao pausar simulação", e);
        }
    }

    public void resumeSimulation() {
        try {
            if (isRunning && isPaused) {
                isPaused = false;
                for (Vehicle vehicle : vehicles) {
                    try {
                        vehicle.resumeRace();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao retomar veículo " + vehicle, e);
                    }
                }
                Log.d(TAG, "Simulação retomada.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao retomar simulação", e);
        }
    }

    public void finishSimulation() {
        try {
            isRunning = false;
            isPaused = false;
            isFinished = true;

            for (Vehicle vehicle : vehicles) {
                try {
                    vehicle.stopRace();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao finalizar veículo " + vehicle, e);
                }
            }

            // Reinicia os parâmetros de cada carro
            for (Car car : cars) {
                car.setPosition(startX, startY);
                car.setDirection(90); // Ajuste de direção padrão
                car.resetFuel();
                car.setDistance(0);
                car.setPenalty(0);
                car.setLapsCompleted(0);
                carStateRepository.saveCarState(car); // Salva o estado reiniciado no Firestore
            }

            vehicles.clear();
            cars.clear();

            trackView.updateCars(new Car[0]);
            trackView.invalidate();

            if (safetyCar != null) {
                safetyCar.setPosition(startX, startY);
                safetyCar.setRunning(false);
                safetyCar.resetFuel();
                safetyCar.resetParameters();
                carStateRepository.saveCarState(safetyCar); // Salva o estado reiniciado do Safety Car
            }
            Log.d(TAG, "Simulação finalizada e parâmetros reiniciados.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao finalizar simulação", e);
        }
    }

    public TrackView getTrackView() {
        try {
            return trackView;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter TrackView", e);
            return null;
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
