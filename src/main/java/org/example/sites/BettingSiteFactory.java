package org.example.sites;

import java.util.ArrayList;
import java.util.List;

public class BettingSiteFactory {
    private static final List<BettingSite> bettingSites = new ArrayList<>();

    static {
        bettingSites.add(new SuperbetController());
        bettingSites.add(new BetanoController());
        bettingSites.add(new UnibetController());
        bettingSites.add(new WinnerController());
    }

    public static List<BettingSite> getAllBettingSites() {
        return bettingSites;
    }
}