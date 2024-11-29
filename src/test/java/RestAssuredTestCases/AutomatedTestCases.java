package RestAssuredTestCases;

import io.restassured.response.Response;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AutomatedTestCases {

    // Base URL for OpenWeather API and API Key
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String API_KEY = "64e55d7c1fac880798953da396288602";

    @Test
    public void testWeatherApiResponse() {
        // Send GET request to the weather endpoint for London and capture the response
        Response response = given()
            .param("q", "London")
            .param("appid", API_KEY)
            .when()
            .get(BASE_URL)
            .then()
            .statusCode(200)  // Verify that the status code is 200 OK
            .time(lessThan(10000L))  // Verify that the response time is less than 10 seconds
            .extract().response();

        // Print response body for debugging
        System.out.println(response.body().asString());

        // Validate response body against JSON schema
        validateJsonSchema(response);

        // Validate specific fields in the response body
        validateResponseData(response);
    }

    private void validateJsonSchema(Response response) {
        // Define the expected JSON schema for the weather API response
        String schema = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"coord\": {\"type\": \"object\", \"properties\": {\"lon\": {\"type\": \"number\"}, \"lat\": {\"type\": \"number\"}}},\n" +
                "    \"weather\": {\"type\": \"array\", \"items\": {\"type\": \"object\", \"properties\": {\"id\": {\"type\": \"integer\"}, \"main\": {\"type\": \"string\"}, \"description\": {\"type\": \"string\"}}}},\n" +
                "    \"name\": {\"type\": \"string\"},\n" +
                "    \"cod\": {\"type\": \"integer\"}\n" +
                "  },\n" +
                "  \"required\": [\"coord\", \"weather\", \"name\", \"cod\"]\n" +
                "}";

        // Use JsonSchemaValidator to validate the response against the schema
        response.then().body(JsonSchemaValidator.matchesJsonSchema(schema));
    }

    private void validateResponseData(Response response) {
        // Validate city name, weather, and coordinates in the response
        String cityName = response.jsonPath().getString("name");
        int cod = response.jsonPath().getInt("cod");

        // Assert city name is London and cod is 200 
        assertThat(cityName, equalTo("London"));
        assertThat(cod, equalTo(200));
        
        //Print city name and cod to get values
        System.out.printf("City Name = %s\n", cityName);  
        System.out.printf("cod = %d\n", cod);

      
        // Assert valid latitude and longitude values exist
        Float latitude = response.jsonPath().getFloat("coord.lat");
        Float longitude = response.jsonPath().getFloat("coord.lon");
        assertThat(latitude, notNullValue());
        assertThat(longitude, notNullValue());

        // Print latitude and longitude for debugging
        System.out.println("Latitude: " + latitude);
        System.out.println("Longitude: " + longitude);

        // Validate the weather array is not empty and contains relevant fields
        String weatherMain = response.jsonPath().getString("weather[0].main");
        String weatherDescription = response.jsonPath().getString("weather[0].description");

        // Assert that weather data is present
        assertThat(weatherMain, notNullValue());
        assertThat(weatherDescription, notNullValue());

        // Print weather info for debugging
        System.out.println("Weather: " + weatherMain + " - " + weatherDescription);
    }
}
