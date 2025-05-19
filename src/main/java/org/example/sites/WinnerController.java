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
        JsonArray combinedEvents = new JsonArray();
        String startDate = getCurrentTimeFormatted();
        String endDate = getTime48HoursLater();

        for (int sportId : List.of(1, 3)) {
            try {
                String jsonPayload = String.format("""
                        {
                            "timeFrom": "%s",
                            "timeTo": "%s",
                            "sportId": "%d"
                        }""", startDate, endDate, sportId);

                String response = executePostRequest(jsonPayload, API_URL).get(30, TimeUnit.SECONDS);
                JsonArray events = extractEventsArray(response);

                if (events != null) {
                    for (JsonElement e : events) {
                        combinedEvents.add(e);
                    }
                }
            } catch (Exception e) {
                System.out.println("Eroare la sportId=" + sportId + ": " + e.getMessage());
            }
        }

        JsonObject data = new JsonObject();
        data.add("events", combinedEvents);
        JsonObject root = new JsonObject();
        root.add("data", data);
        return root.toString();
    }

    private JsonArray extractEventsArray(String jsonResponse) {
        try {
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
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public Map<Integer, List<List<String>>> getMatchesInformation(String response) {
        JsonArray data = extractJsonArray(response);

        if (data == null) {
            return Collections.emptyMap();
        }

        Map<Integer, List<List<String>>> result = new HashMap<>();
        for (JsonElement element : data) {
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("idMatch") && obj.has("team1Name") && obj.has("team2Name")
                        && obj.has("idSport") && obj.has("matchDateTime")) {

                    int sportId = obj.get("idSport").getAsInt();
                    int eventId = obj.get("idMatch").getAsInt();
                    long matchTimestamp = obj.get("matchDateTime").getAsLong();

                    String rawDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                            .format(new Date(matchTimestamp));

                    String matchDate = convertToIsoFormat(rawDate);

                    String team1 = getAsStringSafe(obj, "team1Name");
                    String team2 = getAsStringSafe(obj, "team2Name");
                    String matchName = team1 + " - " + team2;

                    List<String> matchData = Arrays.asList(matchName, String.valueOf(eventId), matchDate);
                    result.computeIfAbsent(sportId, k -> new ArrayList<>()).add(matchData);
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
    public Integer getTennisId() {
        return 3;
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
    public String getTotalGameuri() {
        return "Total game-uri";
    }

    @Override
    public String getTotalGameuriSetul1() {
        return "Primul set: Total";
    }

    @Override
    public String getTotalGameuriJucator() {
        return "%s: Total game-uri câștigate";
    }

    @Override
    public String getTotalSeturi() {
        return "Total seturi";
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

    private String convertToIsoFormat(String input) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            inputFormat.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));

            Date date = inputFormat.parse(input);

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.HOUR, -3);

            SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            outputFormat.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
            return outputFormat.format(cal.getTime());

        } catch (Exception e) {
            return input.replace("T", " ").replace("Z", "").replace(".000", "").trim();
        }
    }
}
