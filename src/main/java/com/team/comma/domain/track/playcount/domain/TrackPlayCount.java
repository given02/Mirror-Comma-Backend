package com.team.comma.domain.track.playcount.domain;

import com.team.comma.domain.track.track.domain.Track;
import com.team.comma.domain.user.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Builder
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "track_play_count_tb")
public class TrackPlayCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id")
    private Track track;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    public static TrackPlayCount createTrackPlayCount(Track track, User user) {
        return TrackPlayCount.builder()
                .track(track)
                .user(user)
                .build();
    }

}
