package com.team.comma.domain.track.track.service;

import com.team.comma.global.common.dto.MessageResponse;
import com.team.comma.domain.track.track.domain.Track;
import com.team.comma.domain.track.playcount.domain.TrackPlayCount;
import com.team.comma.domain.track.playcount.dto.TrackPlayCountResponse;
import com.team.comma.domain.track.track.exception.TrackException;
import com.team.comma.domain.track.playcount.repository.TrackPlayCountRepository;
import com.team.comma.domain.favorite.track.repository.FavoriteTrackRepository;
import com.team.comma.domain.track.track.repository.TrackRepository;
import com.team.comma.global.jwt.support.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.security.auth.login.AccountException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.team.comma.global.common.constant.ResponseCodeEnum.REQUEST_SUCCESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class TrackServiceTest {

    @InjectMocks
    TrackService trackService;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    FavoriteTrackRepository favoriteTrackRepository;

    @Mock
    TrackRepository trackRepository;

    @Test
    @DisplayName("좋아요 표기한 곡 탐색")
    void findTrackByFavoriteTrack() {
        // given
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tracks.add(buildTrack("title", "trackId"));
        }

        doReturn(tracks).when(favoriteTrackRepository).findFavoriteTrackByEmail(any(String.class));
        doReturn("userEmail").when(jwtTokenProvider).getUserPk("token");

        // when
        MessageResponse result = trackService.findTrackByFavoriteTrack("token");

        // then
        assertThat(result.getCode()).isEqualTo(REQUEST_SUCCESS.getCode());
        assertThat(result.getMessage()).isEqualTo(REQUEST_SUCCESS.getMessage());
        assertThat(((List) result.getData()).size()).isEqualTo(5);
    }

    @Test
    @DisplayName("가장 많은 추천 곡 가져오기")
    void findTrackMostRecommended() throws AccountException {
        // given
        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            tracks.add(buildTrack("title", "trackId"));
        }
        doReturn(tracks).when(trackRepository).findTrackMostRecommended();

        // when
        MessageResponse result = trackService.findTrackByMostFavorite();

        // then
        assertThat(result.getMessage()).isEqualTo(REQUEST_SUCCESS.getMessage());
        assertThat(((List) result.getData()).size()).isEqualTo(5);
    }

    private Track buildTrack(String title, String spotifyId) {
        return Track.builder()
                .trackTitle(title)
                .recommendCount(0L)
                .albumImageUrl("url")
                .spotifyTrackHref("spotifyTrackHref")
                .spotifyTrackId(spotifyId)
                .build();
    }

}