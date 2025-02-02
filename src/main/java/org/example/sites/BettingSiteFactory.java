package org.example.sites;

import java.util.ArrayList;
import java.util.List;

public class BettingSiteFactory {
    private static final List<BettingSite> bettingSites = new ArrayList<>();

    static {
        bettingSites.add(new SuperbetController());
        bettingSites.add(new UnibetController());
    }

    public static List<BettingSite> getAllBettingSites() {
        return bettingSites;
    }
}