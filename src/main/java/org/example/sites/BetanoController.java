package org.example.sites;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.*;

public class BetanoController implements BettingSite {

    private static final String BASE_URL = "https://ro.betano.com/api";
    private final OkHttpClient client = new OkHttpClient();

    private String executeGetRequest(String url) {
        try {
            Request request = new Request.Builder()
                    .url("https://ro.betano.com/api")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Referer", "https://ro.betano.com/")
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            try (ResponseBody responseBody = response.body()) {
                return responseBody != null ? responseBody.string() : "";
            }
        } catch (IOException e) {
            System.err.println("Request failed: " + e.getMessage());
            return "";
        }
    }

    public String getMatchContent(Integer matchId) {
        return executeGetRequest(BASE_URL + "/cote/acs-sepsi-farul-constanta/" + matchId + "/?bt=13");
    }

    public String getAllMatchesContent() {
        return executeGetRequest(BASE_URL + "/sport/fotbal/meciurile-urmatoare-de-azi/?sort=Leagues&req=la,s,stnf,c,mb");
    }

    public Map<Integer, List<List<String>>> getMatchesInformation(String response) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return Collections.emptyMap();
            }

            response = response.trim();
            if (!response.startsWith("{") && !response.startsWith("[")) {
                return Collections.emptyMap();
            }

            JsonObject root = JsonParser.parseString(response).getAsJsonObject();
            if (!root.has("data") || !root.getAsJsonObject("data").has("blocks")) {
                return Collections.emptyMap();
            }

            JsonArray blocks = root.getAsJsonObject("data").getAsJsonArray("blocks");
            Map<Integer, List<List<String>>> result = new HashMap<>();

            for (JsonElement blockElement : blocks) {
                JsonObject block = blockElement.getAsJsonObject();
                JsonArray events = block.has("events") ? block.getAsJsonArray("events") : null;

                if (events == null) continue;

                for (JsonElement eventElement : events) {
                    JsonObject event = eventElement.getAsJsonObject();
                    if (event.has("sportId") && event.has("name") && event.has("id")) {

                        // Determină sportId (înlocuim "FOOT" cu 1, altfel 0)
                        String sportCode = event.get("sportId").getAsString();
                        int sportId = sportCode.equals("FOOT") ? 1 : 0;

                        String matchName = event.get("name").getAsString();
                        int eventId = event.get("id").getAsInt();
                        String matchDate = event.has("startDate") ? event.get("startDate").getAsString() : "";

                        List<String> matchInfo = Arrays.asList(matchName, String.valueOf(eventId), matchDate);
                        result.computeIfAbsent(sportId, k -> new ArrayList<>()).add(matchInfo);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        JsonObject eventObject = extractJsonObject(response);

        if (eventObject == null || !eventObject.has("markets")) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, String>> marketMap = new HashMap<>();

        JsonArray markets = eventObject.getAsJsonArray("markets");

        for (JsonElement marketElement : markets) {
            if (marketElement.isJsonObject()) {
                JsonObject marketObj = marketElement.getAsJsonObject();
                String marketName = marketObj.has("name") ? marketObj.get("name").getAsString() : "Unknown Market";

                JsonArray selections = marketObj.getAsJsonArray("selections");
                if (selections != null) {
                    for (JsonElement selectionElement : selections) {
                        if (selectionElement.isJsonObject()) {
                            JsonObject selectionObj = selectionElement.getAsJsonObject();
                            String betName = selectionObj.has("name") ? selectionObj.get("name").getAsString() : "Unknown Name";
                            String betPrice = selectionObj.has("price") ? String.valueOf(selectionObj.get("price").getAsDouble()) : "0.0";

                            String normalizedMarketName = marketName.replace(" (suplimentar)", "").trim();

                            marketMap.computeIfAbsent(normalizedMarketName, k -> new HashMap<>())
                                    .put(betName, betPrice);
                        }
                    }
                }
            }
        }
        return marketMap;
    }

    private static JsonObject extractJsonObject(String response) {
        try {
            JsonElement jsonElement = JsonParser.parseString(response);
            if (jsonElement.isJsonObject()) {
                return jsonElement.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("event");
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getSiteName() {
        return "Betano";
    }

    @Override
    public Integer getFootballId() {
        return 1;
    }

    @Override
    public Integer getTennisId() {
        return 0;
    }

    @Override
    public String getTotalSuturiPePoarta() {
        return "Șuturi pe poartă";
    }

    @Override
    public String getTotalSuturiPePoartaEchipa() {
        return "%s Șuturi pe poartă";
    }

    @Override
    public String getTotalSuturi() {
        return "";
    }

    @Override
    public String getTotalSuturiEchipa() {
        return "";
    }

    @Override
    public String getTotalCartonase() {
        return "Total cartonașe Peste/Sub";
    }

    @Override
    public String getTotalCartonaseEchipa() {
        return "%s Total cartonașe (Peste/Sub)";
    }

    @Override
    public String getTotalCornere() {
        return "Cornere Peste/Sub";
    }

    @Override
    public String getTotalCornereEchipa() {
        return "%s Cornere Peste/Sub";
    }

    @Override
    public String getPrimaReprizaTotalCornere() {
        return "Cornere prima repriză Sub/Peste";
    }

    @Override
    public String getPrimaReprizaTotalCornereEchipa() {
        return "Prima Repriză %s Cornere Peste/Sub";
    }

    @Override
    public String getTotalGoluri() {
        return "Total goluri Peste/Sub";
    }

    @Override
    public String getTotalGoluriEchipa() {
        return "%s - Total goluri Peste/Sub";
    }

    @Override
    public String getPrimaReprizaTotalGoluri() {
        return "";
    }

    @Override
    public String getPrimaReprizaTotalGoluriEchipa() {
        return "";
    }

    @Override
    public String getADouaReprizaTotalGoluriEchipa() {
        return "";
    }

    @Override
    public String getTotalOfsaiduri() {
        return "Total ofsaiduri";
    }

    @Override
    public String getTotalOfsaiduriEchipa() {
        return "%s Total ofsaiduri";
    }

    @Override
    public String getTotalFaulturi() {
        return "Total faulturi comise";
    }

    @Override
    public String getTotalFaulturiEchipa() {
        return "%s Total faulturi comise";
    }

    @Override
    public String getTotalGameuri() {
        return "";
    }

    @Override
    public String getTotalGameuriSetul1() {
        return "";
    }

    @Override
    public String getTotalGameuriJucator() {
        return "";
    }

    @Override
    public String getTotalSeturi() {
        return "";
    }

    @Override
    public String getSplitter() {
        return " - ";
    }
}
