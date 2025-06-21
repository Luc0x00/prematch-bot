package org.example;

import org.example.models.MatchPair;
import org.example.sites.BettingSite;
import org.example.sites.BettingSiteFactory;

import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        List<BettingSite> bettingSites = BettingSiteFactory.getAllBettingSites();
        Set<String> sentMessages = ConcurrentHashMap.newKeySet();

        int threadCount = Math.max(2, Runtime.getRuntime().availableProcessors() * 2);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(threadCount);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }));

        scheduler.scheduleAtFixedRate(() -> {
            for (int i = 0; i < bettingSites.size(); i++) {
                for (int j = i + 1; j < bettingSites.size(); j++) {
                    BettingSite site1 = bettingSites.get(i);
                    BettingSite site2 = bettingSites.get(j);

                    scheduler.submit(() -> {
                        try {
                            MatchingGames matchingGames = new MatchingGames(site1, site2, sentMessages);
                            Map<MatchPair, Integer[]> similarMatches = matchingGames.getSimilarMatches();

                            for (Map.Entry<MatchPair, Integer[]> entry : similarMatches.entrySet()) {
                                scheduler.submit(() -> {
                                    try {
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
                                    } catch (Throwable e) {
                                        System.err.println("Error comparing match from " + site1.getSiteName() + " and " + site2.getSiteName());
                                    }
                                });
                            }
                        } catch (Throwable e) {
                            System.err.println("Error comparing " + site1.getSiteName() + " vs " + site2.getSiteName());
                        }
                    });
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Clearing sentMessages...");
            sentMessages.clear();
        }, 2, 2, TimeUnit.HOURS);
    }
}
