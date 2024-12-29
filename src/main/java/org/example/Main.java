package org.example;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws Throwable {

        SuperbetController superbet = new SuperbetController();
        UnibetController unibet = new UnibetController();

        try (ExecutorService superbetExecutor = Executors.newFixedThreadPool(8);
             ExecutorService unibetExecutor = Executors.newFixedThreadPool(8)) {

            String matchesResponseSuperbet = superbet.getLiveMatchesContent();
            ArrayList<Integer> matchesIdSuperbet = superbet.getMatchesId(matchesResponseSuperbet);
            ArrayList<String> superbetMatches = new ArrayList<>();
            ArrayList<Future<String>> superbetTasks = new ArrayList<>();

            for (Integer matchId : matchesIdSuperbet) {
                Future<String> task = superbetExecutor.submit(() -> {
                    String matchResponse = null;
                    try {
                        matchResponse = superbet.getMatchContent(matchId);
                    } catch (Throwable ignored) {
                        //no-op
                    }
                    return superbet.getMatchName(matchResponse);
                });
                superbetTasks.add(task);
            }

            for (Future<String> task : superbetTasks) {
                try {
                    if (task.get() != null) {
                        superbetMatches.add(task.get());
                    }
                } catch (InterruptedException | ExecutionException ignored) {
                    //no-op
                }
            }

            String matchesResponseUnibet = unibet.getLiveMatchesContent();
            ArrayList<Integer> matchesIdUnibet = unibet.getLiveMatchesId(matchesResponseUnibet);
            ArrayList<String> unibetMatches = new ArrayList<>();
            ArrayList<Future<String>> unibetTasks = new ArrayList<>();

            for (Integer matchId : matchesIdUnibet) {
                Future<String> task = unibetExecutor.submit(() -> {
                    String matchResponse = null;
                    try {
                        matchResponse = unibet.getLiveMatchContent(matchId);
                    } catch (Throwable ignored) {
                        //no-op
                    }
                    return unibet.getMatchName(matchResponse);
                });
                unibetTasks.add(task);
            }

            for (Future<String> task : unibetTasks) {
                try {
                    if (task.get() != null) {
                        unibetMatches.add(task.get());
                    }
                } catch (InterruptedException | ExecutionException ignored) {
                    //no-op
                }
            }

            MatchingGames matchingGames = new MatchingGames(superbetMatches, unibetMatches, matchesIdSuperbet, matchesIdUnibet);
            Map<String, Integer[]> similarMatches = matchingGames.getSimilarMatches();
            for (Map.Entry<String, Integer[]> entry : similarMatches.entrySet()) {
                String superbetGame = entry.getKey();
                Integer[] matchIds = entry.getValue();
                Integer superbetMatchId = matchIds[0];
                Integer unibetMatchId = matchIds[1];

                System.out.println("Superbet Game: " + superbetGame);

                System.out.println("Superbet");

                String superbetResponse = superbet.getMatchContent(superbetMatchId);
                Map<String, Map<String, String>> marketMap = superbet.getMatchMarkets(superbetResponse);
                System.out.println(marketMap);

                System.out.println("Unibet");

                String unibetResponse = unibet.getLiveMatchContent(unibetMatchId);
                System.out.println(unibet.getMatchMarkets(unibetResponse));
                System.out.println();
            }
        }
    }
}
