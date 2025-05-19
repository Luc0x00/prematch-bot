package org.example.sites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.text.SimpleDateFormat;
import java.util.*;

public class UnibetController implements BettingSite {

    private static final String BASE_URL = "https://eu1.offering-api.kambicdn.com/offering/v2018/ubro/";

    private final OkHttpClient client = new OkHttpClient();

    private String executeGetRequest(String url) throws Throwable {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("User-Agent", "insomnia/10.3.0")
                .build();
        return Objects.requireNonNull(client.newCall(request).execute().body()).string();
    }

    @Override
    public String getAllMatchesContent() throws Throwable {
        String fromTime = getCurrentTimeFormatted();
        String toTime = getTime48HoursLater();

        String url = BASE_URL + "listView/all/all/all/all/starting-within.json?lang=ro_RO&market=RO&client_id=2&channel_id=1&ncid=1735514731370&useCombined=true&from=" + fromTime + "%2B0200&to=" + toTime + "%2B0200";

        return executeGetRequest(url);
    }


    @Override
    public String getMatchContent(Integer matchId) throws Throwable {
        String url = BASE_URL + "betoffer/event/" + matchId + ".json?lang=ro_RO&market=RO&ncid=1734917551";
        return executeGetRequest(url);
    }

    @Override
    public Map<Integer, List<List<String>>> getMatchesInformation(String response) {
        Map<Integer, List<List<String>>> matchesMap = new HashMap<>();
        JsonElement jsonElement = JsonParser.parseString(response);
        if (!jsonElement.isJsonObject()) {
            return matchesMap;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray events = extractEvents(jsonObject);
        if (events == null) {
            return matchesMap;
        }

        for (JsonElement matchElement : events) {
            if (isInvalidJsonObject(matchElement)) {
                continue;
            }

            JsonObject matchObject = matchElement.getAsJsonObject();
            JsonObject eventObject = extractEvent(matchObject);
            if (eventObject != null && eventObject.has("id") && !eventObject.get("id").isJsonNull()) {
                int eventId = eventObject.get("id").getAsInt();
                String name = eventObject.get("name").getAsString();
                String rawStart = eventObject.has("start") && !eventObject.get("start").isJsonNull()
                        ? eventObject.get("start").getAsString()
                        : "";
                String start = convertToIsoFormat(rawStart);

                JsonArray pathArray = eventObject.getAsJsonArray("path");

                if (pathArray != null && !pathArray.isEmpty()) {
                    JsonObject groupObject = pathArray.get(0).getAsJsonObject();
                    int sportId = groupObject.has("id") ? groupObject.get("id").getAsInt() : -1;

                    List<String> matchData = Arrays.asList(name, String.valueOf(eventId), start);
                    matchesMap.computeIfAbsent(sportId, k -> new ArrayList<>()).add(matchData);
                }
            }
        }
        return matchesMap;
    }

    @Override
    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        Map<String, Map<String, String>> marketMap = new HashMap<>();
        JsonElement jsonElement = JsonParser.parseString(response);
        if (isInvalidJsonObject(jsonElement)) {
            return marketMap;
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray events = extractEvents(jsonObject);
        if (events == null) {
            return marketMap;
        }
        JsonArray betOffers = extractBetOffers(jsonObject);
        if (betOffers == null) {
            return marketMap;
        }
        for (JsonElement eventElement : events) {
            if (isInvalidJsonObject(eventElement)) {
                return marketMap;
            }
            JsonObject eventObject = eventElement.getAsJsonObject();
            for (JsonElement betOfferElement : betOffers) {
                if (isInvalidJsonObject(betOfferElement)) {
                    return marketMap;
                }
                JsonObject betOfferObject = betOfferElement.getAsJsonObject();
                int eventId = extractEventId(betOfferObject);
                if (eventId == eventObject.get("id").getAsInt()) {
                    String marketName = extractMarketName(betOfferObject);
                    JsonArray outcomes = extractOutcomes(betOfferObject);
                    if (outcomes == null) {
                        return marketMap;
                    }
                    for (JsonElement outcomeElement : outcomes) {
                        if (isInvalidJsonObject(betOfferElement)) {
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

    @Override
    public String getSiteName() {
        return "Unibet";
    }

    @Override
    public Integer getFootballId() {
        return 1000093190;
    }

    @Override
    public Integer getTennisId() {
        return 1000093193;
    }

    @Override
    public String getTotalSuturiPePoarta() {
        return "Total șuturi pe poartă (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalSuturiPePoartaEchipa() {
        return "Total șuturi pe poartă ale echipei %s (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalSuturi() {
        return "Total șuturi (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalSuturiEchipa() {
        return "Total șuturi %s (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalCartonase() {
        return "Total cartonaşe";
    }

    @Override
    public String getTotalCartonaseEchipa() {
        return "Total cartonașe - %s";
    }

    @Override
    public String getTotalCornere() {
        return "Total cornere";
    }

    @Override
    public String getTotalCornereEchipa() {
        return "Total cornere ale %s";
    }

    @Override
    public String getPrimaReprizaTotalCornere() {
        return "Total cornere - Prima repriză";
    }

    @Override
    public String getPrimaReprizaTotalCornereEchipa() {
        return "Total cornere ale echipei %s - Prima repriză";
    }

    @Override
    public String getTotalGoluri() {
        return "Total goluri";
    }

    @Override
    public String getTotalGoluriEchipa() {
        return "Total goluri ale echipei %s";
    }

    @Override
    public String getPrimaReprizaTotalGoluri() {
        return "Total goluri - Prima repriză";
    }

    @Override
    public String getPrimaReprizaTotalGoluriEchipa() {
        return "Total goluri ale echipei %s - Prima repriză";
    }

    @Override
    public String getADouaReprizaTotalGoluriEchipa() {
        return "Total goluri - A doua repriză";
    }

    @Override
    public String getTotalOfsaiduri() {
        return "Total offside-uri (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalOfsaiduriEchipa() {
        return "Total Offside-uri ale echipei %s (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalFaulturi() {
        return "Total faulturi comise (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalFaulturiEchipa() {
        return "Total faulturi comise de %s (Pariurile sunt stabilite utilizând Opta data)";
    }

    @Override
    public String getTotalGameuri() {
        return "Total Game-uri";
    }

    @Override
    public String getTotalGameuriSetul1() {
        return "Total Game-uri - Setul 1";
    }

    @Override
    public String getTotalGameuriJucator() {
        return "Total game-uri câștigate de %s";
    }

    @Override
    public String getTotalSeturi() {
        return "Total seturi";
    }

    @Override
    public String getSplitter() {
        return " - ";
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

    private String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        return sdf.format(new Date());
    }

    private String getTime48HoursLater() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Bucharest"));
        calendar.add(Calendar.HOUR, 48);
        return sdf.format(calendar.getTime());
    }

    private String convertToIsoFormat(String input) {
        return input.replace("T", " ").replace("Z", "").trim();
    }
}
