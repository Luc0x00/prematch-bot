package org.example;

import org.example.models.MatchPair;
import org.example.sites.BettingSite;
import org.example.sites.BettingSiteFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws Throwable {
        List<BettingSite> bettingSites = BettingSiteFactory.getAllBettingSites();
        Set<String> sentMessages = new HashSet<>();

        while (true) {
            for (int i = 0; i < bettingSites.size(); i++) {
                for (int j = i + 1; j < bettingSites.size(); j++) {

                    BettingSite site1 = bettingSites.get(i);
                    BettingSite site2 = bettingSites.get(j);

                    MatchingGames matchingGames = new MatchingGames(site1, site2, sentMessages);
                    Map<MatchPair, Integer[]> similarMatches = matchingGames.getSimilarMatches();

                    for (Map.Entry<MatchPair, Integer[]> entry : similarMatches.entrySet()) {
                        MatchPair matchPair = entry.getKey();
                        Integer[] ids = entry.getValue();

                        Integer site1MatchId = ids[0];
                        Integer site2MatchId = ids[1];

                        String site1Response = site1.getMatchContent(site1MatchId);
                        String site2Response = site2.getMatchContent(site2MatchId);

                        matchingGames.matchingSameBets(
                                matchPair.firstSiteGame(),
                                matchPair.secondSiteGame(),
                                site1.getMatchMarkets(site1Response),
                                site2.getMatchMarkets(site2Response)
                        );
                    }
                }
            }
        }
    }
}
