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
            log.info("=== OAuth2 User Loading Started ===");
            log.info("Provider: {}", userRequest.getClientRegistration().getRegistrationId());

            OAuth2User oAuth2User = super.loadUser(userRequest);
            log.info("OAuth2User loaded successfully from provider");

            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            log.info("Registration ID: {}, User Name Attribute: {}", registrationId, userNameAttributeName);
            log.info("OAuth2 Attributes: {}", oAuth2User.getAttributes());

            OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
            log.info("OAuth2UserInfo created - ID: {}, Name: {}, Email: {}",
                    oAuth2UserInfo.getId(), oAuth2UserInfo.getName(), oAuth2UserInfo.getEmail());

            User user = saveOrUpdate(oAuth2UserInfo, registrationId);
            log.info("User saved/updated successfully - ID: {}, Email: {}", user.getId(), user.getEmail());

            UserPrincipal userPrincipal = UserPrincipal.create(user, oAuth2User.getAttributes());
            log.info("UserPrincipal created - Username: {}", userPrincipal.getUsername());

            log.info("=== OAuth2 User Loading Completed ===");
            return userPrincipal;
        } catch (Exception e) {
            log.error("Error loading OAuth2 user", e);
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
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
        try {
            log.info("Saving or updating user - Provider: {}, ProviderId: {}", registrationId, oAuth2UserInfo.getId());

            User.Provider provider = User.Provider.valueOf(registrationId.toUpperCase());
            log.info("Provider enum resolved: {}", provider);

            User user = userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getId())
                    .map(entity -> {
                        log.info("Existing user found, updating...");
                        return entity.update(oAuth2UserInfo.getName(), oAuth2UserInfo.getImageUrl());
                    })
                    .orElseGet(() -> {
                        log.info("New user, creating...");
                        // 이메일이 없을 경우 providerId@provider.local 형식으로 생성
                        String email = oAuth2UserInfo.getEmail();
                        if (email == null || email.isEmpty()) {
                            email = oAuth2UserInfo.getId() + "@" + registrationId.toLowerCase() + ".oauth";
                            log.info("Email not provided by OAuth2 provider, using generated email: {}", email);
                        }

                        return User.builder()
                                .email(email)
                                .name(oAuth2UserInfo.getName())
                                .picture(oAuth2UserInfo.getImageUrl())
                                .provider(provider)
                                .providerId(oAuth2UserInfo.getId())
                                .role(User.Role.USER)
                                .build();
                    });

            User savedUser = userRepository.save(user);
            log.info("User saved successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            log.error("Error saving/updating user", e);
            throw e;
        }
    }
}