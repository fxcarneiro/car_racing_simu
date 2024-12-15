package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.myapplication.models.Car;
import com.example.mylibrary2.utils.MetricsCollector;

/**
 * A classe TrackView é responsável por renderizar a pista e os carros na tela.
 */
@SuppressLint("ViewConstructor")
public class TrackView extends View {

    private Car[] cars;                           // Array de carros para desenhar na pista
    private final Paint trackPaint = new Paint(); // Paint para desenhar a pista
    private Bitmap trackBitmap;                   // Bitmap da imagem da pista
    private long lastUpdateTime;                  // Tempo da última atualização da tela
    private static final String TAG = "TrackView";

    private final MetricsCollector metricsCollector; // Coleta de métricas de desempenho

    /**
     * Construtor que inicializa o TrackView e carrega o bitmap da pista.
     *
     * @param context Contexto da aplicação
     * @param cars    Array inicial de carros para desenhar na pista
     */
    public TrackView(Context context, Car[] cars) {
        super(context);
        this.cars = cars;
        this.metricsCollector = new MetricsCollector(context); // Passa o Context ao MetricsCollector
        initializeTrackBitmap();
        lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Carrega o bitmap da pista a partir dos recursos.
     */
    private void initializeTrackBitmap() {
        try {
            trackBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.track);
            if (trackBitmap == null) {
                Log.e(TAG, "Erro ao carregar o bitmap da pista. Verifique o recurso R.drawable.track.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar o bitmap da pista", e);
        }
    }

    /**
     * Método chamado para desenhar a pista e os carros na tela.
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        try {
            super.onDraw(canvas);
            if (trackBitmap == null) {
                Log.e(TAG, "trackBitmap não foi carregado corretamente.");
                return;
            }
            drawTrack(canvas);  // Desenha o fundo da pista
            drawCars(canvas);   // Desenha os carros na pista

            if (isRunning()) {
                postInvalidateOnAnimation();  // Atualiza a tela continuamente
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar a tela", e);
        }
    }

    /**
     * Desenha a imagem da pista no canvas.
     *
     * @param canvas Canvas no qual a pista será desenhada
     */
    private void drawTrack(Canvas canvas) {
        try {
            if (trackBitmap != null) {
                Rect destRect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
                canvas.drawBitmap(trackBitmap, null, destRect, trackPaint);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar a pista", e);
        }
    }

    /**
     * Desenha todos os carros na tela e atualiza suas posições.
     *
     * @param canvas Canvas no qual os carros serão desenhados
     */
    private void drawCars(Canvas canvas) {
        try {
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastUpdateTime) / 1000.0; // Delta time em segundos
            lastUpdateTime = currentTime;

            for (Car car : cars) {
                try {
                    car.move(deltaTime);  // Atualiza a posição do carro
                    car.draw(canvas);     // Desenha o carro

                    // Coleta métricas para cada carro
                    collectCarMetrics(car, deltaTime);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao atualizar ou desenhar carro " + car.getName(), e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desenhar os carros", e);
        }
    }

    /**
     * Coleta métricas de desempenho para o carro atual.
     *
     * @param car       Carro para o qual as métricas serão coletadas.
     * @param deltaTime Tempo decorrido desde a última atualização.
     */
    private void collectCarMetrics(Car car, double deltaTime) {
        try {
            long jitter = (long) (Math.random() * 50); // Simula jitter
            long responseTime = (long) (deltaTime * 1000); // Delta time em ms
            double utilization = Math.random() * 100; // Simula utilização

            metricsCollector.collectMetric(car.getName(), jitter, responseTime, utilization);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao coletar métricas para o carro " + car.getName(), e);
        }
    }

    /**
     * Atualiza a lista de carros para ser desenhada na tela.
     *
     * @param newCars Novo array de carros
     */
    public void updateCars(Car[] newCars) {
        try {
            this.cars = newCars;
            invalidate();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao atualizar a lista de carros", e);
        }
    }

    /**
     * Retorna o bitmap atual da pista.
     *
     * @return Bitmap da pista
     */
    public Bitmap getTrackBitmap() {
        return trackBitmap;
    }

    /**
     * Verifica se a simulação está em execução.
     *
     * @return true se a simulação estiver em execução; caso contrário, false.
     */
    private boolean isRunning() {
        return true; // Retorne o estado real da simulação conforme necessário
    }

    /**
     * Exporta métricas coletadas para um arquivo CSV.
     *
     * @param filePath Caminho do arquivo CSV.
     */
    public void exportMetrics(String filePath) {
        try {
            metricsCollector.exportMetrics(filePath);
            Log.d(TAG, "Métricas exportadas para " + filePath);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar métricas", e);
        }
    }
}
