package com.team.comma.domain.favorite.track.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team.comma.domain.artist.dto.ArtistResponse;
import com.team.comma.domain.favorite.track.dto.FavoriteTrackResponse;
import com.team.comma.domain.track.artist.dto.TrackArtistResponse;
import com.team.comma.domain.track.track.dto.TrackResponse;
import com.team.comma.domain.user.user.domain.User;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.querydsl.core.group.GroupBy.groupBy;
import static com.team.comma.domain.artist.domain.QArtist.artist;
import static com.team.comma.domain.favorite.track.domain.QFavoriteTrack.favoriteTrack;
import static com.team.comma.domain.track.artist.domain.QTrackArtist.trackArtist;
import static com.team.comma.domain.track.track.domain.QTrack.track;
import static com.team.comma.domain.user.user.domain.QUser.user;

@RequiredArgsConstructor
public class FavoriteTrackRepositoryImpl implements FavoriteTrackRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<FavoriteTrackResponse> findAllFavoriteTrackByUser(User user) {
        return jpaQueryFactory.select(
                        Projections.constructor(
                                FavoriteTrackResponse.class,
                                favoriteTrack.id,
                                Projections.list(
                                        Projections.constructor(
                                                TrackArtistResponse.class,
                                                Projections.constructor(TrackResponse.class,
                                                        track.id,
                                                        track.trackTitle,
                                                        track.durationTimeMs,
                                                        track.recommendCount,
                                                        track.albumImageUrl,
                                                        track.spotifyTrackId,
                                                        track.spotifyTrackHref
                                                ),
                                                Projections.constructor(ArtistResponse.class,
                                                        artist.spotifyArtistId.min(),
                                                        artist.artistName.min(),
                                                        artist.artistImageUrl.min()
                                                )))))
                .from(favoriteTrack)
                .innerJoin(favoriteTrack.track, track)
                .innerJoin(track.trackArtistList, trackArtist)
                .innerJoin(trackArtist.artist, artist)
                .where(favoriteTrack.user.eq(user))
                .groupBy(favoriteTrack , track)
                .orderBy(favoriteTrack.id.desc())
                .fetch();
    }
}
