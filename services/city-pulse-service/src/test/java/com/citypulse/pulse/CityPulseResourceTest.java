package com.citypulse.pulse;

import com.citypulse.airquality.OpenMeteoAirQualityClient;
import com.citypulse.airquality.OpenMeteoAirQualityResponse;
import com.citypulse.city.PhotonClient;
import com.citypulse.city.PhotonResponse;
import com.citypulse.weather.OpenMeteoClient;
import com.citypulse.weather.OpenMeteoResponse;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;

import jakarta.ws.rs.ProcessingException;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

// Only the true external boundaries (Open-Meteo x2, Photon) are mocked here — the whole
// search -> weather/air-quality -> score chain runs for real, since it's this endpoint's
// entire job (there's no other way to reach it, see CityPulseResource).
@QuarkusTest
class CityPulseResourceTest {

    @InjectMock
    @RestClient
    OpenMeteoClient openMeteoClient;

    @InjectMock
    @RestClient
    OpenMeteoAirQualityClient openMeteoAirQualityClient;

    @InjectMock
    @RestClient
    PhotonClient photonClient;

    private void mockBerlinSearch() {
        Mockito.when(photonClient.search(anyString(), anyInt(), anyString())).thenReturn(new PhotonResponse(List.of(
                new PhotonResponse.Feature(
                        new PhotonResponse.Geometry(List.of(13.405, 52.52)),
                        new PhotonResponse.Properties("Berlin", "Germany", "DE")
                )
        )));
    }

    @Test
    void returnsCompletePulseForTheBestMatch() {
        mockBerlinSearch();
        Mockito.when(openMeteoClient.getCurrentWeather(anyDouble(), anyDouble(), anyString()))
                .thenReturn(new OpenMeteoResponse(new OpenMeteoResponse.Current(21.0, 0, 50, 10.0)));
        Mockito.when(openMeteoAirQualityClient.getCurrentAirQuality(anyDouble(), anyDouble(), anyString()))
                .thenReturn(new OpenMeteoAirQualityResponse(new OpenMeteoAirQualityResponse.Current(5.0, 10.0)));

        given()
                .queryParam("q", "berlin")
        .when()
                .get("/api/v1/pulse")
        .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETE"))
                .body("score", equalTo(100))
                .body("city.name", equalTo("Berlin"))
                .body("city.countryCode", equalTo("DE"))
                .body("weather.condition", equalTo("CLEAR"))
                .body("airQuality.status", equalTo("GOOD"))
                .body("providers.weather", equalTo("AVAILABLE"))
                .body("providers.airQuality", equalTo("AVAILABLE"));
    }

    @Test
    void partialQueryMatchesTheSameWayAFullNameWould() {
        // Photon does prefix/substring matching itself; the resource doesn't care how much
        // of the name was typed — "ber" resolving to Berlin is exactly this test's point.
        mockBerlinSearch();
        Mockito.when(openMeteoClient.getCurrentWeather(anyDouble(), anyDouble(), anyString()))
                .thenReturn(new OpenMeteoResponse(new OpenMeteoResponse.Current(21.0, 0, 50, 10.0)));
        Mockito.when(openMeteoAirQualityClient.getCurrentAirQuality(anyDouble(), anyDouble(), anyString()))
                .thenReturn(new OpenMeteoAirQualityResponse(new OpenMeteoAirQualityResponse.Current(5.0, 10.0)));

        given()
                .queryParam("q", "ber")
        .when()
                .get("/api/v1/pulse")
        .then()
                .statusCode(200)
                .body("city.name", equalTo("Berlin"));
    }

    @Test
    void returnsPartialPulseWhenAirQualityProviderFails() {
        mockBerlinSearch();
        Mockito.when(openMeteoClient.getCurrentWeather(anyDouble(), anyDouble(), anyString()))
                .thenReturn(new OpenMeteoResponse(new OpenMeteoResponse.Current(21.0, 0, 50, 10.0)));
        Mockito.when(openMeteoAirQualityClient.getCurrentAirQuality(anyDouble(), anyDouble(), anyString()))
                .thenThrow(new ProcessingException("connection refused"));

        given()
                .queryParam("q", "berlin")
        .when()
                .get("/api/v1/pulse")
        .then()
                .statusCode(200)
                .body("status", equalTo("PARTIAL"))
                .body("score", equalTo(100))
                .body("airQuality", nullValue())
                .body("providers.airQuality", equalTo("UNAVAILABLE"));
    }

    @Test
    void returnsUnavailablePulseWhenBothProvidersFail() {
        mockBerlinSearch();
        Mockito.when(openMeteoClient.getCurrentWeather(anyDouble(), anyDouble(), anyString()))
                .thenThrow(new ProcessingException("connection refused"));
        Mockito.when(openMeteoAirQualityClient.getCurrentAirQuality(anyDouble(), anyDouble(), anyString()))
                .thenThrow(new ProcessingException("connection refused"));

        given()
                .queryParam("q", "berlin")
        .when()
                .get("/api/v1/pulse")
        .then()
                .statusCode(200)
                .body("status", equalTo("UNAVAILABLE"))
                .body("score", nullValue())
                .body("providers.weather", equalTo("UNAVAILABLE"))
                .body("providers.airQuality", equalTo("UNAVAILABLE"));
    }

    @Test
    void returnsNotFoundWhenNoCityMatches() {
        Mockito.when(photonClient.search(anyString(), anyInt(), anyString()))
                .thenReturn(new PhotonResponse(List.of()));

        given()
                .queryParam("q", "asdkjhasdkjh")
        .when()
                .get("/api/v1/pulse")
        .then()
                .statusCode(404)
                .body("code", equalTo("CITY_NOT_FOUND"));
    }

    @Test
    void rejectsMissingQuery() {
        given()
        .when()
                .get("/api/v1/pulse")
        .then()
                .statusCode(400)
                .body("code", equalTo("INVALID_QUERY"));
    }
}
