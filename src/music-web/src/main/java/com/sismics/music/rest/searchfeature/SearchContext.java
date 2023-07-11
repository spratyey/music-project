package com.sismics.music.rest.searchfeature;

import javax.json.JsonObject;

class SearchContext {
    private SearchStrategy searchStrategy;

    public SearchContext(SearchStrategy searchStrategy) {
        this.searchStrategy = searchStrategy;
    }

    public JsonObject search(String trackName) {
        JsonObject searchResult = searchStrategy.search(trackName);
        return searchResult;
    }
}
