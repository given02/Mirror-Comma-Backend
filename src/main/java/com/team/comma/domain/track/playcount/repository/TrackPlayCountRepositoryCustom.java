package com.team.comma.domain.track.playcount.repository;

import com.team.comma.domain.track.playcount.dto.TrackPlayCountResponse;
import com.team.comma.domain.track.playcount.domain.TrackPlayCount;

import java.util.List;
import java.util.Optional;

public interface TrackPlayCountRepositoryCustom {

    List<TrackPlayCountResponse> findTrackPlayCountByFriend(String userEmail);

    List<TrackPlayCountResponse> findTrackPlayCountByMostListenedTrack(String userEmail);

}
