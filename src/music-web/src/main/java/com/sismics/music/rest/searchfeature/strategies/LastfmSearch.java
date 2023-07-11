package com.sismics.music.rest.searchfeature.strategies;

import com.sismics.music.rest.searchfeature.SearchStrategy;
import com.sismics.music.core.model.context.AppContext;
import com.sismics.music.core.service.lastfm.LastFmService;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonArrayBuilder;
import java.util.Collection;
import de.umass.lastfm.*;

public class LastfmSearch implements SearchStrategy {
    public JsonObject search(String trackName) {
        // Implement lastfm search
        // System.out.println("Searching using LastFm");
        final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
        Collection<Track> result = lastFmService.searchTrack(trackName, 10);
        JsonArrayBuilder trackArray = Json.createArrayBuilder();
        // System.out.println("result: " + result);
        for (Track track : result) {
            trackArray.add(Json.createObjectBuilder()
                    .add("name", track.getName())
                    .add("artist", track.getArtist())
            );
        }

        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("tracks", trackArray)
                .build();
        System.out.println("response: " + response);
        // return the response
        return response;
        // return Response.ok().entity(response).build();
    }
}