package com.team.comma.global.auth.service;

import com.team.comma.global.auth.exception.OAuth2EmailNotFoundException;
import com.team.comma.global.auth.support.OAuthAttributes;
import com.team.comma.domain.user.user.constant.UserRole;
import com.team.comma.domain.user.user.constant.UserType;
import com.team.comma.domain.user.user.domain.User;
import com.team.comma.domain.user.user.dto.UserSession;
import com.team.comma.domain.user.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
            .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName,
            oAuth2User.getAttributes());

        User user = saveOrUpdateUser(attributes);
        httpSession.setAttribute("user", UserSession.of(user));

        return new DefaultOAuth2User(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
            attributes.getAttributes(), attributes.getNameAttributeKey());
    }

    public User saveOrUpdateUser(OAuthAttributes attributes) {
        if (attributes.getEmail() == null) {
            throw new OAuth2EmailNotFoundException("OAuth2 계정의 이메일을 찾을 수 없습니다.");
        }

        return userRepository.findUserByEmail(attributes.getEmail())
            .orElseGet(() -> createAndSaveUser(attributes));
    }

    public User createAndSaveUser(OAuthAttributes attributes) {
        User createUser = User.builder().email(attributes.getEmail())
            .role(UserRole.USER).type(UserType.OAUTH_USER).build();
        return userRepository.save(createUser);
    }
}
