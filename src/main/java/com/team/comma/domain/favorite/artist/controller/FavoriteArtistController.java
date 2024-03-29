package com.team.comma.domain.favorite.artist.controller;

import com.team.comma.domain.favorite.artist.dto.FavoriteArtistRequest;
import com.team.comma.domain.favorite.artist.service.FavoriteArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorite/artist")
public class FavoriteArtistController {

    private final FavoriteArtistService favoriteArtistService;

    @PostMapping
    public ResponseEntity createFavoriteArtist(
            @CookieValue String accessToken,
            @RequestBody FavoriteArtistRequest favoriteArtistRequest) throws AccountException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(favoriteArtistService.createFavoriteArtist(accessToken, favoriteArtistRequest.getSpotifyArtistId()));
    }

    @GetMapping
    public ResponseEntity findAllFavoriteArtist(
            @CookieValue String accessToken) throws AccountException {
        return ResponseEntity.ok()
                .body(favoriteArtistService.findAllFavoriteArtist(accessToken));
    }

    @GetMapping("/{artistName}") // artist 테이블 생성 후 수정 필요
    public ResponseEntity isFavoriteArtist(
            @CookieValue String accessToken,
            @PathVariable String artistName) throws AccountException {
        return ResponseEntity.ok()
                .body(favoriteArtistService.isFavoriteArtist(accessToken , artistName));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteFavoriteArtist(
            @CookieValue String accessToken,
            @PathVariable long id) throws AccountException {
        return ResponseEntity.ok()
                .body(favoriteArtistService.deleteFavoriteArtist(accessToken , id));
    }

}
