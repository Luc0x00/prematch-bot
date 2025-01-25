package org.example;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.*;

public class MatchingGames {

    private final Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapSuperbet;
    private final Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapUnibet;

    private final Map<Integer, Integer> sportIdPairs;
    private final Map<String, String> betsCategories;

    public MatchingGames(Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapSuperbet, Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapUnibet) {
        this.matchesMapSuperbet = matchesMapSuperbet;
        this.matchesMapUnibet = matchesMapUnibet;
        sportIdPairs = new HashMap<>();
        sportIdPairs.put(5, 1000093190);
        sportIdPairs.put(2, 1000093193);
        sportIdPairs.put(4, 1000093204);
        betsCategories = new HashMap<>();
        betsCategories.put("Total șuturi pe poartă", "Total șuturi pe poartă (Pariurile sunt stabilite utilizând Opta data)");
        betsCategories.put("Șuturi pe poartă %s", "Total șuturi pe poartă ale echipei %s (Pariurile sunt stabilite utilizând Opta data)");
        betsCategories.put("Total șuturi %s", "Total șuturi %s (Pariurile sunt stabilite utilizând Opta data)");
        betsCategories.put("Total cartonașe", "Total cartonaşe");
        betsCategories.put("Total cartonașe %s", "Total cartonașe - %s");
        betsCategories.put("Total cornere", "Total cornere");
        betsCategories.put("Total cornere %s", "Total cornere ale %s");
        betsCategories.put("Prima repriză - Total cornere", "Total cornere - Prima repriză");
        betsCategories.put("Prima repriză - Total cornere %s", "Total cornere ale echipei %s - Prima repriză");
        betsCategories.put("Total goluri", "Total goluri");
        betsCategories.put("Prima repriză - Total goluri", "Total goluri - Prima repriză");
        betsCategories.put("Prima repriză - Total goluri %s", "Total goluri ale echipei %s - Prima repriză");
        betsCategories.put("A doua repriză - Total goluri", "Total goluri - A doua repriză");
        betsCategories.put("Total goluri %s", "Total goluri ale echipei %s");
        betsCategories.put("Total ofsaiduri", "Total offside-uri (Pariurile sunt stabilite utilizând Opta data)");
        betsCategories.put("Total ofsaiduri %s", "Total Offside-uri ale echipei %s (Pariurile sunt stabilite utilizând Opta data)");
        betsCategories.put("Total faulturi", "Total faulturi comise (Pariurile sunt stabilite utilizând Opta data)");
        betsCategories.put("Total faulturi %s", "Total faulturi comise de %s (Pariurile sunt stabilite utilizând Opta data)");

        betsCategories.put("Total game-uri", "Total Game-uri");
        betsCategories.put("Total game-uri - %s", "Total game-uri câștigate de %s");
        betsCategories.put("Total seturi", "Total seturi");
        betsCategories.put("Set 1. - Total game-uri", "Total Game-uri - Setul 1");

        betsCategories.put("Total puncte (incl. prelungiri)", "Total puncte - Inclusiv Prelungiri");
        betsCategories.put("Total puncte %s (incl. prelungiri)", "Total Puncte %s - Inclusiv Prelungiri");
    }

    public Map<MatchPair, Integer[]> getSimilarMatches() {
        Map<MatchPair, Integer[]> matchingGames = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : sportIdPairs.entrySet()) {
            Integer superbetSportId = entry.getKey();
            Integer unibetSportId = entry.getValue();

            List<AbstractMap.SimpleEntry<String, Integer>> superbetGames = matchesMapSuperbet.get(superbetSportId);
            List<AbstractMap.SimpleEntry<String, Integer>> unibetGames = matchesMapUnibet.get(unibetSportId);

            if (superbetGames == null || unibetGames == null) {
                System.out.println("No games found for sport ID " + superbetSportId);
                continue;
            }

            for (AbstractMap.SimpleEntry<String, Integer> superbetGameEntry : superbetGames) {
                String superbetGame = superbetGameEntry.getKey();

                for (AbstractMap.SimpleEntry<String, Integer> unibetGameEntry : unibetGames) {
                    String unibetGame = unibetGameEntry.getKey();

                    if (isMatchForFootball(superbetGame, unibetGame) && superbetSportId == 5) {
                        if (!superbetGame.contains("U23") && !unibetGame.contains("U23") &&
                                !superbetGame.contains("U19") && !unibetGame.contains("U19") && !superbetGame.contains("(F)") && !unibetGame.contains("(F)")) {
                            MatchPair matchPair = new MatchPair(superbetGame, unibetGame);
                            matchingGames.put(matchPair, new Integer[]{superbetGameEntry.getValue(), unibetGameEntry.getValue()});
                        } else if ((superbetGame.contains("U23") && unibetGame.contains("U23")) ||
                                (superbetGame.contains("U19") && unibetGame.contains("U19")) || (superbetGame.contains("(F)") && unibetGame.contains("(F)"))) {
                            MatchPair matchPair = new MatchPair(superbetGame, unibetGame);
                            matchingGames.put(matchPair, new Integer[]{superbetGameEntry.getValue(), unibetGameEntry.getValue()});
                        }
                    } else if (isMatchForTennis(superbetGame, unibetGame) && superbetSportId == 2) {
                        MatchPair matchPair = new MatchPair(superbetGame, unibetGame);
                        matchingGames.put(matchPair, new Integer[]{superbetGameEntry.getValue(), unibetGameEntry.getValue()});
                    } else if (isMatchForBasketball(superbetGame, unibetGame) && superbetSportId == 4) {
                        if (!superbetGame.contains("(F)") && !unibetGame.contains("(F)")) {
                            MatchPair matchPair = new MatchPair(superbetGame, unibetGame);
                            matchingGames.put(matchPair, new Integer[]{superbetGameEntry.getValue(), unibetGameEntry.getValue()});
                        } else if (superbetGame.contains("(F)") && unibetGame.contains("(F)")) {
                            MatchPair matchPair = new MatchPair(superbetGame, unibetGame);
                            matchingGames.put(matchPair, new Integer[]{superbetGameEntry.getValue(), unibetGameEntry.getValue()});
                        }
                    }
                }
            }
        }
        return matchingGames;
    }

    private boolean isMatchForFootball(String superbetGame, String unibetGame) {
        if (superbetGame.contains("·") && unibetGame.contains(" - ")) {
            String[] superbetTeams = superbetGame.split("·");
            String[] unibetTeams = unibetGame.split(" - ");
            return areTeamsMatching(superbetTeams, unibetTeams);
        }
        return false;
    }

    private boolean isMatchForTennis(String superbetGame, String unibetGame) {
        if (superbetGame.contains("·") && unibetGame.contains(" - ")) {
            String[] superbetPlayers = superbetGame.split("·");
            String[] unibetPlayers = unibetGame.split(" - ");
            return areTennisPlayersMatching(superbetPlayers, unibetPlayers);
        }
        return false;
    }

    private boolean isMatchForBasketball(String superbetGame, String unibetGame) {
        if (superbetGame.contains("·") && unibetGame.contains(" - ")) {
            String[] superbetTeams = superbetGame.split("·");
            String[] unibetTeams = unibetGame.split(" - ");
            return areTeamsMatching(superbetTeams, unibetTeams);
        }
        return false;
    }

    public static boolean areTeamsMatching(String[] superbetTeams, String[] unibetTeams) {
        String superbetTeam1 = superbetTeams[0];
        String superbetTeam2 = superbetTeams[1];
        String unibetTeam1 = unibetTeams[0];
        String unibetTeam2 = unibetTeams[1];
        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        double threshold = 0.90;

        boolean isFirstTeamMatch = jaroWinkler.apply(superbetTeam1, unibetTeam1) >= threshold;
        boolean isSecondTeamMatch = jaroWinkler.apply(superbetTeam2, unibetTeam2) >= threshold;

        return (isFirstTeamMatch && isSecondTeamMatch) ||
                (jaroWinkler.apply(superbetTeam1, unibetTeam2) >= threshold &&
                        jaroWinkler.apply(superbetTeam2, unibetTeam1) >= threshold);
    }

    private boolean areTennisPlayersMatching(String[] superbetPlayers, String[] unibetPlayers) {
        if (superbetPlayers.length != 2 || unibetPlayers.length != 2) {
            return false;
        }

        String superbetPlayer1 = normalizeName(superbetPlayers[0]);
        String superbetPlayer2 = normalizeName(superbetPlayers[1]);
        String unibetPlayer1 = normalizeName(unibetPlayers[0]);
        String unibetPlayer2 = normalizeName(unibetPlayers[1]);

        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        double threshold = 0.80;

        boolean isFirstPlayerMatch = jaroWinkler.apply(superbetPlayer1, unibetPlayer1) >= threshold;
        boolean isSecondPlayerMatch = jaroWinkler.apply(superbetPlayer2, unibetPlayer2) >= threshold;

        return (isFirstPlayerMatch && isSecondPlayerMatch) ||
                (jaroWinkler.apply(superbetPlayer1, unibetPlayer2) >= threshold &&
                        jaroWinkler.apply(superbetPlayer2, unibetPlayer1) >= threshold);
    }

    private String normalizeName(String name) {
        if (name.contains(",")) {
            String[] parts = name.split(",");
            return parts[1].trim() + " " + parts[0].trim();
        }
        return name.trim();
    }

    public void matchingSameBets(String matchNameSuperbet, String matchNameUnibet,
                                 Map<String, List<AbstractMap.SimpleEntry<String, Double>>> superbetMatch,
                                 Map<String, Map<String, String>> unibetMatch) {

        String[] superbetTeams = extractTeams(matchNameSuperbet);
        String[] unibetTeams = extractTeams(matchNameUnibet);

        if (superbetTeams == null || superbetTeams.length != 2) {
            System.out.println("Invalid Superbet match format for: " + matchNameSuperbet);
            return;
        }

        if (unibetTeams == null || unibetTeams.length != 2) {
            System.out.println("Invalid Unibet match format for: " + matchNameUnibet);
            return;
        }

        String superbetHomeTeam = superbetTeams[0].trim();
        String superbetAwayTeam = superbetTeams[1].trim();

        String unibetHomeTeam = unibetTeams[0].trim();
        String unibetAwayTeam = unibetTeams[1].trim();

        for (Map.Entry<String, String> category : betsCategories.entrySet()) {
            String superbetBetName = category.getKey();
            String unibetBetName = category.getValue();

            if (superbetBetName.contains("%s")) {
                String superbetBetHome = String.format(superbetBetName, superbetHomeTeam);
                String unibetBetHome = String.format(unibetBetName, unibetHomeTeam);

                String superbetBetAway = String.format(superbetBetName, superbetAwayTeam);
                String unibetBetAway = String.format(unibetBetName, unibetAwayTeam);

                processMatchingBets(matchNameSuperbet, superbetBetHome, unibetBetHome, superbetMatch, unibetMatch);
                processMatchingBets(matchNameSuperbet, superbetBetAway, unibetBetAway, superbetMatch, unibetMatch);
            } else {
                processMatchingBets(matchNameSuperbet, superbetBetName, unibetBetName, superbetMatch, unibetMatch);
            }
        }
    }

    private String[] extractTeams(String matchName) {
        if (matchName.contains("·")) {
            return matchName.split("·");
        } else if (matchName.contains(" - ")) {
            return matchName.split(" - ");
        }
        return null;
    }

    private void processMatchingBets(String matchName,
                                     String superbetBetName,
                                     String unibetBetName,
                                     Map<String, List<AbstractMap.SimpleEntry<String, Double>>> superbetMatch,
                                     Map<String, Map<String, String>> unibetMatch) {

        if (superbetMatch.containsKey(superbetBetName) && unibetMatch.containsKey(unibetBetName)) {
            List<AbstractMap.SimpleEntry<String, Double>> superbetBets = superbetMatch.get(superbetBetName);
            Map<String, String> unibetBets = unibetMatch.get(unibetBetName);

            for (AbstractMap.SimpleEntry<String, Double> superbetBet : superbetBets) {
                String superbetBetKey = superbetBet.getKey();
                Double superbetOdds = superbetBet.getValue();

                String oppositeBetKey = getOppositeBetKey(superbetBetKey);

                if (oppositeBetKey != null && unibetBets.containsKey(oppositeBetKey)) {
                    String unibetOddsString = unibetBets.get(oppositeBetKey);
                    Double unibetOdds = unibetOddsString != null ? Double.parseDouble(unibetOddsString) : null;

                    if (unibetOdds != null) {
                        double arbitrage = calculateArbitrage(superbetOdds, unibetOdds);

                        if (arbitrage < 97.0) {
                            System.out.println("Match: " + matchName);
                            System.out.println("Category: " + superbetBetName);
                            System.out.println("Arbitrage Opportunity Detected!");
                            System.out.println("Matching Bet Pair:");
                            System.out.println("Superbet: " + superbetBetKey + " - Odds: " + superbetOdds);
                            System.out.println("Unibet: " + oppositeBetKey + " - Odds: " + unibetOdds);
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