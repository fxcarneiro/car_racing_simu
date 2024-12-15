// Caminho do arquivo: com/example/myapplication/models/Car.java

package com.example.myapplication.models;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import com.example.myapplication.interfaces.Vehicle;
import com.example.mylibrary.utils.CalculationUtils;
import com.example.mylibrary.utils.CarState;
import com.example.mylibrary2.utils.MetricsCollector; // Importação para coleta de métricas
import com.example.mylibrary2.utils.RealTimeScheduler; // Importação para escalonamento de tarefas
import com.example.myapplication.Metrics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * ### 3.2. Car
 * - **Descrição**: Implementa a interface `Vehicle` e representa um carro com propriedades e
 *   comportamentos específicos para a simulação, como posição, direção, velocidade, e combustível.
 *   Esta classe gerencia seu movimento e resposta a colisões e implementa `Runnable`, o que permite
 *   sua execução em uma thread.
 * - **Funcionalidades**:
 *   - Movimenta-se ponto a ponto e atualiza a variável `distance`.
 *   - Controla colisões e incrementa a penalidade (`penalty`) em colisões.
 *   - Usa um semáforo para gerenciar regiões críticas na pista.
 *   - Coleta métricas de desempenho e integra-se a um escalonador de tarefas.
 */

public class Car implements Vehicle, Runnable, CarState {
    private final String name;
    private float x, y;
    private double direction;
    private float speed;
    protected final float initialSpeed = 50.0f;
    public static final float CAR_WIDTH = 46;
    public static final float CAR_HEIGHT = 20;
    private final Paint carPaint;
    private int distance;
    private int penalty;
    private int lapsCompleted;
    private final float startX;
    private final float startY;
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private Bitmap trackBitmap;
    private final List<Car> otherCars;
    private float scaleX, scaleY;
    private static final String TAG = "CarMovement";

    private Thread carThread;
    private float accumulatedMoveX = 0;
    private float accumulatedMoveY = 0;

    private int fuelTank;
    private final int initialFuel = 5000;
    private final Map<Integer, Integer> sensor;

    private static final Semaphore regionSemaphore = new Semaphore(1);
    private static final float CRITICAL_REGION_X_START = 120;
    private static final float CRITICAL_REGION_X_END = 173;
    private static final float CRITICAL_REGION_Y_START = 467;
    private static final float CRITICAL_REGION_Y_END = 493;

    protected final MetricsCollector metricsCollector;

    public Car(String name, float startX, float startY, int carColor, List<Car> otherCars, MetricsCollector metricsCollector) {
        this.name = name;
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        this.speed = initialSpeed;
        this.carPaint = new Paint();
        this.carPaint.setColor(carColor);
        this.distance = 0;
        this.penalty = 0;
        this.lapsCompleted = 0;
        this.fuelTank = initialFuel;
        this.sensor = new HashMap<>();
        this.otherCars = otherCars;
        this.metricsCollector = metricsCollector; // Assign passed MetricsCollector
    }


    @Override
    public void resetParameters() {
        // Redefine os valores iniciais para o carro
        this.x = this.startX;
        this.y = this.startY;
        this.direction = 90; // Direção inicial (90 graus)
        this.speed = this.initialSpeed; // Velocidade inicial
        this.distance = 0; // Reinicia a distância
        this.penalty = 0; // Remove penalidades
        this.lapsCompleted = 0; // Zera as voltas completas
        this.fuelTank = this.initialFuel; // Reabastece o tanque de combustível
        this.accumulatedMoveX = 0; // Reinicia o movimento acumulado no eixo X
        this.accumulatedMoveY = 0; // Reinicia o movimento acumulado no eixo Y
        Log.d("Car", this.name + " resetou os parâmetros para os valores iniciais.");
    }

    @Override
    public Metrics collectMetrics() {
        // Calcula métricas como jitter, tempo de resposta e utilização
        long jitter = System.currentTimeMillis() % 100; // Simulação de jitter
        long responseTime = (long) (Math.random() * 500); // Simulação de tempo de resposta
        double utilization = (speed / initialSpeed) * 100; // Exemplo de cálculo de utilização

        Log.d("Car", "Métricas coletadas para " + name + ": Jitter=" + jitter +
                "ms, Tempo de resposta=" + responseTime + "ms, Utilização=" + utilization + "%");

        return new Metrics(jitter, responseTime, utilization);
    }

    // Implementação dos métodos de CarState
    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public double getDirection() {
        return direction;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public int getFuelTank() {
        return fuelTank;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    @Override
    public int getPenalty() {
        return penalty;
    }

    @Override
    public int getLapsCompleted() {
        return lapsCompleted;
    }

    @Override
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setDirection(double direction) {
        this.direction = direction;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void setFuelTank(int fuelTank) {
        this.fuelTank = fuelTank;
    }

    @Override
    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public void setPenalty(int penalty) {
        this.penalty = penalty;
    }

    @Override
    public void setLapsCompleted(int lapsCompleted) {
        this.lapsCompleted = lapsCompleted;
    }

    public void resetFuel() {
        this.fuelTank = initialFuel;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    @Override
    public synchronized void startRace(Bitmap trackBitmap, int trackWidth, int trackHeight) {
        if (trackBitmap == null) {
            Log.e(TAG, "Erro: trackBitmap não foi inicializado antes de iniciar o carro " + name);
            return;
        }

        this.trackBitmap = trackBitmap;
        this.scaleX = (float) trackBitmap.getWidth() / trackWidth;
        this.scaleY = (float) trackBitmap.getHeight() / trackHeight;
        isRunning = true;
        isPaused = false;

        // Integrar com o RealTimeScheduler
        RealTimeScheduler scheduler = new RealTimeScheduler();
        long deadline = System.currentTimeMillis() + 5000; // Exemplo de deadline
        scheduler.scheduleTask(getName(), deadline, Thread.NORM_PRIORITY, this::run);

        if (carThread == null || !carThread.isAlive()) {
            carThread = new Thread(this);
            carThread.start();
        } else {
            synchronized (this) {
                notifyAll();
            }
        }
    }


    private float adjustSpeed(float currentSpeed, boolean isDelayed) {
        if (isDelayed) {
            return currentSpeed + 10; // Aumenta velocidade em 10
        }
        return currentSpeed - 5; // Reduz velocidade em 5
    }

    @Override
    public void run() {
        long lastUpdateTime = System.currentTimeMillis();

        while (isRunning) {
            try {
                synchronized (this) {
                    while (isPaused) {
                        wait();
                    }
                }

                long currentTime = System.currentTimeMillis();
                double deltaTime = (currentTime - lastUpdateTime) / 1000.0;
                long jitter = currentTime - lastUpdateTime;
                lastUpdateTime = currentTime;

                boolean inCriticalRegion = isInCriticalRegion(x, y);
                if (inCriticalRegion) {
                    regionSemaphore.acquire();
                }

                if (fuelTank > 0) {
                    updateSensors();
                    manageSpeedAndDirection(deltaTime);
                    move(deltaTime);
                    checkLapCompletion();
                } else {
                    Log.d(TAG, name + " está sem combustível. Parando o carro.");
                    stopRace();
                }

                // Collect metrics using the instance passed to the constructor
                metricsCollector.collectMetric(getName(), jitter, (long) (deltaTime * 1000), (long) speed);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Log.e(TAG, "Erro no método run para o carro " + name, e);
            } finally {
                if (isInCriticalRegion(x, y) && regionSemaphore.availablePermits() == 0) {
                    regionSemaphore.release();
                }
            }

            try {
                Thread.sleep(50); // Controle de taxa de atualização
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Export metrics at the end of the simulation
        try {
            metricsCollector.exportMetrics("car_metrics_" + getName() + ".csv");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar métricas", e);
        }
    }

    @Override
    public synchronized void pauseRace() {
        this.isPaused = true;
        Log.d(TAG, name + " está pausado.");
    }

    @Override
    public synchronized void resumeRace() {
        if (isPaused) {
            isPaused = false;
            notifyAll();
            Log.d(TAG, name + " retomou a corrida.");
        }
    }

    private boolean isInCriticalRegion(float x, float y) {
        return x >= CRITICAL_REGION_X_START && x <= CRITICAL_REGION_X_END &&
                y >= CRITICAL_REGION_Y_START && y <= CRITICAL_REGION_Y_END;
    }

    @Override
    public void stopRace() {
        isRunning = false;
        if (carThread != null) {
            carThread.interrupt();
            carThread = null;
        }
    }

    private void updateSensors() {
        sensor.clear();
        int[] angles = {0, 45, 90, 135, 180, 225, 270, 315};
        for (int angle : angles) {
            int distance = measureDistanceInDirection(angle);
            sensor.put(angle, distance);
        }
    }

    private int measureDistanceInDirection(int angle) {
        float testX = x, testY = y;
        int maxDistance = 100;
        for (int d = 1; d <= maxDistance; d++) {
            testX = x + (float) (Math.cos(Math.toRadians(direction + angle)) * d);
            testY = y + (float) (Math.sin(Math.toRadians(direction + angle)) * d);

            if (!isOnTrack(testX, testY)) {
                penalty++;
                return d;
            }
        }
        return maxDistance;
    }

    /**
     * Gerencia a velocidade e direção do carro, considerando possíveis carros à frente.
     */
    private void manageSpeedAndDirection(double deltaTime) {
        Car carAhead = detectCarAhead();

        float maxSpeed = 150.0f;
        if (carAhead != null) {
            if (canOvertake(carAhead)) {
                adjustDirectionToOvertake(carAhead);
                speed = Math.min(speed + 5 * (float) deltaTime, maxSpeed);
            } else {
                slowDownSmoothly();
            }
        } else {
            speed = Math.min(speed + 2 * (float) deltaTime, maxSpeed);
        }
    }

    private Car detectCarAhead() {
        for (Car otherCar : otherCars) {
            if (otherCar != this && isCarInFront(otherCar) && isWithinStoppingDistance(otherCar)) {
                return otherCar;
            }
        }
        return null;
    }

    private boolean isWithinStoppingDistance(Car otherCar) {
        float distanceToOtherCar = CalculationUtils.calculateDistance(this.x, this.y, otherCar.getX(), otherCar.getY());
        return distanceToOtherCar < CAR_WIDTH * 2;
    }

    private boolean isCarInFront(Car otherCar) {
        float distanceToOtherCar = CalculationUtils.calculateDistance(this.x, this.y, otherCar.getX(), otherCar.getY());
        return distanceToOtherCar < 80 && distanceToOtherCar > 0;
    }

    private boolean canOvertake(Car carAhead) {
        return Math.abs(this.y - carAhead.getY()) > CAR_HEIGHT * 1.5 && isClearPathForOvertake(carAhead);
    }

    private boolean isClearPathForOvertake(Car carAhead) {
        float offset = CAR_WIDTH * 2;
        float potentialY = this.y > carAhead.getY() ? carAhead.getY() - offset : carAhead.getY() + offset;
        return isOnTrack(this.x, potentialY);
    }

    private void adjustDirectionToOvertake(Car carAhead) {
        if (this.y > carAhead.getY()) {
            direction += 5;
        } else {
            direction -= 5;
        }
    }

    private void slowDownSmoothly() {
        float minSpeed = 25.0f;
        speed = Math.max(speed - 5, minSpeed);
    }

    public void move(double deltaTime) {
        if (isPaused || fuelTank <= 0) return;

        float moveX = (float) Math.cos(Math.toRadians(direction)) * (float) deltaTime * speed;
        float moveY = (float) Math.sin(Math.toRadians(direction)) * (float) deltaTime * speed;

        accumulatedMoveX += moveX;
        accumulatedMoveY += moveY;

        if (Math.abs(accumulatedMoveX) >= 1.0) {
            float targetX = x + Math.signum(accumulatedMoveX);
            if (isOnTrack(targetX, y)) {
                x = targetX;
                distance++;
                consumeFuel();
                accumulatedMoveX -= Math.signum(accumulatedMoveX);
            } else {
                penalty++;
                adjustDirection();
                accumulatedMoveX = 0;
            }
        }

        if (Math.abs(accumulatedMoveY) >= 1.0) {
            float targetY = y + Math.signum(accumulatedMoveY);
            if (isOnTrack(x, targetY)) {
                y = targetY;
                distance++;
                consumeFuel();
                accumulatedMoveY -= Math.signum(accumulatedMoveY);
            } else {
                penalty++;
                adjustDirection();
                accumulatedMoveY = 0;
            }
        }
    }

    private void consumeFuel() {
        if (fuelTank > 0) {
            fuelTank -= 0.1;
            if (fuelTank <= 0) {
                fuelTank = 0;
                Log.d(TAG, name + " está sem combustível. Chamando stopRace.");
                stopRace();
            }
        }
    }

    private void checkLapCompletion() {
        if (isNearStart()) {
            lapsCompleted++;
            Log.d(TAG, name + " completou " + lapsCompleted + " voltas.");
        }
    }

    private boolean isNearStart() {
        float distanceToStart = CalculationUtils.calculateDistance(this.x, this.y, startX, startY);
        return distanceToStart < CAR_WIDTH;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) direction, CAR_WIDTH / 2, CAR_HEIGHT / 2);
        RectF rect = new RectF(-CAR_WIDTH / 2, -CAR_HEIGHT / 2, CAR_WIDTH / 2, CAR_HEIGHT / 2);
        canvas.drawRect(rect, carPaint);
        canvas.restore();
    }

    public boolean checkCollision(Car otherCar) {
        return CalculationUtils.calculateDistance(this.x, this.y, otherCar.getX(), otherCar.getY()) < CAR_WIDTH;
    }

    private boolean isOnTrack(float testX, float testY) {
        return trackBitmap != null &&
                isPointOnTrack(testX, testY) &&
                isPointOnTrack(testX + CAR_WIDTH / 2, testY) &&
                isPointOnTrack(testX - CAR_WIDTH / 2, testY) &&
                isPointOnTrack(testX, testY + CAR_HEIGHT / 2) &&
                isPointOnTrack(testX, testY - CAR_HEIGHT / 2);
    }

    private boolean isPointOnTrack(float testX, float testY) {
        if (trackBitmap == null) {
            Log.e(TAG, "trackBitmap é null ao tentar acessar isPointOnTrack para o carro " + name);
            return false;
        }

        int bitmapX = (int) (testX * scaleX);
        int bitmapY = (int) (testY * scaleY);

        if (bitmapX >= 0 && bitmapX < trackBitmap.getWidth() && bitmapY >= 0 && bitmapY < trackBitmap.getHeight()) {
            int pixelColor = trackBitmap.getPixel(bitmapX, bitmapY);
            return pixelColor == Color.WHITE;
        }
        return false;
    }

    private void adjustDirection() {
        double adjustmentAngle = 10.0;
        double initialDirection = direction;

        for (int i = 1; i <= 9; i++) {
            double newDirection = initialDirection + i * adjustmentAngle;
            if (newDirection >= 360) newDirection -= 360;

            if (tryDirection(newDirection)) return;

            newDirection = initialDirection - i * adjustmentAngle;
            if (newDirection < 0) newDirection += 360;

            if (tryDirection(newDirection)) return;
        }
    }

    private boolean tryDirection(double newDirection) {
        float forwardX = x + (float) Math.cos(Math.toRadians(newDirection)) * 5;
        float forwardY = y + (float) Math.sin(Math.toRadians(newDirection)) * 5;

        if (isOnTrack(forwardX, forwardY)) {
            direction = newDirection;
            return true;
        }
        return false;
    }

    protected boolean isPaused() {
        return isPaused;
    }

    public void resetAccumulatedMoveX() {
        this.accumulatedMoveX = 0;
    }

    public void resetAccumulatedMoveY() {
        this.accumulatedMoveY = 0;
    }
}
