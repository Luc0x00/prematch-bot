package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.*;

public class Superbet {

    public String getLiveMatchesContent() throws Throwable {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://production-superbet-offer-ro.freetls.fastly.net/v2/ro-RO/events/by-date?currentStatus=active&offerState=live&startDate=2024-12-22+00:00:00")
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return client.newCall(request).execute().body().string();
    }

    public ArrayList<Integer> getLiveMatchesId(String response) {
        ArrayList<Integer> matchesIdList = new ArrayList<>();
        JsonElement jsonElement = JsonParser.parseString(response);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray data = jsonObject.has("data") && jsonObject.get("data").isJsonArray()
                    ? jsonObject.getAsJsonArray("data")
                    : null;
            if (data != null) {
                for (JsonElement match : data) {
                    if (match != null && match.isJsonObject()) {
                        JsonObject matchObject = match.getAsJsonObject();
                        if (matchObject.has("eventId") && !matchObject.get("eventId").isJsonNull()) {
                            matchesIdList.add(matchObject.get("eventId").getAsInt());
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
                .url("https://production-superbet-offer-ro.freetls.fastly.net/v2/ro-RO/events/" + matchId)
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return client.newCall(request).execute().body().string();
    }

    public String getMatchName(String response) {
        JsonElement jsonElement = JsonParser.parseString(response);
        if (jsonElement != null && jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray data = jsonObject.has("data") && jsonObject.get("data").isJsonArray()
                    ? jsonObject.getAsJsonArray("data")
                    : null;
            if (data != null) {
                for (JsonElement matchElement : data) {
                    if (matchElement != null && matchElement.isJsonObject()) {
                        JsonObject matchObject = matchElement.getAsJsonObject();
                        if (matchObject.has("matchName") && !matchObject.get("matchName").isJsonNull()) {
                            return matchObject.get("matchName").getAsString();
                        }
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
            JsonArray data = jsonObject.has("data") && jsonObject.get("data").isJsonArray()
                    ? jsonObject.getAsJsonArray("data")
                    : null;
            if (data != null) {
                for (JsonElement matchElement : data) {
                    if (matchElement != null && matchElement.isJsonObject()) {
                        JsonObject matchObject = matchElement.getAsJsonObject();
                        JsonArray odds = matchObject.has("odds") && matchObject.get("odds").isJsonArray()
                                ? matchObject.getAsJsonArray("odds")
                                : null;
                        if (odds != null) {
                            Map<String, Map<String, String>> marketMap = new HashMap<>();

                            for (JsonElement oddElement : odds) {
                                if (oddElement != null && oddElement.isJsonObject()) {
                                    JsonObject oddObject = oddElement.getAsJsonObject();
                                    String marketName = oddObject.has("marketName") && !oddObject.get("marketName").isJsonNull()
                                            ? oddObject.get("marketName").getAsString()
                                            : "Unknown Market";
                                    String name = oddObject.has("name") && !oddObject.get("name").isJsonNull()
                                            ? oddObject.get("name").getAsString()
                                            : "Unknown Name";
                                    String price = oddObject.has("price") && !oddObject.get("price").isJsonNull()
                                            ? oddObject.get("price").getAsString()
                                            : "Unknown Price";

                                    if (name.contains("Sub") || name.contains("Peste")) {
                                        marketMap.putIfAbsent(marketName, new HashMap<>());
                                        marketMap.get(marketName).put(name, price);
                                    }
                                }
                            }

                            for (Map.Entry<String, Map<String, String>> marketEntry : marketMap.entrySet()) {
                                String marketName = marketEntry.getKey();
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
                                        System.out.println("Market: " + marketName);
                                        matchedBets.forEach(System.out::println);
                                        System.out.print("\n");
                                    }
                                }
                            }
                        } else {
                            System.out.println("No odds available for match.");
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
