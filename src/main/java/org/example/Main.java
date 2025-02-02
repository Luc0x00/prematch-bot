package org.example;

import org.example.models.MatchPair;
import org.example.sites.SuperbetController;
import org.example.sites.UnibetController;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Throwable {
        SuperbetController superbet = new SuperbetController();
        UnibetController unibet = new UnibetController();

        while (true) {

            MatchingGames matchingGames = new MatchingGames(superbet, unibet);
            Map<MatchPair, Integer[]> similarMatches = matchingGames.getSimilarMatches();

            for (Map.Entry<MatchPair, Integer[]> entry : similarMatches.entrySet()) {
                MatchPair matchPair = entry.getKey();
                Integer[] ids = entry.getValue();

                Integer superbetMatchId = ids[0];
                Integer unibetMatchId = ids[1];

                String superbetResponse = superbet.getMatchContent(superbetMatchId);
                String unibetResponse = unibet.getMatchContent(unibetMatchId);

                matchingGames.matchingSameBets(matchPair.firstSiteGame(), matchPair.secondSiteGame(), superbet.getMatchMarkets(superbetResponse), unibet.getMatchMarkets(unibetResponse));
            }
        }
    }
}
