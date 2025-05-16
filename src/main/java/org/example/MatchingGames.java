package org.example;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.example.models.MatchPair;
import org.example.sites.BettingSite;

import java.util.*;

import static org.example.TelegramNotifier.sendMessageToTelegram;

public class MatchingGames {

    private Map<Integer, List<List<String>>> matchesMapFirstSite;
    private Map<Integer, List<List<String>>> matchesMapSecondSite;
    private final Set<String> sentMessages;

    private final BettingSite firstBettingSite;

    private final BettingSite secondBettingSite;

    private Map<Integer, Integer> sportIdPairs;
    private Map<String, String> betsCategories;

    public MatchingGames(BettingSite firstBettingSite, BettingSite secondBettingSite, Set<String> sentMessages) throws Throwable {
        this.firstBettingSite = firstBettingSite;
        this.secondBettingSite = secondBettingSite;
        this.sentMessages = sentMessages;

        initControllers();
        initSportsId();
        initBetsCategories();
    }

    private void initControllers() throws Throwable {
        String matchesResponseFirstSite = firstBettingSite.getAllMatchesContent();
        Map<Integer, List<List<String>>> matchesMapFirstSit = firstBettingSite.getMatchesInformation(matchesResponseFirstSite);

        String matchesResponseSecondSite = secondBettingSite.getAllMatchesContent();
        Map<Integer, List<List<String>>> matchesMapSecondSite = secondBettingSite.getMatchesInformation(matchesResponseSecondSite);

        this.matchesMapFirstSite = matchesMapFirstSit;
        this.matchesMapSecondSite = matchesMapSecondSite;
    }

    private void initSportsId() {
        sportIdPairs = new HashMap<>();
        sportIdPairs.put(this.firstBettingSite.getFootballId(), this.secondBettingSite.getFootballId());
    }

    private void initBetsCategories() {
        betsCategories = new HashMap<>();
        betsCategories.put(firstBettingSite.getTotalSuturiPePoarta(), secondBettingSite.getTotalSuturiPePoarta());
        betsCategories.put(firstBettingSite.getTotalSuturiPePoartaEchipa(), secondBettingSite.getTotalSuturiPePoartaEchipa());
        betsCategories.put(firstBettingSite.getTotalSuturi(), secondBettingSite.getTotalSuturi());
        betsCategories.put(firstBettingSite.getTotalSuturiPePoarta(), secondBettingSite.getTotalSuturiEchipa());
        betsCategories.put(firstBettingSite.getTotalCartonase(), secondBettingSite.getTotalCartonase());
        betsCategories.put(firstBettingSite.getTotalCartonaseEchipa(), secondBettingSite.getTotalCartonaseEchipa());
        betsCategories.put(firstBettingSite.getTotalCornere(), secondBettingSite.getTotalCornere());
        betsCategories.put(firstBettingSite.getTotalCornereEchipa(), secondBettingSite.getTotalCornereEchipa());
        betsCategories.put(firstBettingSite.getPrimaReprizaTotalCornere(), secondBettingSite.getPrimaReprizaTotalCornere());
        betsCategories.put(firstBettingSite.getPrimaReprizaTotalCornereEchipa(), secondBettingSite.getPrimaReprizaTotalCornereEchipa());
        betsCategories.put(firstBettingSite.getTotalGoluri(), secondBettingSite.getTotalGoluri());
        betsCategories.put(firstBettingSite.getPrimaReprizaTotalGoluri(), secondBettingSite.getPrimaReprizaTotalCornere());
        betsCategories.put(firstBettingSite.getPrimaReprizaTotalGoluri(), secondBettingSite.getPrimaReprizaTotalGoluriEchipa());
        betsCategories.put(firstBettingSite.getADouaReprizaTotalGoluriEchipa(), secondBettingSite.getADouaReprizaTotalGoluriEchipa());
        betsCategories.put(firstBettingSite.getTotalGoluriEchipa(), secondBettingSite.getTotalGoluriEchipa());
        betsCategories.put(firstBettingSite.getTotalOfsaiduri(), secondBettingSite.getTotalOfsaiduri());
        betsCategories.put(firstBettingSite.getTotalOfsaiduriEchipa(), secondBettingSite.getTotalOfsaiduriEchipa());
        betsCategories.put(firstBettingSite.getTotalFaulturi(), secondBettingSite.getTotalFaulturi());
        betsCategories.put(firstBettingSite.getTotalFaulturiEchipa(), secondBettingSite.getTotalFaulturiEchipa());
    }

    public Map<MatchPair, Integer[]> getSimilarMatches() {
        Map<MatchPair, Integer[]> matchingGames = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : sportIdPairs.entrySet()) {
            Integer firstSiteSportId = entry.getKey();
            Integer secondSiteSportId = entry.getValue();

            List<List<String>> firstSiteGames = matchesMapFirstSite.get(firstSiteSportId);
            List<List<String>> secondSiteGames = matchesMapSecondSite.get(secondSiteSportId);

            if (firstSiteGames == null || secondSiteGames == null) {
                continue;
            }

            for (List<String> firstSiteGameEntry : firstSiteGames) {
                String firstSiteGame = firstSiteGameEntry.get(0);
                int firstSiteEventId = Integer.parseInt(firstSiteGameEntry.get(1));
                String firstSiteDate = firstSiteGameEntry.get(2);

                for (List<String> secondSiteGameEntry : secondSiteGames) {
                    String secondSiteGame = secondSiteGameEntry.get(0);
                    int secondSiteEventId = Integer.parseInt(secondSiteGameEntry.get(1));
                    String secondSiteDate = secondSiteGameEntry.get(2);

                    if (!firstSiteDate.equals(secondSiteDate)) {
                        continue;
                    }

                    if (isMatchForFootball(firstSiteGame, secondSiteGame) &&
                            Objects.equals(firstSiteSportId, this.firstBettingSite.getFootballId())) {

                        boolean excludeYouth = !firstSiteGame.contains("U23") && !secondSiteGame.contains("U23") &&
                                !firstSiteGame.contains("U19") && !secondSiteGame.contains("U19") &&
                                !firstSiteGame.contains("(F)") && !secondSiteGame.contains("(F)") &&
                                !firstSiteGame.contains("U20") && !secondSiteGame.contains("U20") &&
                                !firstSiteGame.contains("(R)") && !secondSiteGame.contains("(R)") &&
                                !firstSiteGame.contains("II") && !secondSiteGame.contains("II") &&
                                !firstSiteGame.contains("U21") && !secondSiteGame.contains("U21") &&
                                !firstSiteGame.contains(" B") && !secondSiteGame.contains(" B") &&
                                !firstSiteGame.contains("Sub 19") && !secondSiteGame.contains("Sub 19") &&
                                !firstSiteGame.contains("Rezerve") && !secondSiteGame.contains("Rezerve");

                        boolean includeSameYouth = (
                                (firstSiteGame.contains("U23") && secondSiteGame.contains("U23")) ||
                                        (firstSiteGame.contains("U19") && secondSiteGame.contains("U19")) ||
                                        (firstSiteGame.contains("(F)") && secondSiteGame.contains("(F)")) ||
                                        (firstSiteGame.contains("U20") && secondSiteGame.contains("U20")) ||
                                        (firstSiteGame.contains("(R)") && secondSiteGame.contains("(R)")) ||
                                        (firstSiteGame.contains("II") && secondSiteGame.contains("II")) ||
                                        (firstSiteGame.contains("U21") && secondSiteGame.contains("U21")) ||
                                        (firstSiteGame.contains(" B") && secondSiteGame.contains(" B")) ||
                                        (firstSiteGame.contains("Sub 19") && secondSiteGame.contains("Sub 19")) ||
                                        (firstSiteGame.contains("Rezerve") && secondSiteGame.contains("Rezerve"))
                        );

                        if (excludeYouth || includeSameYouth) {
                            MatchPair matchPair = new MatchPair(firstSiteGame, secondSiteGame);
                            matchingGames.put(matchPair, new Integer[]{firstSiteEventId, secondSiteEventId});
                        }
                    }
                }
            }
        }

        return matchingGames;
    }

    private boolean isMatchForFootball(String firstSiteGame, String secondSiteGame) {
        if (firstSiteGame.contains(this.firstBettingSite.getSplitter()) && secondSiteGame.contains(this.secondBettingSite.getSplitter())) {
            String[] firstSiteTeams = firstSiteGame.split(this.firstBettingSite.getSplitter());
            String[] secondSiteTeams = secondSiteGame.split(this.secondBettingSite.getSplitter());
            return areTeamsMatching(firstSiteTeams, secondSiteTeams);
        }
        return false;
    }

    public static boolean areTeamsMatching(String[] firstSiteTeams, String[] secondSiteTeams) {
        String firstSiteTeam1 = firstSiteTeams[0];
        String firstSiteTeam2 = firstSiteTeams[1];
        String secondSiteTeam1 = secondSiteTeams[0];
        String secondSiteTeam2 = secondSiteTeams[1];
        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        double threshold = 0.85;

        boolean isFirstTeamMatch = jaroWinkler.apply(firstSiteTeam1, secondSiteTeam1) >= threshold;
        boolean isSecondTeamMatch = jaroWinkler.apply(firstSiteTeam2, secondSiteTeam2) >= threshold;

        return (isFirstTeamMatch && isSecondTeamMatch);
    }

    public void matchingSameBets(String matchNameFirstSite, String matchNameSecondSite,
                                 Map<String, Map<String, String>> firstSiteMatch,
                                 Map<String, Map<String, String>> secondSiteMatch) {

        String[] firstSteTeams = extractTeams(matchNameFirstSite);
        String[] secondSiteTeams = extractTeams(matchNameSecondSite);

        if (firstSteTeams == null || firstSteTeams.length != 2) {
            return;
        }

        if (secondSiteTeams == null || secondSiteTeams.length != 2) {
            return;
        }

        String firstSiteHomeTeam = firstSteTeams[0].trim();
        String firstSiteAwayTeam = firstSteTeams[1].trim();

        String secondSiteHomeTeam = secondSiteTeams[0].trim();
        String secondSiteAwayTeam = secondSiteTeams[1].trim();

        for (Map.Entry<String, String> category : betsCategories.entrySet()) {
            String firstSiteBetName = category.getKey();
            String secondSiteBetName = category.getValue();

            if (firstSiteBetName.contains("%s")) {
                String firstSiteBetHome = String.format(firstSiteBetName, firstSiteHomeTeam);
                String secondSiteBetHome = String.format(secondSiteBetName, secondSiteHomeTeam);

                String firstSiteBetAway = String.format(firstSiteBetName, firstSiteAwayTeam);
                String secondSiteBetAway = String.format(secondSiteBetName, secondSiteAwayTeam);

                processMatchingBets(matchNameFirstSite, matchNameSecondSite, firstSiteBetHome, secondSiteBetHome, firstSiteMatch, secondSiteMatch);
                processMatchingBets(matchNameFirstSite, matchNameSecondSite, firstSiteBetAway, secondSiteBetAway, firstSiteMatch, secondSiteMatch);
            } else {
                processMatchingBets(matchNameFirstSite, matchNameSecondSite, firstSiteBetName, secondSiteBetName, firstSiteMatch, secondSiteMatch);
            }
        }
    }

    private String[] extractTeams(String matchName) {
        if (matchName.contains(this.firstBettingSite.getSplitter())) {
            return matchName.split(this.firstBettingSite.getSplitter());
        } else if (matchName.contains(this.secondBettingSite.getSplitter())) {
            return matchName.split(this.secondBettingSite.getSplitter());
        }
        return null;
    }

    private void processMatchingBets(String matchNameFirstSite,
                                     String matchNameSecondSite,
                                     String firstSiteBetName,
                                     String secondSiteBetName,
                                     Map<String, Map<String, String>> firstSiteMatch,
                                     Map<String, Map<String, String>> secondSiteMatch) {

        if (firstSiteMatch.containsKey(firstSiteBetName) && secondSiteMatch.containsKey(secondSiteBetName)) {
            Map<String, String> firstSiteBets = firstSiteMatch.get(firstSiteBetName);
            Map<String, String> secondSiteBets = secondSiteMatch.get(secondSiteBetName);

            for (Map.Entry<String, String> firstSiteBet : firstSiteBets.entrySet()) {
                String firstSiteBetKey = firstSiteBet.getKey();
                Double firstSiteOdds = firstSiteBet.getValue() != null ? Double.parseDouble(firstSiteBet.getValue()) : null;

                if (firstSiteOdds == null) continue;

                String oppositeBetKey = getOppositeBetKey(firstSiteBetKey);

                if (oppositeBetKey != null && secondSiteBets.containsKey(oppositeBetKey)) {
                    String secondSiteOddsString = secondSiteBets.get(oppositeBetKey);
                    Double secondSiteOdds = secondSiteOddsString != null ? Double.parseDouble(secondSiteOddsString) : null;

                    if (secondSiteOdds != null) {
                        double arbitrage = calculateArbitrage(firstSiteOdds, secondSiteOdds);

                        if (arbitrage < 95.0) {
                            String message = "**Arbitrage Opportunity Detected!**\n" +
                                    "🏆 **Match:** `" + matchNameFirstSite + "`\n" +
                                    "🏆 **Match:** `" + matchNameSecondSite + "`\n" +
                                    "📌 **Category:** `" + firstSiteBetName + "`\n" +
                                    "📊 **Odds**\n" +
                                    "- `" + this.firstBettingSite.getSiteName() + "`: `" + firstSiteBetKey + "` ➝ **" + firstSiteOdds + "**\n" +
                                    "- `" + this.secondBettingSite.getSiteName() + "`: `" + oppositeBetKey + "` ➝ **" + secondSiteOdds + "**\n" +
                                    "📈 **Arbitrage Percentage:** `" + arbitrage + "%` 🔥";

                            if (!sentMessages.contains(message)) {
                                System.out.println(message);
                                sendMessageToTelegram(message);
                                sentMessages.add(message);
                            }
                        }
                    }
                }
            }
        }
    }

    private String getOppositeBetKey(String betKey) {
        if (betKey.startsWith("Sub")) {
            return betKey.replace("Sub", "Peste");
        } else if (betKey.startsWith("Peste")) {
            return betKey.replace("Peste", "Sub");
        }
        return null;
    }

    private double calculateArbitrage(double odds1, double odds2) {
        double impliedProbability1 = 1 / odds1;
        double impliedProbability2 = 1 / odds2;
        double totalImpliedProbability = impliedProbability1 + impliedProbability2;
        return totalImpliedProbability * 100;
    }
}
