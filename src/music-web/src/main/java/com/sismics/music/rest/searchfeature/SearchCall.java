package com.sismics.music.rest.searchfeature;

import com.sismics.music.rest.searchfeature.strategies.SpotifySearch;

import javax.json.JsonObject;

import com.sismics.music.rest.searchfeature.strategies.LastfmSearch;

// import com.sismics.music.core.dao.dbi.AlbumDao;
// import com.sismics.music.core.dao.dbi.ArtistDao;
// import com.sismics.music.core.dao.dbi.TrackDao;
// import com.sismics.music.core.dao.dbi.criteria.AlbumCriteria;
// import com.sismics.music.core.dao.dbi.criteria.ArtistCriteria;
// import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
// import com.sismics.music.core.dao.dbi.dto.AlbumDto;
// import com.sismics.music.core.dao.dbi.dto.ArtistDto;
// import com.sismics.music.core.dao.dbi.dto.TrackDto;
// import com.sismics.music.core.util.dbi.PaginatedList;
// import com.sismics.music.core.util.dbi.PaginatedLists;
// import com.sismics.music.rest.util.JsonUtil;
// import com.sismics.rest.exception.ForbiddenClientException;
// import com.sismics.rest.util.Validation;

// import javax.json.Json;
// import javax.json.JsonArrayBuilder;
// import javax.json.JsonObjectBuilder;
// import javax.ws.rs.GET;
// import javax.ws.rs.Path;
// import javax.ws.rs.PathParam;
// import javax.ws.rs.QueryParam;
// import javax.ws.rs.core.Response;
// import java.util.List;

public class SearchCall {
    public JsonObject searchMain(int strategyID, String trackName) {
        // strategyID = 0 for Spotify
        // strategyID = 1 for LastFM

        SearchStrategy searchStrategy;

        if(strategyID == 0) {
            searchStrategy = new SpotifySearch();
        }
        else if(strategyID == 1) {
            searchStrategy = new LastfmSearch();
        }
        else {
            System.out.println("Incorrect value for StrategyID");
            return null;
        }
        
        SearchContext searchContext = new SearchContext(searchStrategy);

        JsonObject searchResult = searchContext.search(trackName);

        System.out.println("Searched list: " + trackName);

        return searchResult;
    }   
}
