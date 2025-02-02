package org.example;

import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.example.models.MatchPair;
import org.example.sites.BettingSite;

import java.util.*;

public class MatchingGames {

    private Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapFirstSite;
    private Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapSecondSite;

    private final BettingSite firstBettingSite;

    private final BettingSite secondBettingSite;

    private Map<Integer, Integer> sportIdPairs;
    private Map<String, String> betsCategories;

    public MatchingGames(BettingSite firstBettingSite, BettingSite secondBettingSite) throws Throwable {
        this.firstBettingSite = firstBettingSite;
        this.secondBettingSite = secondBettingSite;

        initControllers();
        initSportsId();
        initBetsCategories();
    }

    private void initControllers() throws Throwable {
        String matchesResponseFirstSite = firstBettingSite.getAllMatchesContent();
        Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapFirstSit = firstBettingSite.getMatchesInformation(matchesResponseFirstSite);

        String matchesResponseSecondSite = secondBettingSite.getAllMatchesContent();
        Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapSecondSite = secondBettingSite.getMatchesInformation(matchesResponseSecondSite);

        this.matchesMapFirstSite = matchesMapFirstSit;
        this.matchesMapSecondSite = matchesMapSecondSite;
    }

    private void initSportsId() {
        sportIdPairs = new HashMap<>();
        sportIdPairs.put(this.firstBettingSite.getFootballId(), this.secondBettingSite.getFootballId());
        sportIdPairs.put(this.firstBettingSite.getTennisId(), this.secondBettingSite.getTennisId());
        sportIdPairs.put(this.firstBettingSite.getBasketballId(), this.secondBettingSite.getBasketballId());
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

        betsCategories.put(firstBettingSite.getTotalGameuri(), secondBettingSite.getTotalGameuri());
        betsCategories.put(firstBettingSite.getTotalGameuriEchipa(), secondBettingSite.getTotalGameuriEchipa());
        betsCategories.put(firstBettingSite.getTotalSeturi(), secondBettingSite.getTotalSeturi());
        betsCategories.put(firstBettingSite.getSet1TotalGameuri(), secondBettingSite.getSet1TotalGameuri());

        betsCategories.put(firstBettingSite.getTotalPuncte(), secondBettingSite.getTotalPuncte());
        betsCategories.put(firstBettingSite.getTotalPuncteEchipa(), secondBettingSite.getTotalPuncteEchipa());
    }

    public Map<MatchPair, Integer[]> getSimilarMatches() {
        Map<MatchPair, Integer[]> matchingGames = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : sportIdPairs.entrySet()) {
            Integer firstSiteSportId = entry.getKey();
            Integer secondSiteSportId = entry.getValue();

            List<AbstractMap.SimpleEntry<String, Integer>> firstSiteGames = matchesMapFirstSite.get(firstSiteSportId);
            List<AbstractMap.SimpleEntry<String, Integer>> secondSiteGames = matchesMapSecondSite.get(secondSiteSportId);

            if (firstSiteGames == null || secondSiteGames == null) {
                continue;
            }

            for (AbstractMap.SimpleEntry<String, Integer> firstSiteGameEntry : firstSiteGames) {
                String firstSiteGame = firstSiteGameEntry.getKey();

                for (AbstractMap.SimpleEntry<String, Integer> secondSiteGameEntry : secondSiteGames) {
                    String secondSiteGame = secondSiteGameEntry.getKey();

                    if (isMatchForFootball(firstSiteGame, secondSiteGame) && Objects.equals(firstSiteSportId, this.firstBettingSite.getFootballId())) {
                        if (!firstSiteGame.contains("U23") && !secondSiteGame.contains("U23") &&
                                !firstSiteGame.contains("U19") && !secondSiteGame.contains("U19") && !firstSiteGame.contains("(F)") && !secondSiteGame.contains("(F)")) {
                            MatchPair matchPair = new MatchPair(firstSiteGame, secondSiteGame);
                            matchingGames.put(matchPair, new Integer[]{firstSiteGameEntry.getValue(), secondSiteGameEntry.getValue()});
                        } else if ((firstSiteGame.contains("U23") && secondSiteGame.contains("U23")) ||
                                (firstSiteGame.contains("U19") && secondSiteGame.contains("U19")) || (firstSiteGame.contains("(F)") && secondSiteGame.contains("(F)"))) {
                            MatchPair matchPair = new MatchPair(firstSiteGame, secondSiteGame);
                            matchingGames.put(matchPair, new Integer[]{firstSiteGameEntry.getValue(), secondSiteGameEntry.getValue()});
                        }
                    } else if (isMatchForTennis(firstSiteGame, secondSiteGame) && Objects.equals(firstSiteSportId, this.firstBettingSite.getTennisId())) {
                        MatchPair matchPair = new MatchPair(firstSiteGame, secondSiteGame);
                        matchingGames.put(matchPair, new Integer[]{firstSiteGameEntry.getValue(), secondSiteGameEntry.getValue()});
                    } else if (isMatchForBasketball(firstSiteGame, secondSiteGame) && Objects.equals(firstSiteSportId, this.firstBettingSite.getBasketballId())) {
                        if (!firstSiteGame.contains("(F)") && !secondSiteGame.contains("(F)")) {
                            MatchPair matchPair = new MatchPair(firstSiteGame, secondSiteGame);
                            matchingGames.put(matchPair, new Integer[]{firstSiteGameEntry.getValue(), secondSiteGameEntry.getValue()});
                        } else if (firstSiteGame.contains("(F)") && secondSiteGame.contains("(F)")) {
                            MatchPair matchPair = new MatchPair(firstSiteGame, secondSiteGame);
                            matchingGames.put(matchPair, new Integer[]{firstSiteGameEntry.getValue(), secondSiteGameEntry.getValue()});
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

    private boolean isMatchForTennis(String firstSiteGame, String secondSiteGame) {
        if (firstSiteGame.contains(this.firstBettingSite.getSplitter()) && secondSiteGame.contains(this.secondBettingSite.getSplitter())) {
            String[] firstSiteTeams = firstSiteGame.split(this.firstBettingSite.getSplitter());
            String[] secondSiteTeams = secondSiteGame.split(this.secondBettingSite.getSplitter());
            return areTennisPlayersMatching(firstSiteTeams, secondSiteTeams);
        }
        return false;
    }

    private boolean isMatchForBasketball(String firstSiteGame, String secondSiteGame) {
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
        double threshold = 0.90;

        boolean isFirstTeamMatch = jaroWinkler.apply(firstSiteTeam1, secondSiteTeam1) >= threshold;
        boolean isSecondTeamMatch = jaroWinkler.apply(firstSiteTeam2, secondSiteTeam2) >= threshold;

        return (isFirstTeamMatch && isSecondTeamMatch) ||
                (jaroWinkler.apply(firstSiteTeam1, secondSiteTeam2) >= threshold &&
                        jaroWinkler.apply(firstSiteTeam2, secondSiteTeam1) >= threshold);
    }

    private boolean areTennisPlayersMatching(String[] firstSitePlayers, String[] secondSitePlayers) {
        if (firstSitePlayers.length != 2 || secondSitePlayers.length != 2) {
            return false;
        }

        String firstSitePlayer1 = normalizeName(firstSitePlayers[0]);
        String firstSitePlayer2 = normalizeName(firstSitePlayers[1]);
        String secondSitePlayer1 = normalizeName(secondSitePlayers[0]);
        String secondSitePlayer2 = normalizeName(secondSitePlayers[1]);

        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        double threshold = 0.80;

        boolean isFirstPlayerMatch = jaroWinkler.apply(firstSitePlayer1, secondSitePlayer1) >= threshold;
        boolean isSecondPlayerMatch = jaroWinkler.apply(firstSitePlayer2, secondSitePlayer2) >= threshold;

        return (isFirstPlayerMatch && isSecondPlayerMatch) ||
                (jaroWinkler.apply(firstSitePlayer1, secondSitePlayer2) >= threshold &&
                        jaroWinkler.apply(firstSitePlayer2, secondSitePlayer1) >= threshold);
    }

    private String normalizeName(String name) {
        if (name.contains(",")) {
            String[] parts = name.split(",");
            return parts[1].trim() + " " + parts[0].trim();
        }
        return name.trim();
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

                processMatchingBets(matchNameFirstSite, firstSiteBetHome, secondSiteBetHome, firstSiteMatch, secondSiteMatch);
                processMatchingBets(matchNameFirstSite, firstSiteBetAway, secondSiteBetAway, firstSiteMatch, secondSiteMatch);
            } else {
                processMatchingBets(matchNameFirstSite, firstSiteBetName, secondSiteBetName, firstSiteMatch, secondSiteMatch);
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

    private void processMatchingBets(String matchName,
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

                        if (arbitrage < 100.0) {
                            System.out.println("Match: " + matchName);
                            System.out.println("Category: " + firstSiteBetName);
                            System.out.println("Arbitrage Opportunity Detected!");
                            System.out.println("Matching Bet Pair:");
                            System.out.println(this.firstBettingSite.getSiteName() + ": " + firstSiteBetKey + " - Odds: " + firstSiteOdds);
                            System.out.println(this.secondBettingSite.getSiteName() + ": " + oppositeBetKey + " - Odds: " + secondSiteOdds);
                            System.out.println("Arbitrage Percentage: " + arbitrage + "%");
                            System.out.println();
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