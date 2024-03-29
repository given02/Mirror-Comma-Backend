package com.team.comma.domain.track.track.controller;

import com.team.comma.domain.track.track.service.TrackService;
import com.team.comma.global.message.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(value = "/tracks")
@RestController
@RequiredArgsConstructor
public class TrackController {
    private final TrackService trackService;

    @GetMapping("/recommend")
    public MessageResponse findTrackByMostFavorite() {
        return trackService.findTrackByMostFavorite();
    }

}
