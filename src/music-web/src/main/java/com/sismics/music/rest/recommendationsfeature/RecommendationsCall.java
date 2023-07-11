package com.sismics.music.rest.recommendationsfeature;

import com.sismics.music.rest.recommendationsfeature.strategies.SpotifyRecommendations;
import com.sismics.music.rest.recommendationsfeature.strategies.LastfmRecommendations;

import javax.json.JsonObject;

public class RecommendationsCall {
    public JsonObject getRecommendationsMain(int strategyID, String playlistId) {
        // strategyID = 0 for Spotify
        // strategyID = 1 for LastFM

        RecommendationsStrategy recommendationsStrategy;

        if(strategyID == 0) {
            recommendationsStrategy = new SpotifyRecommendations();
        }
        else if(strategyID == 1) {
            recommendationsStrategy = new LastfmRecommendations();
        }
        else {
            System.out.println("Incorrect value for StrategyID");
            return null;
        }
        
        RecommendationsContext recommendationsContext = new RecommendationsContext(recommendationsStrategy);

        JsonObject recommendationsResult = recommendationsContext.getRecommendations(playlistId);

        System.out.println("Searched list: " + playlistId);

        return recommendationsResult;
    } 
}
