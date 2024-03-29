package com.team.comma.domain.favorite.track.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class FavoriteTrackRequest {

    private String spotifyTrackId;

    public static FavoriteTrackRequest buildFavoriteTrackRequest() {
        return FavoriteTrackRequest.builder()
                .spotifyTrackId("spotifyTrackId")
                .build();
    }
}
