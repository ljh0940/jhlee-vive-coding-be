package com.vive.auth.config;

import com.vive.auth.entity.User;
import com.vive.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 관리자 계정이 없으면 생성
        if (!userRepository.existsByEmail("admin")) {
            User admin = User.builder()
                    .email("admin")
                    .password(passwordEncoder.encode("admin"))
                    .name("관리자")
                    .role(User.Role.ADMIN)
                    .provider(User.Provider.LOCAL)
                    .build();

            userRepository.save(admin);
            log.info("Admin account created: email=admin, password=admin");
        } else {
            log.info("Admin account already exists");
        }
    }
}
