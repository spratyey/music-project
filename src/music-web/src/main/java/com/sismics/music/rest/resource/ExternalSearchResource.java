package com.sismics.music.rest.resource;

import com.sismics.rest.exception.ForbiddenClientException;
// import de.umass.lastfm.Album;
// import de.umass.lastfm.ImageSize;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.json.JsonObject;

import com.sismics.music.rest.searchfeature.*;

/**
 * Album art REST resources.
 * 
 * @author jtremeaux
 */
@Path("/external")
public class ExternalSearchResource extends BaseResource {
    /**
     * Search Spotify tracks
     *
     * @param query The query
     * @return Response
     */
    @GET
    @Path("spotify-search")
    public JsonObject spotifySearch(
            @QueryParam("query") String query) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        SearchCall call= new SearchCall();
        JsonObject response = call.searchMain(0,query); 

        return response;
    }



    /**
     * Search LastFm tracks
     *
     * @param query The query
     * @return Response
     */
    @GET
    @Path("lastfm-search")
    public JsonObject lastfmSearch(
            @QueryParam("query") String query) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the album arts
        // JsonObjectBuilder response = Json.createObjectBuilder();
        // JsonArray items = Json.createArrayBuilder().add("track1")
        // .add("track2")
        // .add("track3")
        // .add("track4")
        // .build();
        // return renderJson(response);
        SearchCall call= new SearchCall();
        JsonObject response = call.searchMain(1,query); 
        // System.out.println("hello from the other sidee");
        return response;
    }
}
