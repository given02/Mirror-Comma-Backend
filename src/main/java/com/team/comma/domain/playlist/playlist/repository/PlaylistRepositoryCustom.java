package com.team.comma.domain.playlist.playlist.repository;

import com.team.comma.domain.playlist.playlist.dto.PlaylistResponse;
import com.team.comma.domain.user.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepositoryCustom {

    int findTotalDurationTimeMsByPlaylistId(Long playlistId);

    int findMaxListSequence();

    long modifyAlarmFlag(long id, boolean alarmFlag);

    long deletePlaylist(long id);

    List<PlaylistResponse> findAllPlaylistsByUser(User user);

    Optional<PlaylistResponse> findPlaylistsByPlaylistId(long playlistId);
}