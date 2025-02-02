package org.example.sites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.*;

public class SuperbetController implements BettingSite {

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

    @Override
    public String getMatchContent(Integer matchId) throws Throwable {
        return executeGetRequest(BASE_URL + "v2/ro-RO/events/" + matchId);
    }

    @Override
    public String getAllMatchesContent() throws Throwable {
        return executeGetRequest(BASE_URL + "v2/ro-RO/events/by-date?offerState=prematch&startDate=2025-02-01+14:00:00&endDate=2025-02-03+14:00:00");
    }

    @Override
    public Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> getMatchesInformation(String response) {
        JsonArray data = extractJsonArray(response);

        if (data == null) {
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

                    if (!matchName.contains("Maccabi") && !matchName.contains("Hapoel")) {
                        result.computeIfAbsent(sportId, k -> new ArrayList<>())
                                .add(new AbstractMap.SimpleEntry<>(matchName, eventId));
                    }
                }
            }
        }
        return result;
    }

    public Map<String, List<AbstractMap.SimpleEntry<String, Double>>> getMatchMarkets(String response) {
        JsonArray data = extractJsonArray(response);

        if (data == null) {
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
                        }
                    }
                }
            }
        }
        return marketMap;
    }

    @Override
    public String getSiteName() {
        return "Superbet";
    }

    @Override
    public Integer getFootballId() {
        return 5;
    }

    @Override
    public Integer getBasketballId() {
        return 4;
    }

    @Override
    public Integer getTennisId() {
        return 2;
    }

    @Override
    public String getTotalSuturiPePoarta() {
        return "Total șuturi pe poartă";
    }

    @Override
    public String getTotalSuturiPePoartaEchipa() {
        return "Șuturi pe poartă %s";
    }

    @Override
    public String getTotalSuturi() {
        return "Total șuturi";
    }

    @Override
    public String getTotalSuturiEchipa() {
        return "Total șuturi %s";
    }

    @Override
    public String getTotalCartonase() {
        return "Total cartonașe";
    }

    @Override
    public String getTotalCartonaseEchipa() {
        return "Total cartonașe %s";
    }

    @Override
    public String getTotalCornere() {
        return "Total cornere";
    }

    @Override
    public String getTotalCornereEchipa() {
        return "Total cornere %s";
    }

    @Override
    public String getPrimaReprizaTotalCornere() {
        return "Prima repriză - Total cornere";
    }

    @Override
    public String getPrimaReprizaTotalCornereEchipa() {
        return "Prima repriză - Total cornere %s";
    }

    @Override
    public String getTotalGoluri() {
        return "Total goluri";
    }

    @Override
    public String getTotalGoluriEchipa() {
        return "Total goluri %s";
    }

    @Override
    public String getPrimaReprizaTotalGoluri() {
        return "Prima repriză - Total goluri";
    }

    @Override
    public String getPrimaReprizaTotalGoluriEchipa() {
        return "Prima repriză - Total goluri %s";
    }

    @Override
    public String getADouaReprizaTotalGoluriEchipa() {
        return "A doua repriză - Total goluri";
    }

    @Override
    public String getTotalOfsaiduri() {
        return "Total ofsaiduri";
    }

    @Override
    public String getTotalOfsaiduriEchipa() {
        return "Total ofsaiduri %s";
    }

    @Override
    public String getTotalFaulturi() {
        return "Total faulturi";
    }

    @Override
    public String getTotalFaulturiEchipa() {
        return "Total faulturi %s";
    }

    @Override
    public String getTotalGameuri() {
        return "Total game-uri";
    }

    @Override
    public String getTotalGameuriEchipa() {
        return "Total game-uri - %s";
    }

    @Override
    public String getTotalSeturi() {
        return "Total seturi";
    }

    @Override
    public String getSet1TotalGameuri() {
        return "Set 1. - Total game-uri";
    }

    @Override
    public String getTotalPuncte() {
        return "Total puncte (incl. prelungiri)";
    }

    @Override
    public String getTotalPuncteEchipa() {
        return "Total puncte %s (incl. prelungiri)";
    }

    @Override
    public String getSplitter() {
        return "·";
    }

    private JsonArray extractJsonArray(String jsonResponse) {
        JsonElement jsonElement = JsonParser.parseString(jsonResponse);
        if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("data")) {
            return jsonElement.getAsJsonObject().getAsJsonArray("data");
        }
        return null;
    }

    private JsonArray extractJsonArray(JsonObject jsonObject) {
        if (jsonObject.has("odds") && jsonObject.get("odds").isJsonArray()) {
            return jsonObject.getAsJsonArray("odds");
        }
        return null;
    }
}
