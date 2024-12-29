package org.example;

import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MatchingGames {

    private final ArrayList<String> superbet;
    private final ArrayList<String> unibet;
    private final ArrayList<Integer> superbetMatchIds;
    private final ArrayList<Integer> unibetMatchIds;
    private final Map<String, Integer[]> matchingGames;

    public MatchingGames(ArrayList<String> superbet, ArrayList<String> unibet,
                         ArrayList<Integer> superbetMatchIds, ArrayList<Integer> unibetMatchIds) {
        this.superbet = superbet;
        this.unibet = unibet;
        this.superbetMatchIds = superbetMatchIds;
        this.unibetMatchIds = unibetMatchIds;
        this.matchingGames = new HashMap<>();
    }

    public Map<String, Integer[]> getSimilarMatches() {
        for (int i = 0; i < superbet.size(); i++) {
            String superbetGame = superbet.get(i);
            if (superbetGame.contains("·")) {
                String[] superbetTeams = superbetGame.split("·");
                for (int j = 0; j < unibet.size(); j++) {
                    String unibetGame = unibet.get(j);
                    if (unibetGame.contains("-")) {
                        String[] unibetTeams = unibetGame.split(" - ");
                        if (areTeamsMatching(superbetTeams, unibetTeams)) {
                            matchingGames.put(superbetGame, new Integer[]{superbetMatchIds.get(i), unibetMatchIds.get(j)});
                        }
                    }
                }
            }
        }
        return matchingGames;
    }

    public static boolean areTeamsMatching(String[] superbetTeams, String[] unibetTeams) {
        String superbetTeam1 = superbetTeams[0];
        String superbetTeam2 = superbetTeams[1];
        String unibetTeam1 = unibetTeams[0];
        String unibetTeam2 = unibetTeams[1];
        JaroWinklerDistance jaroWinkler = new JaroWinklerDistance();
        double threshold = 0.70;

        boolean isFirstTeamMatch = jaroWinkler.apply(superbetTeam1, unibetTeam1) >= threshold;
        boolean isSecondTeamMatch = jaroWinkler.apply(superbetTeam2, unibetTeam2) >= threshold;

        return (isFirstTeamMatch && isSecondTeamMatch) ||
                (jaroWinkler.apply(superbetTeam1, unibetTeam2) >= threshold &&
                        jaroWinkler.apply(superbetTeam2, unibetTeam1) >= threshold);
    }
}
