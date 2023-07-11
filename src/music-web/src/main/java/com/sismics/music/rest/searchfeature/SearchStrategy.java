package com.sismics.music.rest.searchfeature;

import javax.json.JsonObject;

public interface SearchStrategy {
    public JsonObject search(String trackName);
}