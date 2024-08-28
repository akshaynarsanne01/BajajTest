package com.bajajTest;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserCreationTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://bfhldevapigw.healthrx.co.in";
    }

    private String createPayload(String firstName, String lastName, long phoneNumber, String emailId) {
        return String.format("{\"firstName\":\"%s\",\"lastName\":\"%s\",\"phoneNumber\":%d,\"emailId\":\"%s\"}",
                             firstName, lastName, phoneNumber, emailId);
    }

    private Response createUser(String rollNumber, String payload) {
        return given()
                .header("roll-number", rollNumber)
                .header("Content-Type", "application/json")
                .body(payload)
                .post("/automation-campus/create/user");
    }

    @Test
    public void testValidUserCreation() {
        String payload = createPayload("John", "Doe", 9999999990L, "john.doe@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }

    @Test
    public void testValidUserCreationWithBoundaryValues() {
        String payload = createPayload("A", "B", 1000000000L, "boundary.email@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }

    @Test
    public void testDuplicatePhoneNumber() {
        String payload = createPayload("Test", "User", 9999999991L, "unique.email@test.com");
        createUser("1", payload); // Create user once
        Response response = createUser("2", payload); // Attempt to create duplicate
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("phoneNumber has already been used."));
    }

    @Test
    public void testInvalidPhoneNumberFormat() {
        String payload = createPayload("Test", "User", 123456789L, "valid.email@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("phoneNumber has to be in a proper format."));
    }

    @Test
    public void testDuplicateEmailId() {
        String payload = createPayload("Test", "User", 9999999992L, "duplicate.email@test.com");
        createUser("1", payload); // Create user once
        Response response = createUser("2", payload); // Attempt to create user with same email
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("emailId has already been used."));
    }

    @Test
    public void testInvalidEmailFormat() {
        String payload = createPayload("Test", "User", 9999999993L, "invalidemail.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("emailId is not in the proper format."));
    }

    @Test
    public void testValidUserCreationWithSpecialCharacters() {
        String payload = createPayload("John", "Doe", 9999999994L, "john.doe+special@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }

    @Test
    public void testUserCreationWithMinimalFields() {
        String payload = createPayload("A", "B", 1000000000L, "a.b@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }

    @Test
    public void testUserCreationWithLongEmail() {
        String payload = createPayload("Test", "User", 9999999995L, "a".repeat(50) + "@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }
}
