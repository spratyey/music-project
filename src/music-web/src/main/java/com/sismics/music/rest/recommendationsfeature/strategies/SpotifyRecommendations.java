package com.sismics.music.rest.recommendationsfeature.strategies;

import com.sismics.music.rest.recommendationsfeature.RecommendationsStrategy;
import com.sismics.music.rest.resource.PlaylistResource;
import com.sismics.music.rest.searchfeature.strategies.SpotifySearch;

import java.net.URL;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.Json;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.Base64;

public class SpotifyRecommendations implements RecommendationsStrategy{
    public JsonObject getRecommendations(String playlistId) {
        // implements Spotify recommendations feature

        JsonArrayBuilder recommendedTracks = Json.createArrayBuilder();

        PlaylistResource playlistResource = new PlaylistResource();
        JsonObject playlist = playlistResource.playlistTracks(playlistId);
        JsonArray tracks = playlist.getJsonArray("tracks");

        SpotifySearch spotifySearch = new SpotifySearch();
        
        for (int i=0;i<tracks.size();i++) {
            JsonObject track = tracks.getJsonObject(i);
            String trackName = track.getString("title"); 
            JsonObject searchJson = spotifySearch.search(trackName);

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

                URL url = new URL("https://open.spotify.com/artist/" + ArtistID);
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
