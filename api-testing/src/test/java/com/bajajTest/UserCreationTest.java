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
        String payload = createPayload("Test", "User", 9999999990L, "valid.email@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }

    @Test
    public void testDuplicatePhoneNumber() {
        String payload = createPayload("Test", "User", 9999999999L, "unique.email@test.com");
        createUser("1", payload); // Create user once
        Response response = createUser("2", payload); // Attempt to create duplicate
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("phoneNumber has already been used"));
    }

    @Test
    public void testDuplicateEmailId() {
        String payload = createPayload("Test", "User", 9999999990L, "duplicate.email@test.com");
        createUser("1", payload); // Create user once
        Response response = createUser("2", payload); // Attempt to create user with same email
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("emailId has already been used"));
    }

    @Test
    public void testMissingRollNumber() {
        String payload = createPayload("Test", "User", 9999999990L, "test.missingroll@test.com");
        Response response = given()
                .header("Content-Type", "application/json")
                .body(payload)
                .post("/automation-campus/create/user");
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Unauthorized"));
    }

    @Test
    public void testInvalidEmailFormat() {
        String payload = createPayload("Test", "User", 9999999991L, "invalidemail.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("emailId is not in the proper format"));
    }

    @Test
    public void testMissingFirstName() {
        String payload = createPayload("", "User", 9999999991L, "missing.first@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("firstName is required"));
    }

    @Test
    public void testMissingLastName() {
        String payload = createPayload("Test", "", 9999999992L, "missing.last@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("lastName is required"));
    }

    @Test
    public void testPhoneNumberTooShort() {
        String payload = createPayload("Test", "User", 123456789L, "short.phone@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("phoneNumber must be 10 digits"));
    }

    @Test
    public void testPhoneNumberTooLong() {
        String payload = createPayload("Test", "User", 123456789012L, "long.phone@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("phoneNumber must be 10 digits"));
    }

    @Test
    public void testInvalidPhoneNumber() {
        String payload = createPayload("Test", "User", 999999999L, "invalid.phone@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Invalid phone number"));
    }

    @Test
    public void testEmailTooLong() {
        String payload = createPayload("Test", "User", 9999999992L, "a".repeat(255) + "@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("emailId is too long"));
    }

    @Test
    public void testValidUserCreationWithBoundaryValues() {
        String payload = createPayload("A", "B", 1000000000L, "boundary.email@test.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }

    @Test
    public void testInvalidPhoneNumberFormat() {
        String payload = createPayload("Test", "User", 12345678L, "invalid.phone.format@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Invalid phone number format"));
    }

    @Test
    public void testEmptyPayload() {
        String payload = "{}";
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Required fields missing"));
    }

    @Test
    public void testInvalidRollNumber() {
        String payload = createPayload("Test", "User", 9999999993L, "invalid.rollnumber@test.com");
        Response response = createUser("invalid", payload);
        assertEquals(401, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Unauthorized"));
    }

    @Test
    public void testSpecialCharactersInFirstName() {
        String payload = createPayload("Te$t", "User", 9999999994L, "special.char.first@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Invalid characters in first name"));
    }

    @Test
    public void testSpecialCharactersInLastName() {
        String payload = createPayload("Test", "Us@r", 9999999995L, "special.char.last@test.com");
        Response response = createUser("1", payload);
        assertEquals(400, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("Invalid characters in last name"));
    }

    @Test
    public void testValidUserCreationWithSpecialCharacters() {
        String payload = createPayload("T3st", "Us3r", 9999999996L, "special@user.com");
        Response response = createUser("1", payload);
        assertEquals(201, response.getStatusCode());
        assertTrue(response.getBody().jsonPath().getString("message").contains("User created successfully"));
    }
}
