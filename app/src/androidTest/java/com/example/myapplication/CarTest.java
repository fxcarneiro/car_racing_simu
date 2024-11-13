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

/**
 * #### CarTest
 * Testa as funcionalidades de pausa e retomada de um carro. Valida que o carro
 * permanece estacionário enquanto está pausado e retoma o movimento ao ser reativado.
 */



public class CarTest {

    private Car car;
    private Bitmap mockBitmap;

    @Before
    public void setUp() {
        // Inicializa uma lista vazia de outros carros e um bitmap mockado para a pista
        List<Car> otherCars = new ArrayList<>();
        mockBitmap = Mockito.mock(Bitmap.class);

        // Cria uma instância do Carro com posição inicial e cor, e associa a lista de outros carros
        car = new Car("TestCar", 100, 100, Color.RED, otherCars);
    }

    @Test
    public void testPauseAndResumeCar() throws InterruptedException {
        // Inicia a corrida com o bitmap mockado e define as dimensões da pista
        car.startRace(mockBitmap, 200, 200);

        // Guarda as coordenadas iniciais de posição
        float initialX = car.getX();
        float initialY = car.getY();

        // Aguarda um tempo para que o carro se mova da posição inicial
        Thread.sleep(100);

        // Pausa o carro e registra a posição após a pausa
        car.pauseRace();
        float pausedX = car.getX();
        float pausedY = car.getY();

        // Espera e verifica que o carro não se move enquanto está pausado
        Thread.sleep(100);
        assertEquals("O carro deve permanecer na mesma posição enquanto está pausado", pausedX, car.getX(), 0.01);
        assertEquals("O carro deve permanecer na mesma posição enquanto está pausado", pausedY, car.getY(), 0.01);

        // Retoma o movimento do carro e espera para observar a movimentação
        car.resumeRace();
        Thread.sleep(100);

        // Assegura que o carro se moveu novamente após retomar a corrida
        assertTrue("O carro deve mover após retomar a corrida", car.getX() != pausedX || car.getY() != pausedY);
    }
}
