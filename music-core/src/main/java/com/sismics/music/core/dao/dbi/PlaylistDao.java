package com.sismics.music.core.dao.dbi;

import com.sismics.music.core.constant.PlaylistVisibilityEnum;
import com.google.common.collect.Lists;
import com.sismics.music.core.dao.dbi.criteria.PlaylistCriteria;
import com.sismics.music.core.dao.dbi.dto.PlaylistDto;
import com.sismics.music.core.dao.dbi.mapper.PlaylistMapper;
import com.sismics.music.core.model.dbi.Playlist;
import com.sismics.music.core.util.dbi.QueryParam;
import com.sismics.util.context.ThreadLocalContext;
import com.sismics.util.dbi.BaseDao;
import com.sismics.util.dbi.filter.FilterCriteria;
import org.skife.jdbi.v2.Handle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Playlist DAO.
 * 
 * @author jtremeaux
 */
public class PlaylistDao extends BaseDao<PlaylistDto, PlaylistCriteria> {
    @Override
    protected QueryParam getQueryParam(PlaylistCriteria criteria, FilterCriteria filterCriteria) {
        List<String> criteriaList = new ArrayList<>();
        Map<String, Object> parameterMap = new HashMap<>();

        StringBuilder sb = new StringBuilder("select p.id as id, p.name as c0, p.visibility as v,")
                .append("  p.user_id as userId,")
                .append("  count(pt.id) as c1,")
                .append("  sum(utr.playcount) as c2")
                .append("  from t_playlist p")
                .append("  left join t_playlist_track pt on(pt.playlist_id = p.id)")
                .append("  left join t_user_track utr on(utr.track_id = pt.track_id)");

        // Adds search criteria
        if (criteria.getId() != null) {
            criteriaList.add("p.id = :id");
            parameterMap.put("id", criteria.getId());
        }
        if (criteria.getUserId() != null) {
            criteriaList.add("p.user_id = :userId");
            parameterMap.put("userId", criteria.getUserId());
        }
        if (criteria.getDefaultPlaylist() != null) {
            if (criteria.getDefaultPlaylist()) {
                criteriaList.add("p.name is null");
            } else {
                criteriaList.add("p.name is not null");
            }
        }
        if (criteria.getNameLike() != null) {
            criteriaList.add("lower(p.name) like lower(:nameLike)");
            parameterMap.put("nameLike", "%" + criteria.getNameLike() + "%");
        }
        if (criteria.getVisibility() != null) {
            criteriaList.add("p.visibility = :visibility");
            parameterMap.put("visibility", criteria.getVisibility().toString());
        }

        return new QueryParam(sb.toString(), criteriaList, parameterMap, null, filterCriteria, Lists.newArrayList("p.id"), new PlaylistMapper());
    }

    /**
     * Creates a new playlist.
     * 
     * @param playlist Playlist to create
     * @return Playlist ID
     */
    public String create(Playlist playlist) {
        // playlist.setId(UUID.randomUUID().toString());
        final Handle handle = ThreadLocalContext.get().getHandle();
        System.out.println("Playlist creation in progress for user: " + playlist.getUserId() + " and name: " + playlist.getName());
        handle.createStatement("insert into " +
                "  t_playlist(id, user_id, name)" +
                "  values(:id, :userId, :name)")
                .bind("id", playlist.getId())
                .bind("userId", playlist.getUserId())
                .bind("name", playlist.getName())
                .execute();

        return playlist.getId();
    }

    public void updateVisibility(Playlist playlist) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_playlist" +
                "  set visibility = :visibility" +
                "  where id = :id")
                .bind("visibility", playlist.getVisibility().toString())
                .bind("id", playlist.getId())
                .execute();
    }
    /**
     * Update a playlist.
     *
     * @param playlist Playlist to update
     */
    public void update(Playlist playlist) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("update t_playlist" +
                "  set name = :name," +
                "  visibility = :visibility" +
                "  where id = :id")
                .bind("name", playlist.getName())
                .bind("visibility", playlist.getVisibility().toString())
                .bind("id", playlist.getId())
                .execute();
    }

    /**
     * Delete a playlist.
     *
     * @param playlist Playlist to delete
     */
    public void delete(Playlist playlist) {
        final Handle handle = ThreadLocalContext.get().getHandle();
        handle.createStatement("delete from " +
                "  t_playlist_track" +
                "  where playlist_id = :playlistId")
                .bind("playlistId", playlist.getId())
                .execute();
        handle.createStatement("delete from " +
                "  t_playlist" +
                "  where id = :id")
                .bind("id", playlist.getId())
                .execute();
    }

    /**
     * Gets a playlist by user ID.
     *
     * @param userId User ID
     * @return Playlist
     */
    public PlaylistDto getDefaultPlaylistByUserId(String userId) {
        return findFirstByCriteria(new PlaylistCriteria()
                .setDefaultPlaylist(true)
                .setUserId(userId));
    }

    /**
     * Assemble the query results.
     *
     * @param resultList Query results as a table
     * @return Query results as a list of domain objects
     */
    private List<PlaylistDto> assembleResultList(List<Object[]> resultList) {
        List<PlaylistDto> playlistDtoList = new ArrayList<>();
        for (Object[] o : resultList) {
            int i = 0;
            PlaylistDto playlistDto = new PlaylistDto();
            playlistDto.setId((String) o[i++]);
            playlistDto.setName((String) o[i++]);
            playlistDto.setUserId((String) o[i]);
            playlistDtoList.add(playlistDto);
        }
        return playlistDtoList;
    }
}
