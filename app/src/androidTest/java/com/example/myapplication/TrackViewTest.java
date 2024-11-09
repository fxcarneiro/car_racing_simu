package com.example.myapplication;

import android.content.Context;
import com.example.myapplication.TrackView;
import com.example.myapplication.models.Car;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;

public class TrackViewTest {

    private TrackView trackView;

    @Mock
    private Context mockContext;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        trackView = new TrackView(mockContext, new Car[0]);
    }

    @Test
    public void testTrackBitmapIsNotNull() {
        assertNotNull("Bitmap da pista não deve ser nulo após inicialização",
                trackView.getTrackBitmap());
    }
}
