package com.team.comma.domain.favorite.artist.service;

import com.team.comma.domain.artist.domain.Artist;
import com.team.comma.domain.artist.service.ArtistService;
import com.team.comma.domain.favorite.artist.domain.FavoriteArtist;
import com.team.comma.domain.favorite.artist.repository.FavoriteArtistRepository;
import com.team.comma.domain.user.user.domain.User;
import com.team.comma.domain.user.user.exception.UserException;
import com.team.comma.domain.user.user.repository.UserRepository;
import com.team.comma.global.message.MessageResponse;
import com.team.comma.global.jwt.support.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountException;

import static com.team.comma.global.constant.ResponseCodeEnum.NOT_FOUNT_USER;
import static com.team.comma.global.constant.ResponseCodeEnum.REQUEST_SUCCESS;

@Service
@RequiredArgsConstructor
public class FavoriteArtistService {

    private final FavoriteArtistRepository favoriteArtistRepository;
    private final ArtistService artistService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse createFavoriteArtist(String token , String spotifyArtistId) throws AccountException {
        String userEmail = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserException(NOT_FOUNT_USER));
        Artist artist = artistService.findArtistOrSave(spotifyArtistId);

        FavoriteArtist favoriteArtist = FavoriteArtist.createFavoriteArtist(user, artist);
        favoriteArtistRepository.save(favoriteArtist);

        return MessageResponse.of(REQUEST_SUCCESS);
    }

    public MessageResponse findAllFavoriteArtist(final String accessToken) throws AccountException{
        String userEmail = jwtTokenProvider.getUserPk(accessToken);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserException(NOT_FOUNT_USER));

        return MessageResponse.of(REQUEST_SUCCESS,
                favoriteArtistRepository.findAllFavoriteArtistByUser(user));
    }

    public MessageResponse isFavoriteArtist(String token , String artistName) throws AccountException {
        String userEmail = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserException(NOT_FOUNT_USER));
        boolean isAddedArtist = false;

        if(isAddedFavoriteArtist(user , artistName)) {
            isAddedArtist = true;
        }

        return MessageResponse.of(REQUEST_SUCCESS , isAddedArtist);
    }

    public boolean isAddedFavoriteArtist(User user , String artistName) {
        if(favoriteArtistRepository.findFavoriteArtistByUser(user , artistName).isPresent()) {
            return true;
        }
        return false;
    }

    @Transactional
    public MessageResponse deleteFavoriteArtist(String token , long favoriteArtistId) throws AccountException {
        String userEmail = jwtTokenProvider.getUserPk(token);
        User user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UserException(NOT_FOUNT_USER));

        favoriteArtistRepository.deleteById(favoriteArtistId);

        return MessageResponse.of(REQUEST_SUCCESS);
    }

}
