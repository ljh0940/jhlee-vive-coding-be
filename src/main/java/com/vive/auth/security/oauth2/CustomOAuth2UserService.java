package com.vive.auth.security.oauth2;

import com.vive.auth.entity.User;
import com.vive.auth.repository.UserRepository;
import com.vive.auth.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = super.loadUser(userRequest);

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

            User user = saveOrUpdate(oAuth2UserInfo, registrationId);
            log.info("OAuth2 user authenticated: {}", user.getEmail());

            return UserPrincipal.create(user, oAuth2User.getAttributes());
        } catch (Exception e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("Failed to load OAuth2 user: " + e.getMessage());
        }
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private User saveOrUpdate(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());

        User user = userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getId())
                .map(entity -> {
                    entity.update(oAuth2UserInfo.getName(), oAuth2UserInfo.getImageUrl());
                    entity.updateLastLogin();
                    return entity;
                })
                .orElseGet(() -> {
                    // 이메일이 없을 경우 providerId@provider.oauth 형식으로 생성
                    String email = oAuth2UserInfo.getEmail();
                    if (email == null || email.isEmpty()) {
                        email = oAuth2UserInfo.getId() + "@" + registrationId.toLowerCase() + ".oauth";
                    }

                    User newUser = User.builder()
                            .email(email)
                            .name(oAuth2UserInfo.getName())
                            .picture(oAuth2UserInfo.getImageUrl())
                            .provider(provider)
                            .providerId(oAuth2UserInfo.getId())
                            .role(User.Role.USER)
                            .build();
                    newUser.updateLastLogin();
                    return newUser;
                });

        return userRepository.save(user);
    }
}