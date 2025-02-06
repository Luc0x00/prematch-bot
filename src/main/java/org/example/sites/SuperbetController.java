package org.example.sites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.text.SimpleDateFormat;
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
        return Objects.requireNonNull(client.newCall(request).execute().body()).string();
    }

    @Override
    public String getMatchContent(Integer matchId) throws Throwable {
        return executeGetRequest(BASE_URL + "v2/ro-RO/events/" + matchId);
    }

    @Override
    public String getAllMatchesContent() throws Throwable {
        String startDate = getCurrentTimeFormatted();
        String endDate = getTime48HoursLater();

        return executeGetRequest(BASE_URL + "v2/ro-RO/events/by-date?offerState=prematch&startDate=" + startDate + "&endDate=" + endDate);
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

    @Override
    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        JsonArray data = extractJsonArray(response);

        if (data == null) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, String>> marketMap = new HashMap<>();

        for (JsonElement match : data) {
            if (match.isJsonObject()) {
                JsonArray odds = extractJsonArray(match.getAsJsonObject());
                if (odds != null) {
                    for (JsonElement odd : odds) {
                        if (odd.isJsonObject()) {
                            JsonObject oddObj = odd.getAsJsonObject();
                            String marketName = oddObj.has("marketName") ? oddObj.get("marketName").getAsString() : "Unknown Market";
                            String betName = oddObj.has("name") ? oddObj.get("name").getAsString() : "Unknown Name";
                            String betPrice = oddObj.has("price") ? String.valueOf(oddObj.get("price").getAsDouble()) : "0.0";

                            if (betName.contains("Sub") || betName.contains("Peste")) {
                                marketMap.computeIfAbsent(marketName, k -> new HashMap<>())
                                        .put(betName, betPrice);
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

    private String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        return sdf.format(new Date());
    }

    private String getTime48HoursLater() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd+HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Bucharest"));
        calendar.add(Calendar.HOUR, 48);
        return sdf.format(calendar.getTime());
    }
}
