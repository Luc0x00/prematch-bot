package org.example.sites;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.text.SimpleDateFormat;
import java.util.*;

public class StanleyController implements BettingSite {

    private static final String BASE_URL = "https://sportsbook-sm-distribution-api.nsoft.com/api/v1/";
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
        return executeGetRequest(BASE_URL + "events/" + matchId + "?companyUuid=682e6a38-b5ad-4c58-a743-3b06c79e55cd&timezone=Europe/Bucharest&id=" + matchId + "&language=%7B\"default\":\"ro\",\"tournament\":\"ro\",\"category\":\"ro\",\"sport\":\"ro\"%7D");
    }

    public String getAllMatchesContent() throws Throwable {
        String startDate = getCurrentTimeFormatted();
        String endDate = getTime48HoursLater();

        return executeGetRequest(BASE_URL + "events?companyUuid=682e6a38-b5ad-4c58-a743-3b06c79e55cd&filter%5Bfrom%5D=" + startDate + "&timezone=Europe/Bucharest&language=%7B\"default\":\"ro\",\"tournament\":\"ro\",\"category\":\"ro\",\"sport\":\"ro\"%7D&filter%5Bto%5D=" + endDate + "&shortProps=1&offerTemplate=WEB_OVERVIEW&dataFormat=%7B\"default\":\"array\",\"events\":\"object\",\"outcomes\":\"array\"%7D&filter%5BsportId%5D=4");
    }

    public Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> getMatchesInformation(String response) {
        JsonObject data = JsonParser.parseString(response).getAsJsonObject().getAsJsonObject("data");
        JsonObject events = data.getAsJsonObject("events");

        if (events == null) {
            return Collections.emptyMap();
        }

        Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> result = new HashMap<>();
        for (String key : events.keySet()) {
            JsonObject obj = events.getAsJsonObject(key);
            int sportId = obj.get("b").getAsInt();
            String matchName = obj.get("j").getAsString();
            int eventId = obj.get("a").getAsInt();
            JsonArray participants = obj.getAsJsonArray("p");
            boolean excludeMatch = false;

            if (participants != null) {
                for (JsonElement participant : participants) {
                    if (participant.isJsonObject()) {
                        String participantName = participant.getAsJsonObject().get("d").getAsString();
                        if (participantName.contains("U23") || participantName.contains("U19") || participantName.contains("(F)") ||
                                participantName.contains("U20") || participantName.contains("(R)") || participantName.contains("II") ||
                                participantName.contains("U21")) {
                            excludeMatch = true;
                            break;
                        }
                    }
                }
            }

            if (excludeMatch) {
                continue;
            }

            result.computeIfAbsent(sportId, k -> new ArrayList<>())
                .add(new AbstractMap.SimpleEntry<>(matchName, eventId));
        }
        return result;
    }

    public Map<String, Map<String, String>> getMatchMarkets(String response) {
        JsonObject jsonObject = com.google.gson.JsonParser.parseString(response).getAsJsonObject();

        JsonObject data = jsonObject.has("data") && jsonObject.get("data").isJsonObject()
                ? jsonObject.getAsJsonObject("data")
                : null;

        if (data == null) {
            return Collections.emptyMap();
        }

        Map<String, Map<String, String>> marketMap = new HashMap<>();

        String matchName = data.has("name") ? data.get("name").getAsString() : "Unknown Match";
        String[] teams = matchName.split(" - ");
        String team1 = teams.length > 0 ? teams[0] : "Gazde";
        String team2 = teams.length > 1 ? teams[1] : "Oaspeti";

        JsonArray markets = data.has("markets") && data.get("markets").isJsonArray()
                ? data.getAsJsonArray("markets")
                : null;

        if (markets != null) {
            for (JsonElement market : markets) {
                if (market.isJsonObject()) {
                    JsonObject marketObj = market.getAsJsonObject();
                    String marketName = marketObj.has("name") ? marketObj.get("name").getAsString() : "Unknown Market";

                    JsonArray outcomes = marketObj.has("outcomes") && marketObj.get("outcomes").isJsonArray()
                            ? marketObj.getAsJsonArray("outcomes")
                            : null;

                    if (outcomes != null) {
                        for (JsonElement outcome : outcomes) {
                            if (outcome.isJsonObject()) {
                                JsonObject outcomeObj = outcome.getAsJsonObject();
                                String betName = outcomeObj.has("name") ? outcomeObj.get("name").getAsString() : "Unknown Name";
                                String betPrice = outcomeObj.has("odd") ? outcomeObj.get("odd").getAsString() : "0.0";

                                if (marketName.contains("Gazde")) {
                                    marketName = marketName.replace("Gazde", team1);
                                } else if (marketName.contains("Oaspeti")) {
                                    marketName = marketName.replace("Oaspeti", team2);
                                }

                                if (betName.startsWith("G ")) {
                                    betName = betName.substring(2);
                                } else if (betName.startsWith("O ")) {
                                    betName = betName.substring(2);
                                }

                                if (betName.contains("Sub") || betName.contains("Peste")) {
                                    marketMap.computeIfAbsent(marketName, k -> new HashMap<>())
                                            .put(betName, betPrice);
                                }
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
        return "Stanley";
    }

    @Override
    public Integer getFootballId() {
        return 4;
    }

    @Override
    public String getTotalSuturiPePoarta() {
        return "Total Suturi pe Poarta";
    }

    @Override
    public String getTotalSuturiPePoartaEchipa() {
        return "Total Suturi pe Poarta %s";
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
        return "Total Cartonase";
    }

    @Override
    public String getTotalCartonaseEchipa() {
        return "Total Cartonase %s";
    }

    @Override
    public String getTotalCornere() {
        return "Total Cornere";
    }

    @Override
    public String getTotalCornereEchipa() {
        return "Total Cornere %s";
    }

    @Override
    public String getPrimaReprizaTotalCornere() {
        return "Prima Repriza - Total Cornere";
    }

    @Override
    public String getPrimaReprizaTotalCornereEchipa() {
        return "";
    }

    @Override
    public String getTotalGoluri() {
        return "Total goluri S/P";
    }

    @Override
    public String getTotalGoluriEchipa() {
        return "";
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
        return "Total Ofsaiduri";
    }

    @Override
    public String getTotalOfsaiduriEchipa() {
        return "Total Ofsaiduri %s";
    }

    @Override
    public String getTotalFaulturi() {
        return "Total Faulturi";
    }

    @Override
    public String getTotalFaulturiEchipa() {
        return "Total Faulturi %s";
    }

    @Override
    public String getSplitter() {
        return " - ";
    }

    private String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        return sdf.format(new Date());
    }

    private String getTime48HoursLater() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Bucharest"));
        calendar.add(Calendar.HOUR, 48);
        return sdf.format(calendar.getTime());
    }
}
