package com.citypulse.pulse;

import com.citypulse.airquality.AirQuality;
import com.citypulse.airquality.AirQualityProviderException;
import com.citypulse.airquality.AirQualityService;
import com.citypulse.city.City;
import com.citypulse.city.CityNotFoundException;
import com.citypulse.city.CityService;
import com.citypulse.pulse.score.ScoreEngine;
import com.citypulse.weather.Weather;
import com.citypulse.weather.WeatherProviderException;
import com.citypulse.weather.WeatherService;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

import org.jboss.logging.Logger;

@ApplicationScoped
public class CityPulseService {

    private static final Logger LOG = Logger.getLogger(CityPulseService.class);

    private final CityService cityService;
    private final WeatherService weatherService;
    private final AirQualityService airQualityService;
    private final ScoreEngine scoreEngine;

    public CityPulseService(
            CityService cityService,
            WeatherService weatherService,
            AirQualityService airQualityService,
            ScoreEngine scoreEngine
    ) {
        this.cityService = cityService;
        this.weatherService = weatherService;
        this.airQualityService = airQualityService;
        this.scoreEngine = scoreEngine;
    }

    // The one public entry point: search for a city by text (Photon ranks results, so
    // "best match" is simply the first one) and return the pulse for it in one call.
    public CityPulseResponse getPulseForQuery(String query) {
        City city = cityService.searchBestMatch(query)
                .orElseThrow(() -> new CityNotFoundException("No city found for query: " + query));

        CityInfo cityInfo = new CityInfo(city.name(), city.country(), city.countryCode());
        Optional<Weather> weather = fetchWeather(city.latitude(), city.longitude());
        Optional<AirQuality> airQuality = fetchAirQuality(city.latitude(), city.longitude());

        return buildResponse(cityInfo, weather, airQuality);
    }

    private Optional<Weather> fetchWeather(double latitude, double longitude) {
        try {
            return Optional.of(weatherService.getWeather(latitude, longitude));
        } catch (WeatherProviderException e) {
            LOG.warn("weather lookup failed, continuing with partial result", e);
            return Optional.empty();
        }
    }

    private Optional<AirQuality> fetchAirQuality(double latitude, double longitude) {
        try {
            return Optional.of(airQualityService.getAirQuality(latitude, longitude));
        } catch (AirQualityProviderException e) {
            LOG.warn("air quality lookup failed, continuing with partial result", e);
            return Optional.empty();
        }
    }

    private CityPulseResponse buildResponse(CityInfo cityInfo, Optional<Weather> weather, Optional<AirQuality> airQuality) {
        Integer weatherScore = weather.map(w -> scoreEngine.scoreWeather(w.temperatureCelsius(), w.condition())).orElse(null);
        Integer airQualityScore = airQuality.map(a -> scoreEngine.scoreAirQuality(a.europeanAqi())).orElse(null);

        String status;
        Integer score;
        if (weatherScore != null && airQualityScore != null) {
            status = "COMPLETE";
            score = scoreEngine.overallScore(weatherScore, airQualityScore);
        } else if (weatherScore != null) {
            status = "PARTIAL";
            score = weatherScore;
        } else if (airQualityScore != null) {
            status = "PARTIAL";
            score = airQualityScore;
        } else {
            status = "UNAVAILABLE";
            score = null;
        }

        WeatherSummary weatherSummary = weather.map(w -> new WeatherSummary(w.temperatureCelsius(), w.condition())).orElse(null);
        AirQualitySummary airQualitySummary = airQuality.map(a -> new AirQualitySummary(a.pm25(), a.status())).orElse(null);
        ScoreBreakdown scoreBreakdown = new ScoreBreakdown(weatherScore, airQualityScore);
        ProviderStatus providers = new ProviderStatus(
                weather.isPresent() ? "AVAILABLE" : "UNAVAILABLE",
                airQuality.isPresent() ? "AVAILABLE" : "UNAVAILABLE"
        );

        return new CityPulseResponse(cityInfo, score, status, weatherSummary, airQualitySummary, scoreBreakdown, providers);
    }
}
