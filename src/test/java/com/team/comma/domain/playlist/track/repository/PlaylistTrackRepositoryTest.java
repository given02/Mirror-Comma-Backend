package com.team.comma.domain.playlist.track.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.team.comma.domain.artist.domain.Artist;
import com.team.comma.domain.artist.repository.ArtistRepository;
import com.team.comma.domain.playlist.playlist.domain.Playlist;
import com.team.comma.domain.playlist.track.domain.PlaylistTrack;
import com.team.comma.domain.playlist.track.dto.PlaylistTrackResponse;
import com.team.comma.domain.playlist.playlist.repository.PlaylistRepository;
import com.team.comma.domain.track.artist.domain.TrackArtist;
import com.team.comma.domain.track.track.domain.Track;
import com.team.comma.domain.track.track.repository.TrackRepository;
import com.team.comma.domain.user.user.constant.UserRole;
import com.team.comma.domain.user.user.constant.UserType;
import com.team.comma.domain.user.user.domain.User;
import com.team.comma.domain.user.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import com.team.comma.global.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestConfig.class)
class PlaylistTrackRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private PlaylistTrackRepository playlistTrackRepository;

    @Autowired
    private ArtistRepository artistRepository;

    private final String userEmail = "email@naver.com";

    private final String title = "test playlist";

    private final String trackTitle = "test track";

    @Test
    void 플레이리스트_곡_연관관계_저장() {
        // given
        final User user = userRepository.save(buildUser());
        final Playlist playlist = playlistRepository.save(buildPlaylist(user, title));
        final Track track = trackRepository.save(buildTrack(trackTitle));

        // when
        final PlaylistTrack result = playlistTrackRepository.save(
                buildPlaylistTrack(playlist, track));

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPlaylist().getId()).isEqualTo(playlist.getId());
        assertThat(result.getTrack().getId()).isEqualTo(track.getId());
    }

    @Test
    void 플레이리스트_트랙_상세_조회() {
        // given
        final User user = buildUser();
        userRepository.save(user);

        final Playlist playlist = buildPlaylist(user, "test playlist");
        playlistRepository.save(playlist);

        Artist artist = Artist.builder().spotifyArtistId("artistId").artistName("artist").build();
        artistRepository.save(artist);

        final Track track1 = buildTrack("test track");
        track1.addTrackArtistList(artist);
        final Track track2 = buildTrack("test track");
        track2.addTrackArtistList(artist);
        trackRepository.save(track1);
        trackRepository.save(track2);



        final PlaylistTrack playlistTrack1 = buildPlaylistTrack(playlist,track1);
        final PlaylistTrack playlistTrack2 = buildPlaylistTrack(playlist,track2);
        playlistTrackRepository.save(playlistTrack1);
        playlistTrackRepository.save(playlistTrack2);

        // when
        final List<PlaylistTrackResponse> result = playlistTrackRepository.getPlaylistTracksByPlaylist(playlist);

        // then
        assertThat(result.size()).isEqualTo(2);

    }

    @Test
    void deletePlaylistTracksByIds_Success() {
        //given
        final User user = buildUser();
        userRepository.save(user);

        final Playlist playlist = buildPlaylist(user, "test playlist");
        playlistRepository.save(playlist);

        final Track track1 = buildTrack("test track");
        final Track track2 = buildTrack("test track");
        trackRepository.save(track1);
        trackRepository.save(track2);

        final PlaylistTrack playlistTrack1 = buildPlaylistTrack(playlist,track1);
        final PlaylistTrack playlistTrack2 = buildPlaylistTrack(playlist,track2);
        playlistTrackRepository.save(playlistTrack1);
        playlistTrackRepository.save(playlistTrack2);

        //when
        long result = playlistTrackRepository.deletePlaylistTracksByIds(
                List.of(playlistTrack1.getId(), playlistTrack2.getId()));

        //then
        assertThat(result).isEqualTo(2);
    }

    private User buildUser() {
        return User.builder()
            .email(userEmail)
            .type(UserType.GENERAL_USER)
            .role(UserRole.USER)
            .build();
    }

    private Playlist buildPlaylist(User user, String title) {
        return Playlist.builder()
            .playlistTitle(title)
            .alarmFlag(true)
            .user(user)
            .build();
    }

    private Track buildTrack(String title) {
        return Track.builder()
            .trackTitle(title)
                .albumImageUrl("url")
                .spotifyTrackId("trackId")
                .spotifyTrackHref("href")
            .build();
    }

    private PlaylistTrack buildPlaylistTrack(Playlist playlist, Track track) {
        return PlaylistTrack.builder()
            .playlist(playlist)
            .track(track)
            .build();
    }
}
