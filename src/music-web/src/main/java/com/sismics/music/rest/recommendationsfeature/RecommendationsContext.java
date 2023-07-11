package com.sismics.music.rest.recommendationsfeature;

import javax.json.JsonObject;

class RecommendationsContext {
    private RecommendationsStrategy recommendationsStrategy;

    public RecommendationsContext(RecommendationsStrategy recommendationsStrategy) {
        this.recommendationsStrategy = recommendationsStrategy;
    }

    public JsonObject getRecommendations(String playlistID) {
        JsonObject recommendationsResult = recommendationsStrategy.getRecommendations(playlistID);
        return recommendationsResult;
    }
}
