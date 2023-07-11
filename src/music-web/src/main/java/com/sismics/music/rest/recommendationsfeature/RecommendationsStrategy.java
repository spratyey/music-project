package com.sismics.music.rest.recommendationsfeature;

import javax.json.JsonObject;

public interface RecommendationsStrategy {
    public JsonObject getRecommendations(String playlistId);
}
