package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.*;

public class SuperbetController {

    private static final String BASE_URL = "https://production-superbet-offer-ro.freetls.fastly.net/";
    private final OkHttpClient client = new OkHttpClient();

    private String executeGetRequest(String url) throws Throwable {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return client.newCall(request).execute().body().string();
    }

    public String getMatchContent(Integer matchId) throws Throwable {
        return executeGetRequest(BASE_URL + "v2/ro-RO/events/" + matchId);
    }

    public String getLiveMatchesContent() throws Throwable {
        return executeGetRequest(BASE_URL + "v2/ro-RO/events/by-date?currentStatus=active&offerState=live&startDate=2024-12-22+00:00:00");
    }

    public String getAllMatchesContent() throws Throwable {
        return executeGetRequest(BASE_URL + "v2/ro-RO/events/by-date?offerState=prematch&startDate=2025-01-25+04:00:00&endDate=2025-01-27+04:00:00");
    }

    public Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> getMatchesInformation(String response) {
        JsonArray data = extractJsonArray(response);
        if (data == null) {
//            System.out.println("Error: No data found in response.");
            return Collections.emptyMap();
        }

        Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> result = new HashMap<>();
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("eventId") && obj.has("matchName") && obj.has("sportId")) {
                    int sportId = obj.get("sportId").getAsInt();
                    String matchName = obj.get("matchName").getAsString();
                    int eventId = obj.get("eventId").getAsInt();
                    result.computeIfAbsent(sportId, k -> new ArrayList<>())
                            .add(new AbstractMap.SimpleEntry<>(matchName, eventId));
                } else {
//                    System.out.println("Warning: Missing expected fields in match object.");
                }
            } else {
//                System.out.println("Warning: Invalid match element in data array.");
            }
        }
        return result;
    }

    public Map<String, List<AbstractMap.SimpleEntry<String, Double>>> getMatchMarkets(String response) {
        JsonArray data = extractJsonArray(response);
        if (data == null) {
//            System.out.println("Error: No data found in response.");
            return Collections.emptyMap();
        }

        Map<String, List<AbstractMap.SimpleEntry<String, Double>>> marketMap = new HashMap<>();
        for (JsonElement match : data) {
            if (match.isJsonObject()) {
                JsonArray odds = extractJsonArray(match.getAsJsonObject());
                if (odds != null) {
                    for (JsonElement odd : odds) {
                        if (odd.isJsonObject()) {
                            JsonObject oddObj = odd.getAsJsonObject();
                            String marketName = oddObj.has("marketName") ? oddObj.get("marketName").getAsString() : "Unknown Market";
                            String betName = oddObj.has("name") ? oddObj.get("name").getAsString() : "Unknown Name";
                            double betPrice = oddObj.has("price") ? oddObj.get("price").getAsDouble() : 0.0;
                            if (betName.contains("Sub") || betName.contains("Peste")) {
                                marketMap.computeIfAbsent(marketName, k -> new ArrayList<>())
                                        .add(new AbstractMap.SimpleEntry<>(betName, betPrice));
                            }
                        } else {
//                            System.out.println("Warning: Invalid odd element.");
                        }
                    }
                } else {
//                    System.out.println("Warning: No odds found for match.");
                }
            } else {
//                System.out.println("Warning: Invalid match element in data array.");
            }
        }
        return marketMap;
    }

    private JsonArray extractJsonArray(String jsonResponse) {
        JsonElement jsonElement = JsonParser.parseString(jsonResponse);
        if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("data")) {
            return jsonElement.getAsJsonObject().getAsJsonArray("data");
        } else {
//            System.out.println("Error: Key '" + "data" + "' not found in JSON response.");
            return null;
        }
    }

    private JsonArray extractJsonArray(JsonObject jsonObject) {
        if (jsonObject.has("odds") && jsonObject.get("odds").isJsonArray()) {
            return jsonObject.getAsJsonArray("odds");
        } else {
//            System.out.println("Error: Key '" + "odds" + "' not found in JSON object.");
            return null;
        }
    }
}
