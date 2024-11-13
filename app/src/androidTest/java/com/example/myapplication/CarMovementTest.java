package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.example.myapplication.models.Car;

/**
 * #### CarMovementTest
 * - Verifica se o método `move` da classe `Car` atualiza a posição do carro
 *   corretamente após simular um pequeno movimento.
 */



public class CarMovementTest {

    private Car car;

    @Before
    public void setUp() {
        // Configura um novo objeto Car para cada teste, com uma posição inicial e uma cor definida
        car = new Car("CarroTeste", 100, 200, 0xFF0000FF, null);
    }

    @Test
    public void testCarMovement_updatesPosition() {
        // Armazena a posição inicial do carro
        float initialX = car.getX();
        float initialY = car.getY();

        // Chama o método move para simular o movimento por 0.1 segundos
        car.move(0.1);

        // Verifica se as coordenadas X e Y do carro mudaram após o movimento
        assertTrue("A posição X deve ser diferente da inicial após o movimento",
                car.getX() != initialX);
        assertTrue("A posição Y deve ser diferente da inicial após o movimento",
                car.getY() != initialY);
    }
}
