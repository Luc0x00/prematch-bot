package org.example;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Throwable {
        SuperbetController superbet = new SuperbetController();
        UnibetController unibet = new UnibetController();

        while (true) {

            String matchesResponseSuperbet = superbet.getAllMatchesContent();
            Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapSuperbet = superbet.getMatchesInformation(matchesResponseSuperbet);

            String matchesResponseUnibet = unibet.getAllMatchesContent();
            Map<Integer, List<AbstractMap.SimpleEntry<String, Integer>>> matchesMapUnibet = unibet.getMatchesInformation(matchesResponseUnibet);

            MatchingGames matchingGames = new MatchingGames(matchesMapSuperbet, matchesMapUnibet);
            Map<MatchPair, Integer[]> similarMatches = matchingGames.getSimilarMatches();

            for (Map.Entry<MatchPair, Integer[]> entry : similarMatches.entrySet()) {
                MatchPair matchPair = entry.getKey();
                Integer[] ids = entry.getValue();

                Integer superbetMatchId = ids[0];
                Integer unibetMatchId = ids[1];

                String superbetResponse = superbet.getMatchContent(superbetMatchId);
                String unibetResponse = unibet.getMatchContent(unibetMatchId);

                matchingGames.matchingSameBets(matchPair.superbetGame(), matchPair.unibetGame(), superbet.getMatchMarkets(superbetResponse), unibet.getMatchMarkets(unibetResponse));
            }
        }
    }
}
