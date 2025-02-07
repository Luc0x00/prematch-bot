package org.example.sites;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.*;

public class BetanoController implements BettingSite {

    private static final String BASE_URL = "https://ro.betano.com/api";
    private final OkHttpClient client = new OkHttpClient();

    private String executeGetRequest(String url) throws Throwable {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return Objects.requireNonNull(client.newCall(request).execute().body()).string();
    }

    public String getMatchContent(Integer matchId) throws Throwable {
        return executeGetRequest(BASE_URL + "/cote/acs-sepsi-farul-constanta/" + matchId + "/?bt=13");
    }

    public String getAllMatchesContent() throws Throwable {
        return executeGetRequest(BASE_URL + "/sport/fotbal/meciurile-urmatoare-de-azi/?sort=Leagues&req=la,s,stnf,c,mb");
    }

    public Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> getMatchesInformation(String response) {
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
            Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> result = new HashMap<>();

            for (JsonElement blockElement : blocks) {
                JsonObject block = blockElement.getAsJsonObject();
                JsonArray events = block.has("events") ? block.getAsJsonArray("events") : null;

                if (events == null) continue;

                for (JsonElement eventElement : events) {
                    JsonObject event = eventElement.getAsJsonObject();
                    if (event.has("sportId") && event.has("name") && event.has("id")) {
                        int sportId = event.get("sportId").getAsString().equals("FOOT") ? 1 : 0;
                        String matchName = event.get("name").getAsString();
                        int eventId = event.get("id").getAsInt();

                        if (!matchName.contains("Maccabi") && !matchName.contains("Hapoel")) {
                            result.computeIfAbsent(sportId, k -> new ArrayList<>())
                                    .add(new AbstractMap.SimpleEntry<>(matchName, eventId));
                        }
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
    public String getSplitter() {
        return " - ";
    }
}
