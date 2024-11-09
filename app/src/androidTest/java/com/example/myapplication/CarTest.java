package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.myapplication.models.Car;

public class CarTest {

    private Car car;
    private Bitmap mockBitmap;

    @Before
    public void setUp() {
        List<Car> otherCars = new ArrayList<>();
        mockBitmap = Mockito.mock(Bitmap.class);

        car = new Car("TestCar", 100, 100, Color.RED, otherCars);
    }

    @Test
    public void testPauseAndResumeCar() throws InterruptedException {
        // Configura o mock para o bitmap de pista e inicializa o carro
        car.startRace(mockBitmap, 200, 200);

        // Verifica a posição inicial
        float initialX = car.getX();
        float initialY = car.getY();

        // Aguarda um momento para que o carro comece a se mover
        Thread.sleep(100);

        // Pausa o carro e salva a posição
        car.pauseRace();
        float pausedX = car.getX();
        float pausedY = car.getY();

        // Espera para verificar que o carro não se move enquanto está pausado
        Thread.sleep(100);
        assertEquals("O carro deve permanecer na mesma posição enquanto está pausado", pausedX, car.getX(), 0.01);
        assertEquals("O carro deve permanecer na mesma posição enquanto está pausado", pausedY, car.getY(), 0.01);

        // Retoma a corrida e aguarda para ver se o carro se move novamente
        car.resumeRace();
        Thread.sleep(100);

        // Verifica se o carro retomou o movimento
        assertTrue("O carro deve mover após retomar a corrida", car.getX() != pausedX || car.getY() != pausedY);
    }
}
