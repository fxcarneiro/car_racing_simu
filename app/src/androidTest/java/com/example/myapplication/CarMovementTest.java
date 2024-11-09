package com.example.myapplication;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import com.example.myapplication.models.Car;

public class CarMovementTest {

    private Car car;

    @Before
    public void setUp() {
        car = new Car("CarroTeste", 100, 200, 0xFF0000FF, null);
    }

    @Test
    public void testCarMovement_updatesPosition() {
        float initialX = car.getX();
        float initialY = car.getY();
        car.move(0.1); // Simula 0.1 segundo de movimento

        assertTrue("A posição X deve ser diferente da inicial após o movimento",
                car.getX() != initialX);
        assertTrue("A posição Y deve ser diferente da inicial após o movimento",
                car.getY() != initialY);
    }
}

