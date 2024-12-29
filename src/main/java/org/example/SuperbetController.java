package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SuperbetController {

    private static final String BASE_URL = "https://production-superbet-offer-ro.freetls.fastly.net/";
    private final OkHttpClient client;

    public SuperbetController() {
        client = new OkHttpClient();
    }

    private String executeGetRequest(String url) throws Throwable {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return client.newCall(request).execute().body().string();
    }

    public String getMatchContent(Integer matchId) throws Throwable {
        String url = BASE_URL + "v2/ro-RO/events/" + matchId;
        return executeGetRequest(url);
    }

    public String getLiveMatchesContent() throws Throwable {
        String url = BASE_URL + "v2/ro-RO/events/by-date?currentStatus=active&offerState=live&startDate=2024-12-22+00:00:00";
        return executeGetRequest(url);
    }

    public String getAllMatchesContent() throws Throwable {
        String url = BASE_URL + "subscription/v2/ro-RO/events/all";
        return executeGetRequest(url);
    }

    public ArrayList<Integer> getMatchesId(String response) {
        ArrayList<Integer> matchesIdList = new ArrayList<>();

        JsonElement jsonElement = JsonParser.parseString(response);
        if (isInvalidJsonObject(jsonElement)) {
            System.out.println("Invalid JSON response.");
            return matchesIdList;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray data = extractData(jsonObject);
        if (data == null) {
            System.out.println("No live matches available.");
            return matchesIdList;
        }

        for (JsonElement match : data) {
            if (match != null && match.isJsonObject()) {
                JsonObject matchObject = match.getAsJsonObject();
                Integer eventId = extractEventId(matchObject);
                if (eventId != null) {
                    matchesIdList.add(eventId);
                }
            }
        }
        return matchesIdList;
    }

    public String getMatchName(String response) {
        JsonElement jsonElement = JsonParser.parseString(response);
        if (isInvalidJsonObject(jsonElement)) {
            return "Invalid JSON response.";
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray data = extractData(jsonObject);
        if (data == null) {
            return "No match data available.";
        }

        for (JsonElement matchElement : data) {
            if (matchElement != null && matchElement.isJsonObject()) {
                JsonObject matchObject = matchElement.getAsJsonObject();
                if (matchObject.has("matchName") && !matchObject.get("matchName").isJsonNull()) {
                    return matchObject.get("matchName").getAsString();
                }
            }
        }
        return "Unknown Match";
    }

    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        Map<String, Map<String, String>> marketMap = new HashMap<>();

        JsonElement jsonElement = JsonParser.parseString(response);
        if (isInvalidJsonObject(jsonElement)) {
            System.out.println("Invalid JSON response.");
            return marketMap;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray data = extractData(jsonObject);
        if (data == null) {
            System.out.println("No match data available.");
            return marketMap;
        }

        for (JsonElement matchElement : data) {
            if (isInvalidJsonObject(matchElement)) {
                System.out.println("Invalid JSON response.");
                return marketMap;
            }

            JsonObject matchObject = matchElement.getAsJsonObject();
            JsonArray odds = extractOdds(matchObject);
            if (odds == null) {
                System.out.println("No odds available for match.");
                return marketMap;
            }

            for (JsonElement oddElement : odds) {
                if (isInvalidJsonObject(oddElement)) {
                    System.out.println("Invalid JSON response.");
                    return marketMap;
                }
                JsonObject oddObject = oddElement.getAsJsonObject();

                if (extractBetName(oddObject).contains("Sub") || extractBetName(oddObject).contains("Peste")) {
                    marketMap.putIfAbsent(extractMarketName(oddObject), new HashMap<>());
                    marketMap.get(extractMarketName(oddObject)).put(extractBetName(oddObject), extractBetPrice(oddObject));
                }
            }
        }
        return marketMap;
    }

    private boolean isInvalidJsonObject(JsonElement jsonElement) {
        return jsonElement == null || !jsonElement.isJsonObject();
    }

    private JsonArray extractData(JsonObject jsonObject) {
        return jsonObject.has("data") && jsonObject.get("data").isJsonArray()
                ? jsonObject.getAsJsonArray("data")
                : null;
    }

    private Integer extractEventId(JsonObject matchObject) {
        if (matchObject.has("eventId") && !matchObject.get("eventId").isJsonNull()) {
            return matchObject.get("eventId").getAsInt();
        }
        return null;
    }

    private JsonArray extractOdds(JsonObject matchObject) {
        return matchObject.has("odds") && matchObject.get("odds").isJsonArray()
                ? matchObject.getAsJsonArray("odds")
                : null;
    }

    private String extractMarketName(JsonObject oddObject) {
        return oddObject.has("marketName") && !oddObject.get("marketName").isJsonNull()
                ? oddObject.get("marketName").getAsString()
                : "Unknown Market";
    }

    private String extractBetName(JsonObject oddObject) {
        return oddObject.has("name") && !oddObject.get("name").isJsonNull()
                ? oddObject.get("name").getAsString()
                : "Unknown Name";
    }

    private String extractBetPrice(JsonObject oddObject) {
        return oddObject.has("price") && !oddObject.get("price").isJsonNull()
                ? oddObject.get("price").getAsString()
                : "Unknown Price";
    }
}
