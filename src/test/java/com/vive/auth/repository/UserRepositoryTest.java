package com.vive.auth.repository;

import com.vive.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회 성공")
    void findByEmail_Success() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .provider(User.Provider.GOOGLE)
                .providerId("google123")
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(foundUser.get().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Provider와 ProviderId로 사용자 조회 성공")
    void findByProviderAndProviderId_Success() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .provider(User.Provider.GITHUB)
                .providerId("github456")
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByProviderAndProviderId(
                User.Provider.GITHUB, "github456");

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getProvider()).isEqualTo(User.Provider.GITHUB);
        assertThat(foundUser.get().getProviderId()).isEqualTo("github456");
    }

    @Test
    @DisplayName("이메일 존재 여부 확인")
    void existsByEmail_Success() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .provider(User.Provider.GOOGLE)
                .providerId("google123")
                .role(User.Role.USER)
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByEmail("test@example.com");
        boolean notExists = userRepository.existsByEmail("notfound@example.com");

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("사용자 정보 업데이트")
    void updateUser_Success() {
        // given
        User user = User.builder()
                .email("test@example.com")
                .name("Test User")
                .picture("old-picture.jpg")
                .provider(User.Provider.GOOGLE)
                .providerId("google123")
                .role(User.Role.USER)
                .build();
        User savedUser = userRepository.save(user);

        // when
        savedUser.update("Updated Name", "new-picture.jpg");
        userRepository.save(savedUser);

        // then
        Optional<User> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.get().getPicture()).isEqualTo("new-picture.jpg");
    }
}