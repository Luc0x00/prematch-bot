package org.example.sites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WinnerController implements BettingSite {

    private final OkHttpClient client = new OkHttpClient();
    private static final String API_URL = "https://micros-prod1.gambling-solutions.ro/api/digitain-fetcher/v2/public/events";
    private static final String BASE_URL = "https://micros-prod1.gambling-solutions.ro/api/digitain-tickets/v1/public/bet-builder/evaluate";

    public CompletableFuture<String> executePostRequest(String jsonPayload, String apiUrl) {
        CompletableFuture<String> future = new CompletableFuture<>();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonPayload);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.complete("");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        future.complete(responseBody.string());
                    } else {
                        future.complete("");
                    }
                }
            }
        });

        return future;
    }

    @Override
    public String getMatchContent(Integer matchId) {
        try {
            String jsonPayload = "{\n" +
                    "    \"eventId\": \"" + matchId + "\",\n" +
                    "    \"isLive\": false,\n" +
                    "    \"language\": \"ro\",\n" +
                    "    \"stakes\": []\n" +
                    "}";

            return executePostRequest(jsonPayload, BASE_URL).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
    }


    @Override
    public String getAllMatchesContent() {
        try {
            String startDate = getCurrentTimeFormatted();
            String endDate = getTime48HoursLater();

            String jsonPayload = String.format("""
            {
                "timeFrom": "%s",
                "timeTo": "%s",
                "sportId": "1"
            }""", startDate, endDate);

            return executePostRequest(jsonPayload, API_URL).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
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
                if (obj.has("idMatch") && obj.has("team1Name") && obj.has("team2Name") && obj.has("idSport")) {
                    int sportId = obj.get("idSport").getAsInt();
                    int eventId = obj.get("idMatch").getAsInt();

                    String team1 = getAsStringSafe(obj, "team1Name");
                    String team2 = getAsStringSafe(obj, "team2Name");
                    String matchName = team1 + " - " + team2;


                    if (!matchName.contains("Maccabi") && !matchName.contains("Hapoel")) {
                        result.computeIfAbsent(sportId, k -> new ArrayList<>())
                                .add(new AbstractMap.SimpleEntry<>(matchName, eventId));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        JsonElement jsonElement = JsonParser.parseString(response);
        if (!jsonElement.isJsonObject()) {
            return Collections.emptyMap();
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (!jsonObject.has("bets") || !jsonObject.get("bets").isJsonArray()) {
            return Collections.emptyMap();
        }

        JsonArray betsArray = jsonObject.getAsJsonArray("bets");
        Map<String, Map<String, String>> marketMap = new HashMap<>();

        for (JsonElement betElement : betsArray) {
            if (!betElement.isJsonObject()) continue;

            JsonObject betObject = betElement.getAsJsonObject();
            String marketName = getSafeString(betObject, "mbDisplayName", "Unknown Market");

            if (!betObject.has("mbOutcomes") || !betObject.get("mbOutcomes").isJsonArray()) {
                continue;
            }

            JsonArray outcomesArray = betObject.getAsJsonArray("mbOutcomes");
            Map<String, String> outcomesMap = new HashMap<>();

            for (JsonElement outcomeElement : outcomesArray) {
                if (!outcomeElement.isJsonObject()) continue;

                JsonObject outcomeObject = outcomeElement.getAsJsonObject();
                String baseBetName = getSafeString(outcomeObject, "mboDisplayName", "Unknown Name");
                String argument = getSafeString(outcomeObject, "argument", "");
                String fullBetName = argument.isEmpty() ? baseBetName : baseBetName + " " + argument;
                String betPrice = String.valueOf(getSafeDouble(outcomeObject));

                if (fullBetName.contains("Sub") || fullBetName.contains("Peste")) {
                    outcomesMap.put(fullBetName, betPrice);
                }
            }

            if (!outcomesMap.isEmpty()) {
                marketMap.put(marketName, outcomesMap);
            }
        }

        return marketMap;
    }

    @Override
    public String getSiteName() {
        return "Winner";
    }

    @Override
    public Integer getFootballId() {
        return 1;
    }

    @Override
    public String getTotalSuturiPePoarta() {
        return "Șuturi pe poartă - Total";
    }

    @Override
    public String getTotalSuturiPePoartaEchipa() {
        return "Șuturi pe poartă - Total %s";
    }

    @Override
    public String getTotalSuturi() {
        return "Șuturi - Total";
    }

    @Override
    public String getTotalSuturiEchipa() {
        return "Șuturi - Total %s";
    }

    @Override
    public String getTotalCartonase() {
        return "";
    }

    @Override
    public String getTotalCartonaseEchipa() {
        return "";
    }

    @Override
    public String getTotalCornere() {
        return "Cornere - Total cornere";
    }

    @Override
    public String getTotalCornereEchipa() {
        return "Cornere - %s: Total cornere";
    }

    @Override
    public String getPrimaReprizaTotalCornere() {
        return "Cornere - Prima repriză: Total";
    }

    @Override
    public String getPrimaReprizaTotalCornereEchipa() {
        return "Cornere - Prima repriză: Total %s";
    }

    @Override
    public String getTotalGoluri() {
        return "Total goluri";
    }

    @Override
    public String getTotalGoluriEchipa() {
        return "%s: Total goluri";
    }

    @Override
    public String getPrimaReprizaTotalGoluri() {
        return "Prima repriză - Total";
    }

    @Override
    public String getPrimaReprizaTotalGoluriEchipa() {
        return "Prima repriză - Total goluri %s";
    }

    @Override
    public String getADouaReprizaTotalGoluriEchipa() {
        return "A 2-a repriză - Total";
    }

    @Override
    public String getTotalOfsaiduri() {
        return "Offside-uri - Total";
    }

    @Override
    public String getTotalOfsaiduriEchipa() {
        return "Offside-uri - Total %s";
    }

    @Override
    public String getTotalFaulturi() {
        return "Faulturi - Total";
    }

    @Override
    public String getTotalFaulturiEchipa() {
        return "Faulturi - Total %s";
    }

    @Override
    public String getSplitter() {
        return " - ";
    }

    private String getSafeString(JsonObject obj, String key, String defaultValue) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : defaultValue;
    }

    private double getSafeDouble(JsonObject obj) {
        return (obj.has("mboOddValue") && !obj.get("mboOddValue").isJsonNull()) ? obj.get("mboOddValue").getAsDouble() : 0.0;
    }

    private JsonArray extractJsonArray(String jsonResponse) {
        JsonElement jsonElement = JsonParser.parseString(jsonResponse);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("data")) {
                JsonObject dataObject = jsonObject.getAsJsonObject("data");
                if (dataObject.has("events") && dataObject.get("events").isJsonArray()) {
                    return dataObject.getAsJsonArray("events");
                }
            }
        }
        return null;
    }

    private String getAsStringSafe(JsonObject obj, String key) {
        if (obj.has(key)) {
            JsonElement element = obj.get(key);
            if (element.isJsonObject()) {
                JsonObject nameObj = element.getAsJsonObject();
                if (nameObj.has("42")) {
                    return nameObj.get("42").getAsString();
                }
            }
        }
        return "Unknown";
    }

    private String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        return sdf.format(new Date());
    }

    private String getTime48HoursLater() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Bucharest"));
        calendar.add(Calendar.HOUR, 48);
        return sdf.format(calendar.getTime());
    }
}
