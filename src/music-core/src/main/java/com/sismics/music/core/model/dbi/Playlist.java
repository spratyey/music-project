package com.sismics.music.core.model.dbi;

import com.google.common.base.Objects;
import com.sismics.music.core.dao.dbi.PlaylistDao;
import com.sismics.music.core.constant.PlaylistVisibilityEnum;

import java.util.UUID;

/**
 * Playlist entity.
 * A playlist is either the list of current played tracks (the "default" playlist), or a saved playlist with a name.
 * 
 * @author jtremeaux
 * @author yash4242
 */
public class Playlist {
    /**
     * Playlist ID.
     */
    private String id;

    /**
     * User ID.
     */
    private String userId;

    /**
     * Playlist name.
     */
    private String name;

    /**
     * Visibilty of the playlist.
     */
    private PlaylistVisibilityEnum visibility;


    public Playlist() {
        this.visibility = PlaylistVisibilityEnum.PUBLIC;
    }

    public Playlist(String id) {
        this.id = id;
        this.visibility = PlaylistVisibilityEnum.PUBLIC;
    }

    public Playlist(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.visibility = PlaylistVisibilityEnum.PUBLIC;
    }

    /**
     * Getter of id.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter of id.
     *
     * @param id id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter of userId.
     *
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setter of userId.
     *
     * @param userId userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Getter of visibility.
     *
     * @return visibility
     */
    public PlaylistVisibilityEnum getVisibility() {
        return visibility;
    }

    /**
     * Setter of visibility.
     *
     * @param visibility visibility
     */
    public void setVisibility(PlaylistVisibilityEnum visibility) {
        this.visibility = visibility;
    }



    /**
     * Create a named playlist.
     *
     * @param playlist The playlist to create
     */
    public static void createPlaylist(Playlist playlist) {
        playlist.id = UUID.randomUUID().toString();
        new PlaylistDao().create(playlist);
    }

    /**
     * Update a named playlist.
     *
     * @param playlist The playlist to update
     */
    public static void updatePlaylist(Playlist playlist) {
        new PlaylistDao().update(playlist);
    }

    /**
     * Delete a named playlist.
     *
     * @param playlist The playlist to delete
     */
    public static void deletePlaylist(Playlist playlist) {
        new PlaylistDao().delete(playlist);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("userId", userId)
                .add("name", name)
                .toString();
    }
}
