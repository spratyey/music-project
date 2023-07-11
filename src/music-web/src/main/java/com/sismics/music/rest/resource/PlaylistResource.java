package com.sismics.music.rest.resource;

import com.sismics.music.core.dao.dbi.PlaylistDao;
import com.sismics.music.core.dao.dbi.PlaylistTrackDao;
import com.sismics.music.core.dao.dbi.TrackDao;
import com.sismics.music.core.dao.dbi.criteria.PlaylistCriteria;
import com.sismics.music.core.dao.dbi.criteria.TrackCriteria;
import com.sismics.music.core.dao.dbi.dto.PlaylistDto;
import com.sismics.music.core.dao.dbi.dto.TrackDto;
import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.music.core.model.dbi.PlaylistTrack;
import com.sismics.music.core.model.dbi.Track;
import com.sismics.music.core.util.dbi.PaginatedList;
import com.sismics.music.core.util.dbi.PaginatedLists;
import com.sismics.music.core.util.dbi.SortCriteria;
import com.sismics.music.rest.util.JsonUtil;
import com.sismics.rest.exception.ClientException;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.rest.exception.ServerException;
import com.sismics.rest.util.Validation;

import com.sismics.music.core.service.lastfm.LastFmService;
import com.sismics.music.core.model.context.AppContext;
import java.util.Collection;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.net.URL;
import javax.json.JsonReader;
import com.sismics.music.core.constant.PlaylistVisibilityEnum;

/**
 * Playlist REST resources.
 * 
 * @author jtremeaux
 */
@Path("/playlist")
public class PlaylistResource extends BaseResource {
    public static final String DEFAULt_playlist = "default";

    /**
     * Create a named playlist.
     *
     * @param name The name
     * @return Response
     */
    @PUT
    public Response createPlaylist(
            @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        Validation.required(name, "name");

        // Create the playlist
        Playlist playlist = new Playlist();
        playlist.setUserId(principal.getId());
        playlist.setName(name);
        Playlist.createPlaylist(playlist);

        // Output the playlist
        return renderJson(Json.createObjectBuilder()
                .add("item", Json.createObjectBuilder()
                        .add("id", playlist.getId())
                        .add("name", playlist.getName())
                        .add("trackCount", 0)
                        .add("userTrackPlayCount", 0)
                        .build()));
    }

    /**
     * Update a named playlist.
     *
     * @param name The name
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}")
    public Response updatePlaylist(
            @PathParam("id") String playlistId,
            @FormParam("name") String name) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        Validation.required(playlistId, "id");
        Validation.required(name, "name");

        // Get the playlist
        PlaylistDto playlistDto = new PlaylistDao().findFirstByCriteria(new PlaylistCriteria()
                .setUserId(principal.getId())
                .setDefaultPlaylist(false)
                .setId(playlistId));
        notFoundIfNull(playlistDto, "Playlist: " + playlistId);

        // Update the playlist
        Playlist playlist = new Playlist(playlistDto.getId());
        playlist.setName(name);
        Playlist.updatePlaylist(playlist);

        // Output the playlist
        return Response.ok()
                .build();
    }

    /**
     * Inserts a track in the playlist.
     *
     * @param playlistId Playlist ID
     * @param trackId Track ID
     * @param order Insert at this order in the playlist
     * @param clear If true, clear the playlist
     * @return Response
     */
    @PUT
    @Path("{id: [a-z0-9\\-]+}")
    public Response insertTrack(
            @PathParam("id") String playlistId,
            @FormParam("id") String trackId,
            @FormParam("order") Integer order,
            @FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the track
        Track track = new TrackDao().getActiveById(trackId);
        notFoundIfNull(track, "Track: " + trackId);

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                .setUserId(principal.getId())
                ;
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        // System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        // System.out.println("Trying to add to playlist: " + playlist.getName() );
        notFoundIfNull(playlist, "Playlist: " + playlistId);
        // System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        // System.out.println("Trying to add to playlist: " + playlist.getName() );
        // // Check if the playlist is owned by the user currently logged in
        // if (!(playlist.getVisibility()==PlaylistVisibilityEnum.COLLAB) || !principal.getId().equals(playlist.getUserId())) {
        //     System.out.println("Playlist is not owned by the user currently logged in");
        //     throw new ForbiddenClientException();
        // }

        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        if (clear != null && clear) {
            // Delete all tracks in the playlist
            playlistTrackDao.deleteByPlaylistId(playlist.getId());
        }

        // Get the track order
        if (order == null) {
            order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        }

        // Insert the track into the playlist
        playlistTrackDao.insertPlaylistTrack(playlist.getId(), track.getId(), order);

        // Output the playlist
        return renderJson(buildPlaylistJson(playlist));
    }

    /**
     * Inserts tracks in the playlist.
     *
     * @param playlistId Playlist ID
     * @param idList List of track ID
     * @param clear If true, clear the playlist
     * @return Response
     */
    @PUT
    @Path("{id: [a-z0-9\\-]+}/multiple")
    public Response insertTracks(
            @PathParam("id") String playlistId,
            @FormParam("ids") List<String> idList,
            @FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        Validation.required(idList, "ids");

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                // .setUserId(principal.getId())
                ;
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlist, "Playlist: " + playlistId);
        // System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
        // System.out.println("Trying to add to playlist: " + playlist.getName() );
        // System.out.println("Visibility: " + playlist.getVisibility().toString());
        // Check if the playlist is owned by the user currently logged in
        if (!(playlist.getVisibility()==PlaylistVisibilityEnum.COLLAB) &&  !principal.getId().equals(playlist.getUserId())) {
            System.out.println("Playlist is not owned by the user currently logged in");
            throw new ForbiddenClientException();
        }

        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        if (clear != null && clear) {
            // Delete all tracks in the playlist
            playlistTrackDao.deleteByPlaylistId(playlist.getId());
        }
        
        // Get the track order
        int order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        
        for (String id : idList) {
            // Load the track
            TrackDao trackDao = new TrackDao();
            Track track = trackDao.getActiveById(id);
            if (track == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
            // Insert the track into the playlist
            playlistTrackDao.insertPlaylistTrack(playlist.getId(), track.getId(), order++);
        }

        // Output the playlist
        return renderJson(buildPlaylistJson(playlist));
    }
    
    /**
     * Load a named playlist into the default playlist.
     *
     * @param playlistId Playlist ID
     * @param clear If true, clear the default playlist
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/load")
    public Response loadPlaylist(
            @PathParam("id") String playlistId,
            @FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        Validation.required(playlistId, "id");

        // Get the named playlist
        PlaylistDto namedPlaylist = new PlaylistDao().findFirstByCriteria(new PlaylistCriteria()
                .setUserId(principal.getId())
                .setDefaultPlaylist(false)
                .setId(playlistId));
        notFoundIfNull(namedPlaylist, "Playlist: " + playlistId);

        // Get the default playlist
        PlaylistDto defaultPlaylist = new PlaylistDao().getDefaultPlaylistByUserId(principal.getId());
        if (defaultPlaylist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Default playlist not found for user {0}", principal.getId()));
        }

        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        if (clear != null && clear) {
            // Delete all tracks in the default playlist
            playlistTrackDao.deleteByPlaylistId(defaultPlaylist.getId());
        }

        // Get the track order
        int order = playlistTrackDao.getPlaylistTrackNextOrder(namedPlaylist.getId());

        // Insert the tracks into the playlist
        List<TrackDto> trackList = new TrackDao().findByCriteria(new TrackCriteria()
                .setUserId(principal.getId())
                .setPlaylistId(namedPlaylist.getId()));
        for (TrackDto trackDto : trackList) {
            PlaylistTrack playlistTrack = new PlaylistTrack();
            playlistTrack.setPlaylistId(defaultPlaylist.getId());
            playlistTrack.setTrackId(trackDto.getId());
            playlistTrack.setOrder(order++);
            PlaylistTrack.createPlaylistTrack(playlistTrack);
        }

        // Output the playlist
        return renderJson(buildPlaylistJson(defaultPlaylist));
    }

    /**
     * Start or continue party mode.
     * Adds some good tracks.
     * 
     * @param clear If true, clear the playlist
     * @return Response
     */
    @POST
    @Path("party")
    public Response party(@FormParam("clear") Boolean clear) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // Get the default playlist
        PlaylistDto playlist = new PlaylistDao().getDefaultPlaylistByUserId(principal.getId());
        if (playlist == null) {
            throw new ServerException("UnknownError", MessageFormat.format("Default playlist not found for user {0}", principal.getId()));
        }

        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        if (clear != null && clear) {
            // Delete all tracks in the playlist
            playlistTrackDao.deleteByPlaylistId(playlist.getId());
        }
        
        // Get the track order
        int order = playlistTrackDao.getPlaylistTrackNextOrder(playlist.getId());
        
        // TODO Add prefered tracks
        // Add random tracks
        PaginatedList<TrackDto> paginatedList = PaginatedLists.create();
        new TrackDao().findByCriteria(paginatedList, new TrackCriteria().setRandom(true), null, null);
        
        for (TrackDto trackDto : paginatedList.getResultList()) {
            // Insert the track into the playlist
            playlistTrackDao.insertPlaylistTrack(playlist.getId(), trackDto.getId(), order++);
        }
        
        // Output the playlist
        return renderJson(buildPlaylistJson(playlist));
    }

    /**
     * Move the track to another position in the playlist.
     *
     * @param playlistId Playlist ID
     * @param order Current track order in the playlist
     * @param newOrder New track order in the playlist
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/{order: [0-9]+}/move")
    public Response moveTrack(
            @PathParam("id") String playlistId,
            @PathParam("order") Integer order,
            @FormParam("neworder") Integer newOrder) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        Validation.required(order, "order");
        Validation.required(newOrder, "neworder");

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                .setUserId(principal.getId());
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlist, "Playlist: " + playlistId);

        // Remove the track at the current order from playlist
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        String trackId = playlistTrackDao.removePlaylistTrack(playlist.getId(), order);
        if (trackId == null) {
            throw new ClientException("TrackNotFound", MessageFormat.format("Track not found at position {0}", order));
        }

        // Insert the track at the new order into the playlist
        playlistTrackDao.insertPlaylistTrack(playlist.getId(), trackId, newOrder);

        // Output the playlist
        return renderJson(buildPlaylistJson(playlist));
    }

    /**
     * Remove a track from the playlist.
     *
     * @param playlistId Playlist ID
     * @param order Current track order in the playlist
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}/{order: [0-9]+}")
    public Response delete(
            @PathParam("id") String playlistId,
            @PathParam("order") Integer order) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        Validation.required(order, "order");

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                .setUserId(principal.getId());
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlist, "Playlist: " + playlistId);

        // Remove the track at the current order from playlist
        PlaylistTrackDao playlistTrackDao = new PlaylistTrackDao();
        String trackId = playlistTrackDao.removePlaylistTrack(playlist.getId(), order);
        if (trackId == null) {
            throw new ClientException("TrackNotFound", MessageFormat.format("Track not found at position {0}", order));
        }

        // Output the playlist
        return renderJson(buildPlaylistJson(playlist));
    }

    /**
     * Delete a named playlist.
     *
     * @param playlistId Playlist ID
     * @return Response
     */
    @DELETE
    @Path("{id: [a-z0-9\\-]+}")
    public Response deletePlaylist(
            @PathParam("id") String playlistId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlist
        PlaylistDto playlistDto = new PlaylistDao().findFirstByCriteria(new PlaylistCriteria()
                .setDefaultPlaylist(false)
                .setUserId(principal.getId())
                .setId(playlistId));
        notFoundIfNull(playlistDto, "Playlist: " + playlistId);

        // Delete the playlist
        Playlist playlist = new Playlist(playlistDto.getId());
        Playlist.deletePlaylist(playlist);

        // Output the playlist
        return Response.ok()
                .build();
    }

    /**
     * Returns all named playlists.
     *
     * @return Response
     */
    @GET
    public Response listPlaylist(
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("sort_column") Integer sortColumn,
            @QueryParam("asc") Boolean asc) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlists
        PaginatedList<PlaylistDto> paginatedList = PaginatedLists.create(limit, offset);
        SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
        new PlaylistDao().findByCriteria(paginatedList, new PlaylistCriteria()
                .setDefaultPlaylist(false)
                // .setUserId(principal.getId())
                , sortCriteria, null);
        
        // Print the contents of paginatedList
        // System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        // System.out.println(paginatedList.getResultList().size());

        // Output the list
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder items = Json.createArrayBuilder();
        for (PlaylistDto playlist : paginatedList.getResultList()) {
            // System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            // System.out.println(playlist.getVisibility());
            // System.out.println("Playlist created by: "+playlist.getUserId());
            // System.out.println("Current user: "+principal.getId());
            // check if the playlist is public or if the playlist.user_id is the same as the current user
            if (!(playlist.getVisibility() == PlaylistVisibilityEnum.PRIVATE) || playlist.getUserId().equals(principal.getId())) {
                items.add(Json.createObjectBuilder()
                        .add("id", playlist.getId())
                        .add("name", playlist.getName())
                        .add("trackCount", playlist.getPlaylistTrackCount())
                        .add("userTrackPlayCount", playlist.getUserTrackPlayCount()));
            }
            // items.add(Json.createObjectBuilder()
            //         .add("id", playlist.getId())
            //         .add("name", playlist.getName())
            //         .add("trackCount", playlist.getPlaylistTrackCount())
            //         .add("userTrackPlayCount", playlist.getUserTrackPlayCount()));
        }

        response.add("total", paginatedList.getResultCount());
        response.add("items", items);
        JsonArray actual= items.build();
        // System.out.println("Returning: " + renderJson(response));
        // print the items in the list of items
        for (int i = 0; i < actual.size(); i++) {
            System.out.println("Item: " + actual.get(i));
        }
        System.out.println("Returning: " );
        return renderJson(response);
    }

    /**
     * Returns all tracks in the playlist.
     *
     * @return Response
     */
    @GET
    @Path("{id: [a-z0-9\\-]+}")
    public Response listTrack(
            @PathParam("id") String playlistId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                // .setUserId(principal.getId())
                ;
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlist, "Playlist: " + playlistId);

        // Output the playlist
        // System.out.println("Playlist Contents: " + buildPlaylistJson(playlist).build());
        return renderJson(buildPlaylistJson(playlist));
    }

    /**
     * Removes all tracks from the playlist.
     *
     * @param playlistId Playlist ID
     * @return Response
     */
    @POST
    @Path("{id: [a-z0-9\\-]+}/clear")
    public Response clear(
            @PathParam("id") String playlistId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                .setUserId(principal.getId());
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlist, "Playlist: " + playlistId);

        // Delete all tracks in the playlist
        new PlaylistTrackDao().deleteByPlaylistId(playlist.getId());

        // Always return OK
        return okJson();
    }
    
    /**
     * Build the JSON output of a playlist.
     * 
     * @param playlist Playlist
     * @return JSON
     */
    private JsonObjectBuilder buildPlaylistJson(PlaylistDto playlist) {
        JsonObjectBuilder response = Json.createObjectBuilder();
        JsonArrayBuilder tracks = Json.createArrayBuilder();
        TrackDao trackDao = new TrackDao();
        List<TrackDto> trackList = trackDao.findByCriteria(new TrackCriteria()
                .setUserId(principal.getId())
                .setPlaylistId(playlist.getId()));
        int i = 0;
        for (TrackDto trackDto : trackList) {
            tracks.add(Json.createObjectBuilder()
                    .add("order", i++)
                    .add("id", trackDto.getId())
                    .add("title", trackDto.getTitle())
                    .add("year", JsonUtil.nullable(trackDto.getYear()))
                    .add("genre", JsonUtil.nullable(trackDto.getGenre()))
                    .add("length", trackDto.getLength())
                    .add("bitrate", trackDto.getBitrate())
                    .add("vbr", trackDto.isVbr())
                    .add("format", trackDto.getFormat())
                    .add("play_count", trackDto.getUserTrackPlayCount())
                    .add("liked", trackDto.isUserTrackLike())

                    .add("artist", Json.createObjectBuilder()
                            .add("id", trackDto.getArtistId())
                            .add("name", trackDto.getArtistName()))
                    
                    .add("album", Json.createObjectBuilder()
                            .add("id", trackDto.getAlbumId())
                            .add("name", trackDto.getAlbumName())
                            .add("albumart", trackDto.getAlbumArt() != null)));
        }
        response.add("tracks", tracks);
        response.add("id", playlist.getId());
        if (playlist.getName() != null) {
            response.add("name", playlist.getName());
        }
        return response;
    }

    // function to change the visibility of a playlist
    @POST
    @Path("{id: [a-z0-9\\-]+}/visibility")
    public Response changeVisibility(
            @PathParam("id") String playlistId,
            @FormParam("visibility") String visibility) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        // System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        // System.out.println(visibility);
        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                .setUserId(principal.getId());
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlistDto = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlistDto, "Playlist: " + playlistId);
        visibility=visibility.toUpperCase();
        Playlist playlist=new Playlist(playlistDto.getId(),playlistDto.getUserId());
        System.out.println("User logged in: " + principal.getId());
        System.out.println("Playlist creator: " + playlist.getUserId());
        System.out.println("Playlist visibility: " + playlist.getVisibility());
        System.out.println("Visibility to be changed to: " + visibility);
        if(playlist.getVisibility().equals(visibility)) {
            return okJson();
        }
        playlist.setVisibility(PlaylistVisibilityEnum.valueOf(visibility));
        System.out.println("Visibility changed to: "+ playlist.getVisibility().toString());
        new PlaylistDao().updateVisibility(playlist);
        

        // Always return OK
        return okJson();
    }
    
    // This function returns a JsonObject containing the tracks in the playlist
    public JsonObject playlistTracks(@PathParam("id") String playlistId) {
        // Get the playlist
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }

        // Get the playlist
        PlaylistCriteria criteria = new PlaylistCriteria()
                .setUserId(principal.getId());
        if (DEFAULt_playlist.equals(playlistId)) {
            criteria.setDefaultPlaylist(true);
        } else {
            criteria.setDefaultPlaylist(false);
            criteria.setId(playlistId);
        }
        PlaylistDto playlist = new PlaylistDao().findFirstByCriteria(criteria);
        notFoundIfNull(playlist, "Playlist: " + playlistId);

        // Output the playlist
        // System.out.println("Playlist Contents: " + buildPlaylistJson(playlist).build());
        /* 
         * This Json is of the form
         * {"tracks":[{"order":0,"id":"a4936196-b0f0-4483-9fba-f77b939ccb20","title":"Industry Baby","year":0,"genre":null,"length":212,"bitrate":44100,"vbr":false,"format":"mp3","play_count":1,"liked":false,"artist":{"id":"18740b3c-9d58-4f67-953c-cf4879a652ec","name":"Lil Nas X"},"album":{"id":"15b77dd2-5211-49c3-9b40-043544652a97","name":"Montero","albumart":false}},{"order":1,"id":"d4ffc849-ec13-48b7-9762-41db9a85f926","title":"Believer","year":0,"genre":null,"length":204,"bitrate":44100,"vbr":false,"format":"mp3","play_count":0,"liked":false,"artist":{"id":"8a184724-cc1c-4258-9bed-e1a4f065de23","name":"Imagine Dragons"},"album":{"id":"27ee3771-2f33-4258-8ff0-ae6c1fda5e5f","name":"Believe","albumart":false}}],"id":"d64a0063-24d3-4952-a719-1f98cbb7cf01","name":"trial"}
         */
        return buildPlaylistJson(playlist).build();
    }
    @GET
    @Path("{id: [a-z0-9\\-]+}/lastfmrecommendation")
    public JsonObject getRecommendation(@PathParam("id") String playlistId) {
        //implement strategy pattern ig
        JsonObject playlist = playlistTracks(playlistId);
        final LastFmService lastFmService = AppContext.getInstance().getLastFmService();
        // iterate over all the tracks in the playlist,ie, playlist.tracks using a for loop
        // for each track, get the artist and album
        JsonArray tracks = playlist.getJsonArray("tracks");
        JsonArrayBuilder rectrackArray = Json.createArrayBuilder();
        for (int i=0;i<tracks.size();i++) {
            JsonObject track = tracks.getJsonObject(i);
            String artist = track.getJsonObject("artist").getString("name");
            String trackname = track.getString("title"); 
            Collection<de.umass.lastfm.Track> recom=lastFmService.getRecommendations(artist,trackname,1);
            for (de.umass.lastfm.Track t : recom) {
                rectrackArray.add(Json.createObjectBuilder()
                        .add("name", t.getName())
                        .add("artist", t.getArtist()));
            }
        }
        JsonObject response = Json.createObjectBuilder()
                .add("status", "ok")
                .add("tracks", rectrackArray)
                .build();
        System.out.println("response: " + response);
        return response;
    }


    public JsonObject spotifySearch(String trackName) {
        // Implement spotify search
        System.out.println("Searching using Spotify");

        String client_id = "955bd7aa550643ad92fb619df39cbde6";
        String client_secret = "66815b8214c443b7a445b61432d1180f";
        String access_token = null;

        StringBuffer response = new StringBuffer();

        try {
            // Obtain client credentials token
            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((client_id + ":" + client_secret).getBytes()));
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            String body = "grant_type=client_credentials";
            con.getOutputStream().write(body.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse access token from response
            String responseStr = response.toString();
            access_token = responseStr.substring(responseStr.indexOf("access_token") + 15,
                    responseStr.indexOf("token_type") - 3);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println(access_token);
        if (access_token != null) {
            try {
                // Make GET request to Spotify API

                // https://api.spotify.com/v1/recommendations?limit=1&market=ES&seed_artists=4NHQUGzhtTLFvgF5SZesLK&seed_genres=classical%2Ccountry&seed_tracks=0c6xIDDpzE81m2q797ordA"
                // -H "Accept: application/json" -H "Content-Type: application/json" -H
                // "Authorization: Bearer
                // BQCvSGd40LljEBD8zZrxJOIPLvgw-DWDKhViuYgLn2g-C--nADMl3IM-hziSTLlAp6e_aG-a51Qgorjqh9OTI1tskhqAgXci_5-P28-tf98_08cHy1Sm3D8QqrksHxH-qJJCbYRyDke-xYw13EA515TlLy6QgAL8ho3gxSqu0jFSJgr8PdtnJWvMJZN6NaCK

                trackName = trackName.replace(" ", "+");
                URL url = new URL("https://api.spotify.com/v1/search?q=" + trackName + "&type=track&limit=10&market=IN");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "Bearer " + access_token);
                con.setRequestProperty("Content-Type", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    System.out.println(inputLine);
                }
                in.close();

                // Print response
                System.out.println(response.toString());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
        JsonObject jsonObject = jsonReader.readObject();

        // THIS SHOULD IDEALLY BE RETURNED BUT THE CODE IS NEVER REACHING HERE
        return jsonObject;
        //return null;
    }


    @GET
    @Path("{id: [a-z0-9\\-]+}/spotifyrecommendation")
    public JsonObject getRecommendations(@PathParam("id") String playlistId) {
        // implements Spotify recommendations feature

        
        System.out.println("start");

        JsonArrayBuilder recommendedTracks = Json.createArrayBuilder();
        System.out.println("test");

        JsonObject playlist = playlistTracks(playlistId);
        System.out.println("test1");
        JsonArray tracks = playlist.getJsonArray("tracks");


        System.out.println("test2");
        
        for (int i=0;i<tracks.size();i++) {
            JsonObject track = tracks.getJsonObject(i);
            String trackName = track.getString("title"); 

            System.out.println("before");
            JsonObject searchJson = spotifySearch(trackName);

            System.out.println("after");

            JsonObject searchedTrack = searchJson.getJsonObject("tracks").getJsonArray("items").getJsonObject(0);
            String searchedTrackID = searchedTrack.getString("id");
            JsonObject searchedArtist = searchedTrack.getJsonArray("artists").getJsonObject(0);
            String searchedArtistID = searchedArtist.getString("id");

            String artistGenres = getArtistGenres(searchedArtistID);

            // "seed_artists=4NHQUGzhtTLFvgF5SZesLK&seed_genres=classical%2Ccountry&seed_tracks=0c6xIDDpzE81m2q797ordA"
            String recommendationsSeed = "seed_artists=" + searchedArtistID + "&seed_genres=" + artistGenres + "&seed_tracks=" + searchedTrackID;

            JsonObject recommendedJson = recommendationsAPI(recommendationsSeed);
            recommendedTracks.add(recommendedJson);
        }

        JsonObjectBuilder recommendations = Json.createObjectBuilder();
        recommendations.add("tracks", recommendedTracks.build());

        return recommendations.build();
    }

    public JsonObject recommendationsAPI(String recommendationsSeed) {
        // Implement spotify recommendations
        System.out.println("Recommending using Spotify");

        String client_id = "955bd7aa550643ad92fb619df39cbde6";
        String client_secret = "66815b8214c443b7a445b61432d1180f";
        String access_token = null;

        StringBuffer response = new StringBuffer();

        try {
            // Obtain client credentials token
            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((client_id + ":" + client_secret).getBytes()));
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            String body = "grant_type=client_credentials";
            con.getOutputStream().write(body.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse access token from response
            String responseStr = response.toString();
            access_token = responseStr.substring(responseStr.indexOf("access_token") + 15,
                    responseStr.indexOf("token_type") - 3);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println(access_token);
        if (access_token != null) {
            try {
                // Make GET request to Spotify API

                // https://api.spotify.com/v1/recommendations?limit=1&market=ES&seed_artists=4NHQUGzhtTLFvgF5SZesLK&seed_genres=classical%2Ccountry&seed_tracks=0c6xIDDpzE81m2q797ordA"
                // -H "Accept: application/json" -H "Content-Type: application/json" -H
                // "Authorization: Bearer
                // BQCvSGd40LljEBD8zZrxJOIPLvgw-DWDKhViuYgLn2g-C--nADMl3IM-hziSTLlAp6e_aG-a51Qgorjqh9OTI1tskhqAgXci_5-P28-tf98_08cHy1Sm3D8QqrksHxH-qJJCbYRyDke-xYw13EA515TlLy6QgAL8ho3gxSqu0jFSJgr8PdtnJWvMJZN6NaCK

                recommendationsSeed = recommendationsSeed.replace(" ", "+");
                URL url = new URL("https://api.spotify.com/v1/recommendations?limit=1&market=ES&" + recommendationsSeed);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "Bearer " + access_token);
                con.setRequestProperty("Content-Type", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    System.out.println(inputLine);
                }
                in.close();

                // Print response
                System.out.println(response.toString());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
        JsonObject jsonObject = jsonReader.readObject();

        return jsonObject;
    }

    public String getArtistGenres(String ArtistID) {
        System.out.println("Searching artist using Spotify");

        String client_id = "955bd7aa550643ad92fb619df39cbde6";
        String client_secret = "66815b8214c443b7a445b61432d1180f";
        String access_token = null;

        StringBuffer response = new StringBuffer();

        try {
            // Obtain client credentials token
            URL url = new URL("https://accounts.spotify.com/api/token");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString((client_id + ":" + client_secret).getBytes()));
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setDoOutput(true);
            String body = "grant_type=client_credentials";
            con.getOutputStream().write(body.getBytes());

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse access token from response
            String responseStr = response.toString();
            access_token = responseStr.substring(responseStr.indexOf("access_token") + 15,
                    responseStr.indexOf("token_type") - 3);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.println(access_token);
        if (access_token != null) {
            try {
                // Make GET request to Spotify API

                // https://api.spotify.com/v1/recommendations?limit=1&market=ES&seed_artists=4NHQUGzhtTLFvgF5SZesLK&seed_genres=classical%2Ccountry&seed_tracks=0c6xIDDpzE81m2q797ordA"
                // -H "Accept: application/json" -H "Content-Type: application/json" -H
                // "Authorization: Bearer
                // BQCvSGd40LljEBD8zZrxJOIPLvgw-DWDKhViuYgLn2g-C--nADMl3IM-hziSTLlAp6e_aG-a51Qgorjqh9OTI1tskhqAgXci_5-P28-tf98_08cHy1Sm3D8QqrksHxH-qJJCbYRyDke-xYw13EA515TlLy6QgAL8ho3gxSqu0jFSJgr8PdtnJWvMJZN6NaCK

                URL url = new URL("https://api.spotify.com/v1/artists/" + ArtistID);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", "Bearer " + access_token);
                con.setRequestProperty("Content-Type", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    System.out.println(inputLine);
                }
                in.close();

                // Print response
                System.out.println(response.toString());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        JsonReader jsonReader = Json.createReader(new StringReader(response.toString()));
        JsonObject jsonObject = jsonReader.readObject();

        JsonArray genres = jsonObject.getJsonArray("genres");
        String artistGenresString = "";
        for (int i=0;i<genres.size() && i<3;i++) {
            if(i != 0){
                artistGenresString = artistGenresString + ",";
            }
            artistGenresString = artistGenresString + genres.getString(i);
        }

        return artistGenresString;
    }

}
