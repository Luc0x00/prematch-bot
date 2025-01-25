package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.*;

public class UnibetController {

    private static final String BASE_URL = "https://eu1.offering-api.kambicdn.com/offering/v2018/ubro/";
    private final OkHttpClient client;

    public UnibetController() {
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

    public String getLiveMatchesContent() throws Throwable {
        String url = BASE_URL + "listView/all/all/all/all/in-play.json?lang=ro_RO&market=RO&client_id=2&channel_id=1&ncid=1734916013028&useCombined=true";
        return executeGetRequest(url);
    }

    public String getAllMatchesContent() throws Throwable {
        String url = BASE_URL + "listView/all/all/all/all/starting-within.json?lang=ro_RO&market=RO&client_id=2&channel_id=1&ncid=1735514731370&useCombined=true&from=20250125T042531%2B0200&to=20250127T042531%2B0200";
        return executeGetRequest(url);
    }

    public String getMatchContent(Integer matchId) throws Throwable {
        String url = BASE_URL + "betoffer/event/" + matchId + ".json?lang=ro_RO&market=RO&ncid=1734917551";
        return executeGetRequest(url);
    }

    public Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> getMatchesInformation(String response) {
        Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMap = new HashMap<>();

        JsonElement jsonElement = JsonParser.parseString(response);
        if (!jsonElement.isJsonObject()) {
            System.out.println("Invalid JSON response.");
            return matchesMap;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray events = extractEvents(jsonObject);
        if (events == null) {
            System.out.println("No matches available.");
            return matchesMap;
        }

        for (JsonElement matchElement : events) {
            if (isInvalidJsonObject(matchElement)) {
                System.out.println("Invalid JSON response.");
                return matchesMap;
            }

            JsonObject matchObject = matchElement.getAsJsonObject();
            JsonObject eventObject = extractEvent(matchObject);
            if (eventObject != null && eventObject.has("id") && !eventObject.get("id").isJsonNull()) {
                int eventId = eventObject.get("id").getAsInt();
                String name = eventObject.get("name").getAsString();
                JsonArray pathArray = eventObject.getAsJsonArray("path");
                if (pathArray != null && !pathArray.isEmpty()) {
                    JsonObject groupObject = pathArray.get(0).getAsJsonObject();

                    int sportId = groupObject.has("id") ? groupObject.get("id").getAsInt() : -1;

                    AbstractMap.SimpleEntry<String, Integer> matchInfo = new AbstractMap.SimpleEntry<>(name, eventId);
                    matchesMap.putIfAbsent(sportId, new ArrayList<>());
                    matchesMap.get(sportId).add(matchInfo);
                }
            }
        }
        return matchesMap;
    }

    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        Map<String, Map<String, String>> marketMap = new HashMap<>();

        JsonElement jsonElement = JsonParser.parseString(response);
        if (isInvalidJsonObject(jsonElement)) {
            System.out.println("Invalid JSON response.");
            return marketMap;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray events = extractEvents(jsonObject);
        if (events == null) {
            System.out.println("No match data available.");
            return marketMap;
        }

        JsonArray betOffers = extractBetOffers(jsonObject);
        if (betOffers == null) {
            System.out.println("No bets available.");
            return marketMap;
        }

        for (JsonElement eventElement : events) {
            if (isInvalidJsonObject(eventElement)) {
                System.out.println("Invalid JSON response.");
                return marketMap;
            }

            JsonObject eventObject = eventElement.getAsJsonObject();
            for (JsonElement betOfferElement : betOffers) {
                if (isInvalidJsonObject(betOfferElement)) {
                    System.out.println("Invalid JSON response.");
                    return marketMap;
                }

                JsonObject betOfferObject = betOfferElement.getAsJsonObject();
                int eventId = extractEventId(betOfferObject);
                if (eventId == eventObject.get("id").getAsInt()) {
                    String marketName = extractMarketName(betOfferObject);

                    JsonArray outcomes = extractOutcomes(betOfferObject);
                    if (outcomes == null) {
                        System.out.println("Invalid outcomes.");
                        return marketMap;
                    }

                    for (JsonElement outcomeElement : outcomes) {
                        if (isInvalidJsonObject(betOfferElement)) {
                            System.out.println("Invalid JSON response.");
                            return marketMap;
                        }

                        JsonObject outcomeObject = outcomeElement.getAsJsonObject();

                        String betLabel = extractLabel(outcomeObject);
                        double betLine = extractLine(outcomeObject);
                        String betName;
                        if (betLine != 0.0) {
                            betName = betLabel + " " + betLine;
                        } else {
                            betName = betLabel;
                        }

                        double price = extractOdds(outcomeObject);

                        if (betName.contains("Sub") || betName.contains("Peste")) {
                            marketMap.putIfAbsent(marketName, new HashMap<>());
                            marketMap.get(marketName).put(betName, String.valueOf(price));
                        }
                    }
                }
            }
        }
        return marketMap;
    }

    private boolean isInvalidJsonObject(JsonElement jsonElement) {
        return jsonElement == null || !jsonElement.isJsonObject();
    }

    private JsonArray extractEvents(JsonObject jsonObject) {
        return jsonObject.has("events") && jsonObject.get("events").isJsonArray()
                ? jsonObject.getAsJsonArray("events")
                : null;
    }

    private JsonObject extractEvent(JsonObject matchObject) {
        return matchObject.has("event") && matchObject.get("event").isJsonObject()
                ? matchObject.getAsJsonObject("event")
                : null;
    }

    private int extractEventId(JsonObject betOfferObject) {
        return betOfferObject.has("eventId") && !betOfferObject.get("eventId").isJsonNull()
                ? betOfferObject.get("eventId").getAsInt()
                : -1;
    }

    private JsonArray extractBetOffers(JsonObject jsonObject) {
        return jsonObject.has("betOffers") && jsonObject.get("betOffers").isJsonArray()
                ? jsonObject.getAsJsonArray("betOffers")
                : null;
    }

    private String extractMarketName(JsonObject betOfferObject) {
        return betOfferObject.has("criterion") &&
                betOfferObject.get("criterion").isJsonObject() &&
                betOfferObject.getAsJsonObject("criterion").has("label")
                ? betOfferObject.getAsJsonObject("criterion").get("label").getAsString()
                : "Unknown Market";
    }

    private JsonArray extractOutcomes(JsonObject betOfferObject) {
        return betOfferObject.has("outcomes") && betOfferObject.get("outcomes").isJsonArray()
                ? betOfferObject.getAsJsonArray("outcomes")
                : null;
    }

    private String extractLabel(JsonObject outcomeObject) {
        return outcomeObject.has("label") && !outcomeObject.get("label").isJsonNull()
                ? outcomeObject.get("label").getAsString()
                : "Unknown Bet Name";
    }

    private double extractLine(JsonObject outcomeObject) {
        return outcomeObject.has("line") && !outcomeObject.get("line").isJsonNull() ?
                outcomeObject.get("line").getAsDouble() / 1000.0 : 0.0;
    }

    private double extractOdds(JsonObject outcomeObject) {
        return outcomeObject.has("odds") && !outcomeObject.get("odds").isJsonNull()
                ? outcomeObject.get("odds").getAsDouble() / 1000.0
                : 0.0;
    }
}
