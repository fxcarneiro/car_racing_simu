package com.example.myapplication;

import android.content.Context;
import com.example.myapplication.TrackView;
import com.example.myapplication.models.Car;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;

/**
 * #### TrackViewTest
 *   Verifica se o bitmap da pista em `TrackView` não é nulo após a inicialização,
 *   assegurando que a pista é carregada corretamente.
 */


public class TrackViewTest {

    private TrackView trackView;

    // Mock da classe Context, que será passado para o TrackView
    @Mock
    private Context mockContext;

    @Before
    public void setUp() {
        // Inicializa os mocks do Mockito, incluindo o mockContext
        MockitoAnnotations.openMocks(this);

        // Cria uma nova instância de TrackView usando o mockContext e um array vazio de Carros
        trackView = new TrackView(mockContext, new Car[0]);
    }

    @Test
    public void testTrackBitmapIsNotNull() {
        // Verifica se o bitmap da pista foi corretamente inicializado
        assertNotNull("Bitmap da pista não deve ser nulo após inicialização",
                trackView.getTrackBitmap());
    }
}
