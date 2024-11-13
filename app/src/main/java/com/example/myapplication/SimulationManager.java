// Caminho do arquivo: com/example/myapplication/SimulationManager.java

package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import com.example.myapplication.interfaces.Vehicle;
import com.example.myapplication.models.Car;
import com.example.myapplication.models.SafetyCar;
import com.example.mylibrary.utils.CarStateRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe SimulationManager gerencia a simulação de corrida, incluindo a criação de veículos,
 * controle de estado (início, pausa, retomada e finalização) e monitoramento de colisões.
 * Responsável por coordenar o movimento dos carros, incluindo o SafetyCar, e por manter
 * o estado da simulação centralizado.
 */
public class SimulationManager {

    private List<Vehicle> vehicles;              // Lista de todos os veículos na simulação
    private List<Car> cars;                      // Lista específica de carros (exclui SafetyCar)
    private TrackView trackView;                 // Exibição da pista
    private SafetyCar safetyCar;                 // Instância do carro de segurança
    private boolean isRunning;                   // Flag de estado: se a simulação está em execução
    private boolean isPaused;                    // Flag de estado: se a simulação está pausada
    private boolean isFinished;                  // Flag de estado: se a simulação foi finalizada
    private final float startX = 75;             // Coordenada inicial X para veículos
    private final float startY = 400;            // Coordenada inicial Y para veículos
    private final int[] carColors = {Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA};  // Cores dos carros
    private static final String TAG = "SimulationManager";
    private final CarStateRepository carStateRepository = new CarStateRepository();  // Repositório de estado dos carros

    /**
     * Construtor que inicializa a simulação e configura o estado inicial.
     */
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

    /**
     * Reinicia o estado da simulação para os valores iniciais.
     */
    private void resetSimulationState() {
        isPaused = false;
        isFinished = false;
        isRunning = false;
    }

    /**
     * Inicia a simulação com um número especificado de veículos.
     */
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

    /**
     * Carrega os estados dos carros salvos e inicializa o número especificado de carros.
     */
    private void loadCarStatesAndInitialize(int vehicleCount) {
        vehicles.clear();
        cars.clear();

        List<Car> loadedCars = new ArrayList<>();

        // Cria e carrega o estado de cada carro
        for (int i = 0; i < vehicleCount; i++) {
            int carColor = carColors[i % carColors.length];
            Car car = new Car("Car" + (i + 1), startX, startY, carColor, cars);
            carStateRepository.loadCarState(car, loadedCar -> {
                if (loadedCar instanceof Car) { // Verifica se o estado carregado é uma instância de Car
                    vehicles.add((Car) loadedCar); // Adiciona à lista de veículos e carros
                    cars.add((Car) loadedCar);
                } else {
                    vehicles.add(car);
                    cars.add(car);
                }
                loadedCars.add(car);

                // Quando todos os carros estiverem carregados, inicializa o SafetyCar e o TrackView
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

    /**
     * Inicializa o SafetyCar e o configura na pista.
     */
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
                    carStateRepository.saveCarState(safetyCar); // Salva o estado inicial do Safety Car
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

    /**
     * Inicia a corrida de cada veículo em sequência, com uma pausa entre eles.
     */
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

    /**
     * Monitora continuamente a pista para detectar colisões entre veículos.
     */
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

    /**
     * Verifica colisões entre todos os carros na simulação.
     */
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

    /**
     * Pausa a simulação e salva o estado atual de cada carro.
     */
    public void pauseSimulation() {
        try {
            if (isRunning && !isFinished && !isPaused) {
                isPaused = true;
                for (Vehicle vehicle : vehicles) {
                    vehicle.pauseRace();
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

    /**
     * Retoma a simulação de onde parou.
     */
    public void resumeSimulation() {
        try {
            if (isRunning && isPaused) {
                isPaused = false;
                for (Vehicle vehicle : vehicles) {
                    vehicle.resumeRace();
                }
                Log.d(TAG, "Simulação retomada.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao retomar simulação", e);
        }
    }

    /**
     * Finaliza a simulação e reinicia os parâmetros dos veículos.
     */
    public void finishSimulation() {
        try {
            isRunning = false;
            isPaused = false;
            isFinished = true;

            for (Vehicle vehicle : vehicles) {
                vehicle.stopRace();
            }

            // Reinicia os parâmetros de cada carro para o estado inicial
            for (Car car : cars) {
                car.setPosition(startX, startY);
                car.setDirection(90);
                car.resetFuel();
                car.setDistance(0);
                car.setPenalty(0);
                car.setLapsCompleted(0);
                carStateRepository.saveCarState(car);
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
                carStateRepository.saveCarState(safetyCar);
            }
            Log.d(TAG, "Simulação finalizada e parâmetros reiniciados.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao finalizar simulação", e);
        }
    }

    /**
     * Retorna a TrackView associada à simulação.
     */
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
