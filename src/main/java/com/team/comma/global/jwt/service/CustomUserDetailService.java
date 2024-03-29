package com.team.comma.global.jwt.service;

import com.team.comma.domain.user.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository loginRepository;

    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loginRepository.findUserByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("토큰이 변조됐거나 사용자를 찾을 수 없습니다."));
    }

}