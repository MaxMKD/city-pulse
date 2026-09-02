package com.citypulse.pulse.score;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScoreEngineTest {

    private final ScoreEngine scoreEngine = new ScoreEngine();

    @Test
    void idealWeatherAndGoodAirProduceMaximumScore() {
        int weatherScore = scoreEngine.scoreWeather(21.0, "CLEAR");
        int airQualityScore = scoreEngine.scoreAirQuality(10.0);

        assertEquals(100, weatherScore);
        assertEquals(100, airQualityScore);
        assertEquals(100, scoreEngine.overallScore(weatherScore, airQualityScore));
    }

    @Test
    void thunderstormReducesWeatherScore() {
        int clearScore = scoreEngine.scoreWeather(21.0, "CLEAR");
        int thunderstormScore = scoreEngine.scoreWeather(21.0, "THUNDERSTORM");

        assertTrue(thunderstormScore < clearScore);
        assertEquals(65, thunderstormScore);
    }

    @Test
    void temperatureOutsideIdealRangeReducesScore() {
        int idealScore = scoreEngine.scoreWeather(21.0, "CLEAR");
        int coldScore = scoreEngine.scoreWeather(-5.0, "CLEAR");

        assertTrue(coldScore < idealScore);
    }

    @Test
    void poorAirQualityReducesAirQualityScore() {
        int goodScore = scoreEngine.scoreAirQuality(10.0);
        int poorScore = scoreEngine.scoreAirQuality(150.0);

        assertTrue(poorScore < goodScore);
        assertEquals(20, poorScore);
    }

    @Test
    void weatherScoreIsClampedToZero() {
        int score = scoreEngine.scoreWeather(-60.0, "THUNDERSTORM");

        assertEquals(0, score);
    }

    @Test
    void overallScoreWeightsWeatherAt60PercentAndAirQualityAt40Percent() {
        int overallScore = scoreEngine.overallScore(100, 0);

        assertEquals(60, overallScore);
    }
}
