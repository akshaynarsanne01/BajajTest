package com.example;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DestinationHashGenerator {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN_Number> <path_to_json_file>");
            return;
        }

        String prnNumber = args[0].toLowerCase().replace(" ", "");
        String jsonFilePath = args[1];

        try {
            String destinationValue = findDestinationValue(jsonFilePath);
            if (destinationValue == null) {
                System.out.println("The key 'destination' was not found in the JSON file.");
                return;
            }
            String randomString = generateRandomString(8);
            String concatenatedString = prnNumber + destinationValue + randomString;
            String md5Hash = generateMD5Hash(concatenatedString);
            System.out.println(md5Hash + ";" + randomString);

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    private static String findDestinationValue(String jsonFilePath) throws Exception {
        JSONParser parser = new JSONParser();

        try (FileReader reader = new FileReader(jsonFilePath)) {
            Object obj = parser.parse(reader);
            return traverseJSON(obj);
        } catch (ParseException e) {
            throw new Exception("Failed to parse JSON file.");
        }
    }
    private static String traverseJSON(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) obj;
            for (Object key : jsonObject.keySet()) {
                String keyStr = (String) key;
                Object value = jsonObject.get(keyStr);

                if (keyStr.equals("destination")) {
                    return value.toString();
                }

                if (value instanceof JSONObject || value instanceof Map) {
                    String foundValue = traverseJSON(value);
                    if (foundValue != null) return foundValue;
                }

                if (value instanceof Iterable) {
                    Iterator<?> iterator = ((Iterable<?>) value).iterator();
                    while (iterator.hasNext()) {
                        String foundValue = traverseJSON(iterator.next());
                        if (foundValue != null) return foundValue;
                    }
                }
            }
        }
        return null;
    }
    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
