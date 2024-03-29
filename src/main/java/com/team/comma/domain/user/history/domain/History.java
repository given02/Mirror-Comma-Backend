package com.team.comma.domain.user.history.domain;

import com.team.comma.domain.user.user.domain.User;
import com.team.comma.global.converter.BooleanConverter;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "history_tb")
public class History {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 50)
    private String searchHistory;

    @Convert(converter = BooleanConverter.class)
    private boolean delFlag;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
}
