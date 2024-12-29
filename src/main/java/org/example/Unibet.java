package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.*;

public class Unibet {

    public String getLiveMatchesContent() throws Throwable {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://eu1.offering-api.kambicdn.com/offering/v2018/ubro/listView/all/all/all/all/in-play.json?lang=ro_RO&market=RO&client_id=2&channel_id=1&ncid=1734916013028&useCombined=true")
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return client.newCall(request).execute().body().string();
    }

    public ArrayList<Integer> getLiveMatchesId(String response) {
        ArrayList<Integer> matchesIdList = new ArrayList<>();
        JsonElement jsonElement = JsonParser.parseString(response);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray events = jsonObject.has("events") && jsonObject.get("events").isJsonArray()
                    ? jsonObject.getAsJsonArray("events")
                    : null;
            if (events != null) {
                for (JsonElement matchElement : events) {
                    if (matchElement != null && matchElement.isJsonObject()) {
                        JsonObject matchObject = matchElement.getAsJsonObject();
                        JsonObject eventObject = matchObject.has("event") && matchObject.get("event").isJsonObject()
                                ? matchObject.getAsJsonObject("event")
                                : null;
                        if (eventObject != null && eventObject.has("id") && !eventObject.get("id").isJsonNull()) {
                            int eventId = eventObject.get("id").getAsInt();
                            matchesIdList.add(eventId);
                        }
                    }
                }
            } else {
                System.out.println("No live matches available.");
            }
        } else {
            System.out.println("Invalid JSON response.");
        }
        return matchesIdList;
    }

    public String getLiveMatchContent(Integer matchId) throws Throwable {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://eu-offering-api.kambicdn.com/offering/v2018/ubro/betoffer/event/" + matchId + ".json?lang=ro_RO&market=RO&ncid=1734917551")
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return client.newCall(request).execute().body().string();
    }

    public String getMatchName(String response) {
        JsonElement jsonElement = JsonParser.parseString(response);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray events = jsonObject.has("events") && jsonObject.get("events").isJsonArray()
                    ? jsonObject.getAsJsonArray("events")
                    : null;
            if (events != null) {
                for (JsonElement eventElement : events) {
                    if (eventElement != null && eventElement.isJsonObject()) {
                        JsonObject eventObject = eventElement.getAsJsonObject();
                        return eventObject.has("name") && !eventObject.get("name").isJsonNull()
                                ? eventObject.get("name").getAsString()
                                : "Unknown Match Name";
                    }
                }
            } else {
                System.out.println("No match data available.");
            }
        } else {
            System.out.println("Invalid JSON response.");
        }
        return "Unknown Match Name";
    }

    public void getMatchMarkets(String response) {
        JsonElement jsonElement = JsonParser.parseString(response);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray events = jsonObject.has("events") && jsonObject.get("events").isJsonArray()
                    ? jsonObject.getAsJsonArray("events")
                    : null;
            JsonArray betOffers = jsonObject.has("betOffers") && jsonObject.get("betOffers").isJsonArray()
                    ? jsonObject.getAsJsonArray("betOffers")
                    : null;
            if (events != null) {
                for (JsonElement eventElement : events) {
                    if (eventElement != null && eventElement.isJsonObject()) {
                        JsonObject eventObject = eventElement.getAsJsonObject();
                        if (betOffers != null) {
                            for (JsonElement betOfferElement : betOffers) {
                                if (betOfferElement != null && betOfferElement.isJsonObject()) {
                                    JsonObject betOfferObject = betOfferElement.getAsJsonObject();
                                    int eventId = betOfferObject.has("eventId") && !betOfferObject.get("eventId").isJsonNull()
                                            ? betOfferObject.get("eventId").getAsInt()
                                            : -1;
                                    if (eventId == eventObject.get("id").getAsInt()) {
                                        String marketName = betOfferObject.has("criterion") &&
                                                betOfferObject.get("criterion").isJsonObject() &&
                                                betOfferObject.getAsJsonObject("criterion").has("label")
                                                ? betOfferObject.getAsJsonObject("criterion").get("label").getAsString()
                                                : "Unknown Market";
                                        JsonArray outcomes = betOfferObject.has("outcomes") && betOfferObject.get("outcomes").isJsonArray()
                                                ? betOfferObject.getAsJsonArray("outcomes")
                                                : null;
                                        if (outcomes != null) {

                                            Map<String, Map<String, String>> marketMap = new HashMap<>();

                                            for (JsonElement outcomeElement : outcomes) {
                                                if (outcomeElement != null && outcomeElement.isJsonObject()) {
                                                    JsonObject outcomeObject = outcomeElement.getAsJsonObject();
                                                    String betName = outcomeObject.has("label") && !outcomeObject.get("label").isJsonNull()
                                                            ? outcomeObject.get("label").getAsString()
                                                            : "Unknown Bet Name";

                                                    double betLine = outcomeObject.has("line") && !outcomeObject.get("line").isJsonNull() ?
                                                            outcomeObject.get("line").getAsDouble() / 1000.0 : 0.0;

                                                    String betNameWithLine = betName + " " + betLine;

                                                    double price = outcomeObject.has("odds") && !outcomeObject.get("odds").isJsonNull()
                                                            ? outcomeObject.get("odds").getAsDouble() / 1000.0
                                                            : 0.0;

                                                    if (betName.contains("Sub") || betName.contains("Peste")) {
                                                        marketMap.putIfAbsent(marketName, new HashMap<>());
                                                        marketMap.get(marketName).put(betNameWithLine, String.valueOf(price));
                                                    }
                                                }
                                            }

                                            for (Map.Entry<String, Map<String, String>> marketEntry : marketMap.entrySet()) {
                                                String market = marketEntry.getKey();
                                                Map<String, String> bets = marketEntry.getValue();

                                                Map<String, List<String>> valueGroup = new HashMap<>();
                                                for (Map.Entry<String, String> betEntry : bets.entrySet()) {
                                                    String betName = betEntry.getKey();
                                                    String price = betEntry.getValue();
                                                    String value = betName.split(" ")[1];

                                                    valueGroup.putIfAbsent(value, new ArrayList<>());
                                                    valueGroup.get(value).add(betName + " - Odds: " + price);
                                                }

                                                for (Map.Entry<String, List<String>> valueEntry : valueGroup.entrySet()) {
                                                    List<String> matchedBets = valueEntry.getValue();
                                                    if (matchedBets.size() == 2) {
                                                        System.out.println("Market: " + market);
                                                        matchedBets.forEach(System.out::println);
                                                        System.out.print("\n");
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                System.out.println("No match data available.");
            }
        } else {
            System.out.println("Invalid JSON response.");
        }
    }
}
