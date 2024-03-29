package com.team.comma.domain.favorite.artist.repository;

import com.team.comma.domain.favorite.artist.domain.FavoriteArtist;
import com.team.comma.domain.favorite.artist.dto.FavoriteArtistResponse;
import com.team.comma.domain.user.user.domain.User;

import java.util.List;
import java.util.Optional;

public interface FavoriteArtistRepositoryCustom {
    List<String> findFavoriteArtistListByUser(User user);

    Optional<FavoriteArtist> findFavoriteArtistByUser(User user , String artistName);

    List<FavoriteArtistResponse> findAllFavoriteArtistByUser(User user);

}
